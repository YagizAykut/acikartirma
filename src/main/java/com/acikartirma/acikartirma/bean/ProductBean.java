package com.acikartirma.acikartirma.bean;

import com.acikartirma.acikartirma.entity.Bid;
import com.acikartirma.acikartirma.entity.Product;
import com.acikartirma.acikartirma.entity.Transaction;
import com.acikartirma.acikartirma.entity.TransactionType;
import com.acikartirma.acikartirma.entity.User;
import com.acikartirma.acikartirma.facade.BidFacade;
import com.acikartirma.acikartirma.facade.ProductFacade;
import com.acikartirma.acikartirma.facade.TransactionFacade;
import com.acikartirma.acikartirma.facade.UserFacade;
import com.acikartirma.acikartirma.websocket.AuctionWebSocket;
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

@Named
@ViewScoped
public class ProductBean implements Serializable {

    @EJB
    private ProductFacade productFacade;

    @EJB
    private BidFacade bidFacade;

    @EJB
    private UserFacade userFacade;

    @EJB
    private TransactionFacade transactionFacade; // FİNANSAL LOGLAMA İÇİN EKLENDİ

    @Inject
    private UserBean userBean;

    private String name;
    private String description;
    private BigDecimal startingPrice;
    private String imagePath;
    private int durationInMinutes;

    private List<Product> productList;

    @PostConstruct
    public void init() {
        productList = productFacade.findAll();
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

        productFacade.create(p);
        return "index?faces-redirect=true";
    }

    public void placeBid(Product product) {
        BigDecimal newBid = product.getNewBidAmount();
        User currentUser = userBean.getCurrentUser();

        if (newBid == null) return;

        if (LocalDateTime.now().isAfter(product.getEndTime())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hata: Süre Doldu! Bu açık artırma sona ermiştir.", null));
            return;
        }

        try {
            if (newBid.compareTo(product.getCurrentPrice()) > 0) {
                if (currentUser.getBalance().compareTo(newBid) < 0) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hata: Yetersiz Bakiye!", null));
                    return;
                }

                // İADE MANTIĞI VE LOGU
                List<Bid> pastBids = bidFacade.findBidsByProduct(product.getId());
                if (pastBids != null && !pastBids.isEmpty()) {
                    Bid lastBid = pastBids.get(0);
                    User previousBidder = lastBid.getBidder();
                    BigDecimal refundAmount = lastBid.getAmount();

                    previousBidder.setBalance(previousBidder.getBalance().add(refundAmount));
                    userFacade.update(previousBidder);

                    // --- İŞLEMİ LOGLA (BID_REFUND) ---
                    Transaction refundTx = new Transaction();
                    refundTx.setAmount(refundAmount);
                    refundTx.setType(TransactionType.BID_REFUND);
                    refundTx.setUser(previousBidder);
                    transactionFacade.create(refundTx);
                }

                // KESİNTİ MANTIĞI VE LOGU
                currentUser.setBalance(currentUser.getBalance().subtract(newBid));
                userFacade.update(currentUser);

                // --- İŞLEMİ LOGLA (BID_DEDUCTION) ---
                Transaction deductTx = new Transaction();
                deductTx.setAmount(newBid);
                deductTx.setType(TransactionType.BID_DEDUCTION);
                deductTx.setUser(currentUser);
                transactionFacade.create(deductTx);

                product.setCurrentPrice(newBid);
                productFacade.update(product);

                Bid bidLog = new Bid();
                bidLog.setAmount(newBid);
                bidLog.setBidder(currentUser);
                bidLog.setProduct(product);
                bidFacade.create(bidLog);

                AuctionWebSocket.broadcast("💰 Yeni Teklif! " + product.getName() + " için " + newBid + " TL teklif verildi!");
            }
        } catch (jakarta.persistence.OptimisticLockException e) {
            System.out.println("Çakışma tespit edildi!");
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