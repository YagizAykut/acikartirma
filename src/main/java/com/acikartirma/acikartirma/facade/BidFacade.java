package com.acikartirma.acikartirma.facade;

import com.acikartirma.acikartirma.entity.Bid;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class BidFacade {

    // Veritabanı işlemlerini yöneten ana nesnemiz
    @PersistenceContext(unitName = "default")
    private EntityManager em;

    // Yeni bir teklifi veritabanına kaydeder
    public void create(Bid bid) {
        em.persist(bid);
    }

    // Bir teklifi günceller
    public void update(Bid bid) {
        em.merge(bid);
    }

    // Bir teklifi siler
    public void remove(Bid bid) {
        em.remove(em.merge(bid));
    }

    // Sistemdeki tüm teklifleri getirir (Genel loglama için)
    public List<Bid> findAll() {
        return em.createQuery("SELECT b FROM Bid b ORDER BY b.bidTime DESC", Bid.class).getResultList();
    }

    // SADECE BELİRLİ BİR ÜRÜNE YAPILAN TEKLİFLERİ GETİRİR (En yeni teklif en üstte)
    public List<Bid> findBidsByProduct(Long productId) {
        return em.createQuery("SELECT b FROM Bid b WHERE b.product.id = :productId ORDER BY b.bidTime DESC", Bid.class)
                .setParameter("productId", productId)
                .getResultList();
    }

    // BELİRLİ BİR KULLANICININ YAPTIĞI TEKLİFLERİ GETİRİR (Kullanıcı profili için)
    public List<Bid> findBidsByUser(Long userId) {
        return em.createQuery("SELECT b FROM Bid b WHERE b.bidder.id = :userId ORDER BY b.bidTime DESC", Bid.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}