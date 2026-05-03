package com.acikartirma.acikartirma.service;

import com.acikartirma.acikartirma.entity.Bid;
import com.acikartirma.acikartirma.entity.Product;
import com.acikartirma.acikartirma.entity.ProductStatus;
import com.acikartirma.acikartirma.entity.Transaction;
import com.acikartirma.acikartirma.entity.TransactionType;
import com.acikartirma.acikartirma.entity.User;
import com.acikartirma.acikartirma.facade.BidFacade;
import com.acikartirma.acikartirma.facade.ProductFacade;
import com.acikartirma.acikartirma.facade.TransactionFacade;
import com.acikartirma.acikartirma.facade.UserFacade;
import com.acikartirma.acikartirma.websocket.AuctionWebSocket;
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
    private ProductFacade productFacade;

    @EJB
    private BidFacade bidFacade;

    @EJB
    private UserFacade userFacade;

    @EJB
    private TransactionFacade transactionFacade; // FİNANSAL LOGLAMA İÇİN EKLENDİ

    @Schedule(hour = "*", minute = "*", second = "*/30", persistent = false)
    public void checkExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Product> expiredProducts = productFacade.findExpiredActiveProducts(now);

        for (Product product : expiredProducts) {

            List<Bid> productBids = bidFacade.findBidsByProduct(product.getId());

            if (productBids != null && !productBids.isEmpty()) {
                Bid winningBid = productBids.get(0);
                User winner = winningBid.getBidder();
                User seller = product.getSeller();

                product.setWinner(winner);
                product.setStatus(ProductStatus.SOLD);

                seller.setBalance(seller.getBalance().add(winningBid.getAmount()));
                userFacade.update(seller);

                // --- İŞLEMİ LOGLA (SALE_REVENUE) ---
                Transaction saleTx = new Transaction();
                saleTx.setAmount(winningBid.getAmount());
                saleTx.setType(TransactionType.SALE_REVENUE);
                saleTx.setUser(seller);
                transactionFacade.create(saleTx);
                // -----------------------------------

                System.out.println("İHALE SONUÇLANDI: " + product.getName() + " ürününü " + winner.getUsername() + " kazandı!");
                AuctionWebSocket.broadcast("🏆 İhale Bitti! " + product.getName() + " ürününü " + winner.getUsername() + " kazandı.");

            } else {
                product.setStatus(ProductStatus.EXPIRED);
                System.out.println("İHALE SÜRESİ DOLDU: " + product.getName() + " için teklif veren olmadı.");
            }

            productFacade.update(product);
        }
    }
}