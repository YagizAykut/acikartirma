package com.acikartirma.acikartirma.facade;

import com.acikartirma.acikartirma.entity.Transaction;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class TransactionFacade {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    // Yeni bir finansal işlemi veritabanına kaydeder
    public void create(Transaction transaction) {
        em.persist(transaction);
    }

    // Belirli bir kullanıcının tüm finansal geçmişini getirir (En yenisi en üstte)
    public List<Transaction> findTransactionsByUser(Long userId) {
        return em.createQuery("SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.transactionDate DESC", Transaction.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}