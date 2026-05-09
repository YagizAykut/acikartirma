package com.acikartirma.acikartirma.service;

import com.acikartirma.acikartirma.entity.Notification;
import com.acikartirma.acikartirma.entity.Product;
import com.acikartirma.acikartirma.entity.Transaction;
import com.acikartirma.acikartirma.enums.ProductStatus;
import com.acikartirma.acikartirma.enums.TransactionType;
import com.acikartirma.acikartirma.facadelocal.NotificationFacadeLocal;
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
    private UserFacadeLocal userFacade;

    @EJB
    private TransactionFacadeLocal transactionFacade;

    @EJB
    private NotificationFacadeLocal notificationFacade;

    @Schedule(hour = "*", minute = "*", second = "*/10", persistent = false)
    public void checkExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Product> expiredProducts = productFacade.findExpiredActiveProducts(now);

        for (Product product : expiredProducts) {
            if (product.getWinner() != null) {
                product.setStatus(ProductStatus.SOLD);

                product.getSeller().setBalance(product.getSeller().getBalance().add(product.getCurrentPrice()));
                userFacade.update(product.getSeller());

                Transaction t = new Transaction();
                t.setUser(product.getSeller());
                t.setAmount(product.getCurrentPrice());
                t.setType(TransactionType.SALE_REVENUE);
                transactionFacade.create(t);

                Notification sellerNotif = new Notification();
                sellerNotif.setUser(product.getSeller());
                sellerNotif.setMessage("Tebrikler! '" + product.getName() + "' adlı ürününüz " + product.getCurrentPrice() + " TL'ye satıldı.");
                notificationFacade.create(sellerNotif);

                Notification winnerNotif = new Notification();
                winnerNotif.setUser(product.getWinner());
                winnerNotif.setMessage("Kazandınız! '" + product.getName() + "' ihalesini " + product.getCurrentPrice() + " TL ile kazandınız.");
                notificationFacade.create(winnerNotif);

            } else {
                product.setStatus(ProductStatus.EXPIRED);

                Notification expireNotif = new Notification();
                expireNotif.setUser(product.getSeller());
                expireNotif.setMessage("Süre Doldu: '" + product.getName() + "' adlı ürününüze alıcı çıkmadı.");
                notificationFacade.create(expireNotif);
            }

            productFacade.update(product);
        }
    }
}