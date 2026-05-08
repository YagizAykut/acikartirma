package com.acikartirma.acikartirma.facade;

import com.acikartirma.acikartirma.entity.Bid;
import com.acikartirma.acikartirma.facadelocal.BidFacadeLocal;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
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
    public List<Bid> findBidsByProduct(Long productId) {
        return em.createQuery("SELECT b FROM Bid b WHERE b.product.id = :productId ORDER BY b.amount DESC", Bid.class)
                .setParameter("productId", productId)
                .getResultList();
    }

    @Override
    public List<Bid> findBidsByUser(Long userId) {
        return em.createQuery("SELECT b FROM Bid b WHERE b.bidder.id = :userId ORDER BY b.bidTime DESC", Bid.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}