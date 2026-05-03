package com.acikartirma.acikartirma.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids") // pgAdmin'de çoğul isim standartına uyuyoruz
public class Bid implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Teklif miktarı
    @Column(nullable = false)
    private BigDecimal amount;

    // Teklifin yapıldığı anın tam tarihi ve saati
    @Column(name = "bid_time", nullable = false)
    private LocalDateTime bidTime;

    // Teklifi HANGİ kullanıcı verdi? (Çoka-Bir İlişki)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    // Teklif HANGİ ürüne verildi? (Çoka-Bir İlişki)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public Bid() {
    }

    // @PrePersist: Bu nesne veritabanına kaydedilmeden hemen önce
    // zaman damgasını otomatik olarak atar.
    @PrePersist
    protected void onCreate() {
        this.bidTime = LocalDateTime.now();
    }

    // --- GETTER VE SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getBidTime() { return bidTime; }
    public void setBidTime(LocalDateTime bidTime) { this.bidTime = bidTime; }

    public User getBidder() { return bidder; }
    public void setBidder(User bidder) { this.bidder = bidder; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}