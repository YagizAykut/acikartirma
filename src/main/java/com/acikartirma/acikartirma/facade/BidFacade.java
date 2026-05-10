package com.acikartirma.acikartirma.facade;

import com.acikartirma.acikartirma.entity.Bid;
import com.acikartirma.acikartirma.facadelocal.BidFacadeLocal;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
@SuppressWarnings("unused")
public class BidFacade extends AbstractFacade<Bid> implements BidFacadeLocal {

    @PersistenceContext
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public BidFacade() {
        super(Bid.class);
    }

    @Override
    public void update(Bid bid) {
        this.edit(bid);
    }

    // Arayüzdeki isme (findBidsByProduct) göre güncellendi
    @Override
    public List<Bid> findBidsByProduct(Long productId) {
        return em.createQuery("SELECT b FROM Bid b WHERE b.product.id = :productId ORDER BY b.amount DESC", Bid.class)
                .setParameter("productId", productId)
                .getResultList();
    }

    // HATA MESAJINDAKİ ASIL DÜZELTME:
    // Arayüz bu metodun ismini 'findBidsByUser' olarak bekliyor.
    @Override
    public List<Bid> findBidsByUser(Long userId) {
        return em.createQuery("SELECT b FROM Bid b WHERE b.bidder.id = :userId", Bid.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}