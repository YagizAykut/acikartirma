package com.acikartirma.acikartirma.entity;

public enum ProductStatus {
    ACTIVE,    // Açık artırma devam ediyor
    SOLD,      // Süre bitti ve biri satın aldı
    EXPIRED,   // Süre bitti ama kimse teklif vermedi
    CANCELED   // Satıcı ürünü iptal etti
}