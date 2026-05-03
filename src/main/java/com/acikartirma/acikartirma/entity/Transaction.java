package com.acikartirma.acikartirma.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // İşlem miktarı
    @Column(nullable = false)
    private BigDecimal amount;

    // İşlemin türü (Yükleme mi, kesinti mi, iade mi?)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    // İşlemin yapıldığı tam tarih ve saat
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    // Bu işlemi HANGİ kullanıcı yaptı?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Transaction() {
    }

    // @PrePersist: Veritabanına kaydedilmeden hemen önce saniyeyi otomatik atar
    @PrePersist
    protected void onCreate() {
        this.transactionDate = LocalDateTime.now();
    }

    // --- GETTER VE SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}