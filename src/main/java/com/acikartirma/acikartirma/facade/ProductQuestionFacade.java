package com.acikartirma.acikartirma.facade;

import com.acikartirma.acikartirma.entity.ProductQuestion;
import com.acikartirma.acikartirma.facadelocal.ProductQuestionFacadeLocal;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
@SuppressWarnings("unused")
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
    public void update(ProductQuestion productQuestion) {
        this.edit(productQuestion);
    }

    @Override
    public List<ProductQuestion> findQuestionsByProduct(Long productId) {
        return em.createQuery("SELECT q FROM ProductQuestion q WHERE q.product.id = :productId ORDER BY q.createdAt DESC", ProductQuestion.class)
                .setParameter("productId", productId)
                .getResultList();
    }

    @Override
    public List<ProductQuestion> findQuestionsByUser(Long userId) {
        return em.createQuery("SELECT q FROM ProductQuestion q WHERE q.user.id = :userId ORDER BY q.createdAt DESC", ProductQuestion.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}