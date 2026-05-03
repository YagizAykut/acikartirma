package com.acikartirma.acikartirma.entity;

public enum TransactionType {
    DEPOSIT,         // Cüzdana para yükleme
    BID_DEDUCTION,   // Teklif verildiği için yapılan kesinti
    BID_REFUND,      // Daha yüksek teklif geldiği için yapılan iade
    SALE_REVENUE     // Ürün satıldığı için satıcıya yatan para
}