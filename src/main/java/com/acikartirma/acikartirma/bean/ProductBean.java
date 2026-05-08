package com.acikartirma.acikartirma.bean;

import com.acikartirma.acikartirma.entity.Bid;
import com.acikartirma.acikartirma.entity.Product;
import com.acikartirma.acikartirma.entity.Transaction;
import com.acikartirma.acikartirma.enums.TransactionType;
import com.acikartirma.acikartirma.entity.User;
import com.acikartirma.acikartirma.facade.ProductFacadeProxy;
import com.acikartirma.acikartirma.service.AuctionWebSocket;
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
import java.util.List;
import com.acikartirma.acikartirma.facadelocal.ProductFacadeLocal;
import com.acikartirma.acikartirma.facadelocal.BidFacadeLocal;
import com.acikartirma.acikartirma.facadelocal.UserFacadeLocal;
import com.acikartirma.acikartirma.facadelocal.TransactionFacadeLocal;

@Named
@ViewScoped
public class ProductBean implements Serializable {

    @EJB
    private ProductFacadeLocal productFacade;

    @EJB
    private BidFacadeLocal bidFacade;

    @EJB
    private UserFacadeLocal userFacade;

    @EJB
    private TransactionFacadeLocal transactionFacade;

    @Inject
    private UserBean userBean;


    private ProductFacadeLocal productProxy;

    private String name;
    private String description;
    private BigDecimal startingPrice;
    private String imagePath;
    private int durationInMinutes;

    private List<Product> productList;

    @PostConstruct
    public void init() {

        productProxy = new ProductFacadeProxy(productFacade);
        refreshProductList();
    }

    private void refreshProductList() {

        productList = productProxy.findAll();
        if (productList != null) {

            productList.sort((p1, p2) -> p2.getId().compareTo(p1.getId()));
        }
    }

    public List<Product> getAllProducts() {
        return productList;
    }

    public String saveProduct() {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setStartingPrice(startingPrice);
        p.setCurrentPrice(startingPrice);

        LocalDateTime now = LocalDateTime.now();
        p.setStartTime(now);
        p.setEndTime(now.plusMinutes(durationInMinutes));
        p.setImagePath(imagePath);
        p.setSeller(userBean.getCurrentUser());


        productProxy.create(p);

        return "index?faces-redirect=true";
    }

    public void deleteProduct(Product product) {
        if (!product.getSeller().getUsername().equals(userBean.getCurrentUser().getUsername())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hata: Sadece kendi eklediğiniz ürünleri silebilirsiniz!", null));
            return;
        }

        List<Bid> bids = bidFacade.findBidsByProduct(product.getId());
        if (bids != null && !bids.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "İşlem Reddedildi: Bu ürüne teklif verildiği için ihale iptal edilemez!", null));
            return;
        }

        try {

            productProxy.remove(product);
            refreshProductList();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Başarılı: İhale iptal edildi ve ürün silindi.", null));
        } catch (Exception e) {
            String errorDetail = e.getMessage() != null ? e.getMessage() : e.toString();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hata: Ürün silinemedi! Detay: " + errorDetail, null));
            e.printStackTrace();
        }
    }

    public void placeBid(Product product) {
        BigDecimal newBid = product.getNewBidAmount();
        if (newBid == null) return;

        if (LocalDateTime.now().isAfter(product.getEndTime())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hata: Süre Doldu! Bu açık artırma sona ermiştir.", null));
            return;
        }

        try {
            if (newBid.compareTo(product.getCurrentPrice()) > 0) {
                User currentUser = userFacade.findByUsername(userBean.getCurrentUser().getUsername());
                List<Bid> pastBids = bidFacade.findBidsByProduct(product.getId());

                User previousBidder = null;
                BigDecimal refundAmount = BigDecimal.ZERO;
                boolean isOwnBid = false;

                if (pastBids != null && !pastBids.isEmpty()) {
                    Bid lastBid = pastBids.get(0);
                    previousBidder = lastBid.getBidder();
                    refundAmount = lastBid.getAmount();

                    if (previousBidder.getId().equals(currentUser.getId())) {
                        isOwnBid = true;
                    }
                }

                BigDecimal availablePower = currentUser.getBalance();
                if (isOwnBid) {
                    availablePower = availablePower.add(refundAmount);
                }

                if (availablePower.compareTo(newBid) < 0) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hata: Yetersiz Bakiye!", null));
                    return;
                }

                if (pastBids != null && !pastBids.isEmpty()) {
                    if (isOwnBid) {
                        currentUser.setBalance(currentUser.getBalance().add(refundAmount));
                        Transaction refundTx = new Transaction();
                        refundTx.setAmount(refundAmount);
                        refundTx.setType(TransactionType.BID_REFUND);
                        refundTx.setUser(currentUser);
                        transactionFacade.create(refundTx);
                    } else {
                        previousBidder.setBalance(previousBidder.getBalance().add(refundAmount));
                        userFacade.update(previousBidder);
                        Transaction refundTx = new Transaction();
                        refundTx.setAmount(refundAmount);
                        refundTx.setType(TransactionType.BID_REFUND);
                        refundTx.setUser(previousBidder);
                        transactionFacade.create(refundTx);
                    }
                }

                currentUser.setBalance(currentUser.getBalance().subtract(newBid));
                userFacade.update(currentUser);
                userBean.getCurrentUser().setBalance(currentUser.getBalance());

                Transaction deductTx = new Transaction();
                deductTx.setAmount(newBid);
                deductTx.setType(TransactionType.BID_DEDUCTION);
                deductTx.setUser(currentUser);
                transactionFacade.create(deductTx);

                product.setCurrentPrice(newBid);


                productProxy.update(product);

                Bid bidLog = new Bid();
                bidLog.setAmount(newBid);
                bidLog.setBidder(currentUser);
                bidLog.setProduct(product);
                bidFacade.create(bidLog);

                AuctionWebSocket.broadcast("💰 Yeni Teklif! " + product.getName() + " için " + newBid + " TL teklif verildi!");
            }
        } catch (Exception e) {
            System.out.println("Teklif verilirken bir hata oluştu: " + e.getMessage());
        }
    }


    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getStartingPrice() { return startingPrice; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public int getDurationInMinutes() { return durationInMinutes; }
    public void setDurationInMinutes(int durationInMinutes) { this.durationInMinutes = durationInMinutes; }
}