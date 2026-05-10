package com.acikartirma.acikartirma.facade;

import com.acikartirma.acikartirma.entity.Product;
import com.acikartirma.acikartirma.facadelocal.ProductFacadeLocal;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
@SuppressWarnings("unused")
public class ProductFacade extends AbstractFacade<Product> implements ProductFacadeLocal {

    @PersistenceContext
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProductFacade() {
        super(Product.class);
    }

    @Override
    public void update(Product product) {
        this.edit(product);
    }

    @Override
    public List<Product> findExpiredActiveProducts(LocalDateTime now) {
        return em.createQuery("SELECT p FROM Product p WHERE p.status = com.acikartirma.acikartirma.enums.ProductStatus.ACTIVE AND p.endTime <= :now", Product.class)
                .setParameter("now", now)
                .getResultList();
    }

    @Override
    public List<Product> findBySeller(Long sellerId) {
        return em.createQuery("SELECT p FROM Product p WHERE p.seller.id = :sellerId", Product.class)
                .setParameter("sellerId", sellerId)
                .getResultList();
    }
}