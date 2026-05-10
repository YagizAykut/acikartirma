package com.acikartirma.acikartirma.facadelocal;

import com.acikartirma.acikartirma.entity.Transaction;
import jakarta.ejb.Local;
import java.util.List;

@Local
@SuppressWarnings("unused")
public interface TransactionFacadeLocal {
    void create(Transaction transaction);

    void update(Transaction transaction);

    void remove(Transaction transaction);
    Transaction find(Object id);
    List<Transaction> findAll();
    List<Transaction> findRange(int[] range);
    int count();
    List<Transaction> findTransactionsByUser(Long userId);
}