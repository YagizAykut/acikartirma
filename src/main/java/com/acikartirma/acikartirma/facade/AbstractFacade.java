package com.acikartirma.acikartirma.facade;

import jakarta.persistence.EntityManager;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import java.util.List;

public abstract class AbstractFacade<T> {

    private Class<T> entityClass;

    public AbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract EntityManager getEntityManager();

    public void create(T entity) {
        getEntityManager().persist(entity);
    }

    public void update(T entity) {
        getEntityManager().merge(entity);
    }

    public void remove(T entity) {
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    public T find(Object id) {
        return getEntityManager().find(entityClass, id);
    }

    public List<T> findAll() {
        jakarta.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));

        jakarta.persistence.Query query = getEntityManager().createQuery(cq);

        // KRİTİK NOKTA: GlassFish Önbelleğini (Cache) bypass ediyoruz.
        // Her zaman veritabanındaki en taze ve güncel veriyi getirmeye zorluyoruz!
        query.setHint("jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
        query.setHint("jakarta.persistence.cache.storeMode", CacheStoreMode.REFRESH);

        return query.getResultList();
    }
}