package com.acikartirma.acikartirma.facade;

import com.acikartirma.acikartirma.entity.Bid;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import com.acikartirma.acikartirma.facadelocal.BidFacadeLocal;

@Stateless
public class BidFacade implements BidFacadeLocal {


    @PersistenceContext(unitName = "default")
    private EntityManager em;


    public void create(Bid bid) {
        em.persist(bid);
    }


    public void update(Bid bid) {
        em.merge(bid);
    }


    public void remove(Bid bid) {
        em.remove(em.merge(bid));
    }


    public List<Bid> findAll() {
        return em.createQuery("SELECT b FROM Bid b ORDER BY b.bidTime DESC", Bid.class).getResultList();
    }


    public List<Bid> findBidsByProduct(Long productId) {
        return em.createQuery("SELECT b FROM Bid b WHERE b.product.id = :productId ORDER BY b.bidTime DESC", Bid.class)
                .setParameter("productId", productId)
                .getResultList();
    }


    public List<Bid> findBidsByUser(Long userId) {
        return em.createQuery("SELECT b FROM Bid b WHERE b.bidder.id = :userId ORDER BY b.bidTime DESC", Bid.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}