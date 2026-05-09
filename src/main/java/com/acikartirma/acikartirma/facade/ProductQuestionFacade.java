package com.acikartirma.acikartirma.facade;

import com.acikartirma.acikartirma.entity.ProductQuestion;
import com.acikartirma.acikartirma.facadelocal.ProductQuestionFacadeLocal;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class ProductQuestionFacade extends AbstractFacade<ProductQuestion> implements ProductQuestionFacadeLocal {

    @PersistenceContext
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProductQuestionFacade() {
        super(ProductQuestion.class);
    }

    @Override
    public List<ProductQuestion> findQuestionsByProduct(Long productId) {
        return em.createQuery("SELECT pq FROM ProductQuestion pq WHERE pq.product.id = :productId ORDER BY pq.createdAt DESC", ProductQuestion.class)
                .setParameter("productId", productId)
                .getResultList();
    }
}