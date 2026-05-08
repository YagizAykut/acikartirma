package com.acikartirma.acikartirma.facade;

import com.acikartirma.acikartirma.entity.Product;
import com.acikartirma.acikartirma.facadelocal.ProductFacadeLocal;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
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
    public List<Product> findExpiredActiveProducts(LocalDateTime currentTime) {

        return em.createQuery("SELECT p FROM Product p WHERE p.status = com.acikartirma.acikartirma.enums.ProductStatus.ACTIVE AND p.endTime <= :currentTime", Product.class)
                .setParameter("currentTime", currentTime)
                .getResultList();
    }
}