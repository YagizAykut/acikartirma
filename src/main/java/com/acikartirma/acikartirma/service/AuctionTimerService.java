package com.acikartirma.acikartirma.service;

import com.acikartirma.acikartirma.entity.Bid;
import com.acikartirma.acikartirma.entity.Product;
import com.acikartirma.acikartirma.enums.ProductStatus;
import com.acikartirma.acikartirma.entity.Transaction;
import com.acikartirma.acikartirma.enums.TransactionType;
import com.acikartirma.acikartirma.entity.User;
import com.acikartirma.acikartirma.websocket.AuctionWebSocket;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.time.LocalDateTime;
import java.util.List;
import com.acikartirma.acikartirma.facadelocal.ProductFacadeLocal;
import com.acikartirma.acikartirma.facadelocal.BidFacadeLocal;
import com.acikartirma.acikartirma.facadelocal.UserFacadeLocal;
import com.acikartirma.acikartirma.facadelocal.TransactionFacadeLocal;


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

    @Schedule(hour = "*", minute = "*", second = "*/30", persistent = false)
    public void checkExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Product> expiredProducts = productFacade.findExpiredActiveProducts(now);

        for (Product product : expiredProducts) {

            List<Bid> productBids = bidFacade.findBidsByProduct(product.getId());

            if (productBids != null && !productBids.isEmpty()) {
                Bid winningBid = productBids.get(0);
                User winner = winningBid.getBidder();


                User seller = userFacade.findByUsername(product.getSeller().getUsername());

                if (seller != null) {
                    product.setWinner(winner);
                    product.setStatus(ProductStatus.SOLD);


                    seller.setBalance(seller.getBalance().add(winningBid.getAmount()));
                    userFacade.update(seller);


                    Transaction saleTx = new Transaction();
                    saleTx.setAmount(winningBid.getAmount());
                    saleTx.setType(TransactionType.SALE_REVENUE);
                    saleTx.setUser(seller);
                    transactionFacade.create(saleTx);


                    System.out.println("İHALE SONUÇLANDI: " + product.getName() + " ürününü " + winner.getUsername() + " kazandı! " + winningBid.getAmount() + " TL Satıcının hesabına yatırıldı.");
                    AuctionWebSocket.broadcast("🏆 İhale Bitti! " + product.getName() + " ürününü " + winner.getUsername() + " kazandı.");
                }

            } else {
                product.setStatus(ProductStatus.EXPIRED);
                System.out.println("İHALE SÜRESİ DOLDU: " + product.getName() + " için teklif veren olmadı.");
            }

            productFacade.update(product);
        }
    }
}