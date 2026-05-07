package com.acikartirma.acikartirma.facadelocal;

import com.acikartirma.acikartirma.entity.Transaction;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface TransactionFacadeLocal {
    void create(Transaction transaction);
    List<Transaction> findTransactionsByUser(Long userId);
}