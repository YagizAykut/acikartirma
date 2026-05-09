package com.acikartirma.acikartirma.bean;

import com.acikartirma.acikartirma.entity.Category;
import com.acikartirma.acikartirma.entity.Notification;
import com.acikartirma.acikartirma.entity.Product;
import com.acikartirma.acikartirma.entity.Transaction;
import com.acikartirma.acikartirma.entity.User;
import com.acikartirma.acikartirma.enums.TransactionType;
import com.acikartirma.acikartirma.facade.ProductFacadeProxy;
import com.acikartirma.acikartirma.facadelocal.CategoryFacadeLocal;
import com.acikartirma.acikartirma.facadelocal.NotificationFacadeLocal;
import com.acikartirma.acikartirma.facadelocal.ProductFacadeLocal;
import com.acikartirma.acikartirma.facadelocal.TransactionFacadeLocal;
import com.acikartirma.acikartirma.facadelocal.UserFacadeLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class ProductBean implements Serializable {

    @EJB
    private ProductFacadeLocal productFacade;

    @EJB
    private CategoryFacadeLocal categoryFacade;

    @EJB
    private UserFacadeLocal userFacade;

    @EJB
    private TransactionFacadeLocal transactionFacade;

    @EJB
    private NotificationFacadeLocal notificationFacade;

    @Inject
    private UserBean userBean;

    private ProductFacadeLocal productProxy;

    private List<Product> allProducts;
    private List<Category> categories;
    private Long selectedCategoryId;
    private Long filterCategoryId;

    private String name;
    private String description;
    private BigDecimal startingPrice;
    private Integer durationInMinutes;
    private String imagePath;

    public ProductBean() {
    }

    @PostConstruct
    public void init() {
        productProxy = new ProductFacadeProxy(productFacade);
        refreshProductList();
        categories = categoryFacade.findAll();
    }

    public void refreshProductList() {
        allProducts = productProxy.findAll().stream()
                .sorted(Comparator.comparing(Product::getId).reversed())
                .collect(Collectors.toList());
    }

    public void filterProducts() {
        List<Product> baseList = productProxy.findAll();
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
        productProxy.create(newProduct);

        name = null;
        description = null;
        startingPrice = null;
        durationInMinutes = null;
        imagePath = null;
        selectedCategoryId = null;

        refreshProductList();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Başarılı", "Ürün ihaleye çıkarıldı."));
    }

    public void placeBid(Product product) {
        User currentUser = userBean.getCurrentUser();
        BigDecimal bidAmount = product.getNewBidAmount();

        if (bidAmount == null || bidAmount.compareTo(product.getCurrentPrice()) <= 0) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hata", "Teklif mevcut fiyattan yüksek olmalıdır."));
            return;
        }

        if (currentUser.getBalance().compareTo(bidAmount) < 0) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hata", "Yetersiz bakiye."));
            return;
        }

        if (product.getWinner() != null) {
            User oldWinner = product.getWinner();
            oldWinner.setBalance(oldWinner.getBalance().add(product.getCurrentPrice()));
            userFacade.update(oldWinner);

            Transaction refund = new Transaction();
            refund.setUser(oldWinner);
            refund.setAmount(product.getCurrentPrice());
            refund.setType(TransactionType.BID_REFUND);
            transactionFacade.create(refund);

            Notification refundNotif = new Notification();
            refundNotif.setUser(oldWinner);
            refundNotif.setMessage("'" + product.getName() + "' ürünündeki teklifiniz geçildi. Bakiyeniz iade edildi.");
            notificationFacade.create(refundNotif);
        }

        currentUser.setBalance(currentUser.getBalance().subtract(bidAmount));
        userFacade.update(currentUser);
        userBean.setCurrentUser(userFacade.find(currentUser.getId()));

        Transaction deduction = new Transaction();
        deduction.setUser(currentUser);
        deduction.setAmount(bidAmount);
        deduction.setType(TransactionType.BID_DEDUCTION);
        transactionFacade.create(deduction);

        Notification sellerNotif = new Notification();
        sellerNotif.setUser(product.getSeller());
        sellerNotif.setMessage("Ürününüz '" + product.getName() + "' için " + bidAmount + " TL değerinde yeni bir teklif var!");
        notificationFacade.create(sellerNotif);

        product.setCurrentPrice(bidAmount);
        product.setWinner(currentUser);
        productProxy.update(product);

        product.setNewBidAmount(null);
        filterProducts();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Başarılı", "Teklifiniz alındı."));
    }

    public void deleteProduct(Product product) {
        productProxy.remove(product);
        filterProducts();
    }

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