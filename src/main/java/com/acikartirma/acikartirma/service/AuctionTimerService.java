package com.acikartirma.acikartirma.service;

import com.acikartirma.acikartirma.entity.Bid;
import com.acikartirma.acikartirma.entity.Product;
import com.acikartirma.acikartirma.entity.Transaction;
import com.acikartirma.acikartirma.entity.User;
import com.acikartirma.acikartirma.enums.ProductStatus;
import com.acikartirma.acikartirma.enums.TransactionType;
import com.acikartirma.acikartirma.facadelocal.BidFacadeLocal;
import com.acikartirma.acikartirma.facadelocal.ProductFacadeLocal;
import com.acikartirma.acikartirma.facadelocal.TransactionFacadeLocal;
import com.acikartirma.acikartirma.facadelocal.UserFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.time.LocalDateTime;
import java.util.List;

@Singleton
@Startup
public class AuctionTimerService {

    @EJB
    private ProductFacadeLocal productFacade;

    @EJB
    private BidFacadeLocal bidFacade;

    @EJB
    private UserFacadeLocal userFacade;

    @EJB
    private TransactionFacadeLocal transactionFacade;

    // Her 10 saniyede bir arka planda çalışır
    @Schedule(hour = "*", minute = "*", second = "*/10", persistent = false)
    public void checkExpiredAuctions() {

        List<Product> expiredProducts = productFacade.findExpiredActiveProducts(LocalDateTime.now());
        boolean hasChanges = false;

        if (expiredProducts != null && !expiredProducts.isEmpty()) {
            for (Product p : expiredProducts) {
                List<Bid> bids = bidFacade.findBidsByProduct(p.getId());

                if (bids != null && !bids.isEmpty()) {
                    // Teklif varsa ürünü SATILDI yap ve parayı satıcıya aktar
                    Bid winningBid = bids.get(0);
                    p.setStatus(ProductStatus.SOLD);
                    p.setWinner(winningBid.getBidder());

                    User seller = p.getSeller();
                    seller.setBalance(seller.getBalance().add(winningBid.getAmount()));
                    userFacade.update(seller);

                    Transaction tx = new Transaction();
                    tx.setAmount(winningBid.getAmount());
                    tx.setType(TransactionType.DEPOSIT);
                    tx.setUser(seller);
                    transactionFacade.create(tx);

                } else {
                    // Teklif yoksa ürünü SÜRESİ DOLDU yap
                    p.setStatus(ProductStatus.EXPIRED);
                }

                productFacade.update(p);
                hasChanges = true;
            }
        }

        // EĞER BİR ÜRÜNÜN SÜRESİ BİTTİYSE TÜM EKRANLARA "YENİLE" SİNYALİ GÖNDER
        if (hasChanges) {
            AuctionWebSocket.broadcast("RELOAD_PAGE");
        }
    }
}