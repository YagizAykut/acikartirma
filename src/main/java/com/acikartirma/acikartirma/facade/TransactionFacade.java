package com.acikartirma.acikartirma.facade;

import com.acikartirma.acikartirma.entity.Transaction;
import com.acikartirma.acikartirma.facadelocal.TransactionFacadeLocal;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class TransactionFacade extends AbstractFacade<Transaction> implements TransactionFacadeLocal {

    @PersistenceContext
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TransactionFacade() {
        super(Transaction.class);
    }

    // Transaction'a özel metotlar burada kalmaya devam ediyor
    @Override
    public List<Transaction> findTransactionsByUser(Long userId) {
        return em.createQuery("SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.transactionDate DESC", Transaction.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}