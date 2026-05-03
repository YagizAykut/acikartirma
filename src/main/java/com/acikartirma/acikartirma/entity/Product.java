package com.acikartirma.acikartirma.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    // Satıcının istediği ilk fiyat
    @Column(name = "starting_price", nullable = false)
    private BigDecimal startingPrice;

    // Gelen tekliflerle artacak olan anlık fiyat
    @Column(name = "current_price")
    private BigDecimal currentPrice;

    // Ne zaman başlayacak?
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    // Ne zaman bitecek?
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // Ürünün fotoğraf linki
    @Column(name = "image_path")
    private String imagePath;

    // Ürünün anlık durumu (Varsayılan olarak ACTIVE başlar)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    // Açık artırmayı kazanan kişi (Başlangıçta boştur)
    @ManyToOne
    @JoinColumn(name = "winner_id")
    private User winner;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    @Version
    private Long version;

    @Transient
    private BigDecimal newBidAmount;

    public Product() {
    }

    // --- GETTER VE SETTER ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getStartingPrice() { return startingPrice; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }

    public User getWinner() { return winner; }
    public void setWinner(User winner) { this.winner = winner; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public Long getVersion() { return version; }

    public BigDecimal getNewBidAmount() { return newBidAmount; }
    public void setNewBidAmount(BigDecimal newBidAmount) { this.newBidAmount = newBidAmount; }
}