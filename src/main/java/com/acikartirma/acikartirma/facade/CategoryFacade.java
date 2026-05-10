package com.acikartirma.acikartirma.facade;

import com.acikartirma.acikartirma.entity.Category;
import com.acikartirma.acikartirma.facadelocal.CategoryFacadeLocal;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
@SuppressWarnings("unused")
public class CategoryFacade extends AbstractFacade<Category> implements CategoryFacadeLocal {

    @PersistenceContext
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CategoryFacade() {
        super(Category.class);
    }

    @Override
    public void update(Category category) {
        this.edit(category);
    }
}