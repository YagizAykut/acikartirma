package com.acikartirma.acikartirma.facade;

import com.acikartirma.acikartirma.entity.Product;
import com.acikartirma.acikartirma.entity.ProductStatus;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class ProductFacade {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    public void create(Product product) {
        em.persist(product);
    }

    public void update(Product product) {
        em.merge(product);
    }

    public void remove(Product product) {
        em.remove(em.merge(product));
    }

    public List<Product> findAll() {
        return em.createQuery("SELECT p FROM Product p ORDER BY p.id DESC", Product.class).getResultList();
    }

    // YENİ EKLENEN METOT: Zamanlayıcı bot için süresi dolmuş ama hala ACTIVE olan ürünleri bulur
    public List<Product> findExpiredActiveProducts(LocalDateTime now) {
        return em.createQuery("SELECT p FROM Product p WHERE p.status = :status AND p.endTime < :now", Product.class)
                .setParameter("status", ProductStatus.ACTIVE)
                .setParameter("now", now)
                .getResultList();
    }
}