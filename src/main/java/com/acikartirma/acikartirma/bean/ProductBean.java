package com.acikartirma.acikartirma.bean;

import com.acikartirma.acikartirma.entity.*;
import com.acikartirma.acikartirma.enums.TransactionType;
import com.acikartirma.acikartirma.facadelocal.*;
import com.acikartirma.acikartirma.websocket.AuctionWebSocket;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.OptimisticLockException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class ProductBean implements Serializable {

    @EJB(beanName = "ProductFacade")
    private ProductFacadeLocal productFacade;

    @EJB
    private CategoryFacadeLocal categoryFacade;

    @EJB
    private UserFacadeLocal userFacade;

    @EJB
    private TransactionFacadeLocal transactionFacade;

    @EJB
    private NotificationFacadeLocal notificationFacade;

    @EJB
    private BidFacadeLocal bidFacade;

    @Inject
    private UserBean userBean;

    private List<Product> allProducts;
    private List<Category> categories;
    private Long selectedCategoryId;
    private Long filterCategoryId;

    private String name;
    private String description;
    private BigDecimal startingPrice;
    private Integer durationInMinutes;
    private String imagePath;

    @PostConstruct
    public void init() {
        refreshProductList();
        categories = categoryFacade.findAll();
    }

    public void refreshProductList() {
        allProducts = productFacade.findAll().stream()
                .sorted(Comparator.comparing(Product::getId).reversed())
                .collect(Collectors.toList());
    }

    public void filterProducts() {
        List<Product> baseList = productFacade.findAll();
        if (filterCategoryId == null || filterCategoryId == 0L) {
            allProducts = baseList.stream()
                    .sorted(Comparator.comparing(Product::getId).reversed())
                    .collect(Collectors.toList());
        } else {
            allProducts = baseList.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(filterCategoryId))
                    .sorted(Comparator.comparing(Product::getId).reversed())
                    .collect(Collectors.toList());
        }
    }

    public void saveProduct() {
        Product newProduct = new Product();
        newProduct.setName(name);
        newProduct.setDescription(description);
        newProduct.setStartingPrice(startingPrice);
        newProduct.setCurrentPrice(startingPrice);
        newProduct.setStartTime(LocalDateTime.now());
        newProduct.setEndTime(LocalDateTime.now().plusMinutes(durationInMinutes));
        newProduct.setImagePath(imagePath);

        if (selectedCategoryId != null) {
            newProduct.setCategory(categoryFacade.find(selectedCategoryId));
        }

        newProduct.setSeller(userBean.getCurrentUser());
        productFacade.create(newProduct);

        name = description = imagePath = null;
        startingPrice = null;
        durationInMinutes = null;
        selectedCategoryId = null;

        refreshProductList();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Başarılı", "Ürün ihaleye çıkarıldı."));
    }

    public void placeBid(Product product) {
        try {
            User currentUser = userBean.getCurrentUser();
            BigDecimal bidAmount = product.getNewBidAmount();

            // 1. Veritabanından ürünün en taze halini al (Çoğalmayı önleyen en kritik adım)
            Product managedProduct = productFacade.find(product.getId());

            if (bidAmount == null || bidAmount.compareTo(managedProduct.getCurrentPrice()) <= 0) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hata", "Teklif mevcut fiyattan yüksek olmalıdır."));
                return;
            }

            if (currentUser.getBalance().compareTo(bidAmount) < 0) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hata", "Yetersiz bakiye."));
                return;
            }

            // 2. ESKİ KAZANANA İADE
            if (managedProduct.getWinner() != null && !managedProduct.getWinner().getId().equals(currentUser.getId())) {
                User oldWinner = managedProduct.getWinner();
                oldWinner.setBalance(oldWinner.getBalance().add(managedProduct.getCurrentPrice()));
                userFacade.update(oldWinner);

                Transaction refund = new Transaction();
                refund.setUser(oldWinner);
                refund.setAmount(managedProduct.getCurrentPrice());
                refund.setType(TransactionType.BID_REFUND);
                transactionFacade.create(refund);

                Notification refundNotif = new Notification();
                refundNotif.setUser(oldWinner);
                refundNotif.setMessage("'" + managedProduct.getName() + "' ürünündeki teklifiniz geçildi. Bakiyeniz iade edildi.");
                notificationFacade.create(refundNotif);
            }

            // 3. YENİ TEKLİF VERENİN BAKİYESİNİ DÜŞ
            currentUser.setBalance(currentUser.getBalance().subtract(bidAmount));
            userFacade.update(currentUser);
            // Session'ı veritabanından tazeleyerek güncelle (8 TL görünmesi için)
            userBean.setCurrentUser(userFacade.find(currentUser.getId()));

            Transaction deduction = new Transaction();
            deduction.setUser(currentUser);
            deduction.setAmount(bidAmount);
            deduction.setType(TransactionType.BID_DEDUCTION);
            transactionFacade.create(deduction);

            // 4. ÜRÜNÜ GÜNCELLE
            managedProduct.setCurrentPrice(bidAmount);
            managedProduct.setWinner(currentUser);
            productFacade.update(managedProduct);

            // 5. TEKLİF KAYDI OLUŞTUR
            Bid newBid = new Bid();
            newBid.setAmount(bidAmount);
            newBid.setBidder(currentUser);
            newBid.setProduct(managedProduct);
            bidFacade.create(newBid);

            product.setNewBidAmount(null);
            filterProducts();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Başarılı", "Teklifiniz alındı."));

            AuctionWebSocket.broadcast("'" + managedProduct.getName() + "' için yeni teklif: " + bidAmount + " TL");

        } catch (OptimisticLockException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Sistem Uyarısı", "Başka bir kullanıcı teklif verdi. Sayfayı yenileyin."));
            refreshProductList();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hata", "İşlem başarısız: " + e.getMessage()));
        }
    }

    public void deleteProduct(Product product) {
        productFacade.remove(product);
        filterProducts();
    }

    // Getter ve Setterlar
    public List<Product> getAllProducts() { return allProducts; }
    public void setAllProducts(List<Product> allProducts) { this.allProducts = allProducts; }
    public List<Category> getCategories() { return categories; }
    public void setCategories(List<Category> categories) { this.categories = categories; }
    public Long getSelectedCategoryId() { return selectedCategoryId; }
    public void setSelectedCategoryId(Long selectedCategoryId) { this.selectedCategoryId = selectedCategoryId; }
    public Long getFilterCategoryId() { return filterCategoryId; }
    public void setFilterCategoryId(Long filterCategoryId) { this.filterCategoryId = filterCategoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getStartingPrice() { return startingPrice; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }
    public Integer getDurationInMinutes() { return durationInMinutes; }
    public void setDurationInMinutes(Integer durationInMinutes) { this.durationInMinutes = durationInMinutes; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}