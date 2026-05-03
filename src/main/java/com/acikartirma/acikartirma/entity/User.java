package com.acikartirma.acikartirma.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users") // PostgreSQL'de çakışmayı önlemek için "users" kullanıyoruz
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    // E-mail kişiye özel (unique) ve boş bırakılamaz
    @Column(nullable = false, unique = true)
    private String email;

    // Telefon numarası kişiye özel (unique) ve boş bırakılamaz
    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    // Yeni kayıt olan kullanıcının bakiyesi varsayılan olarak 0'dır
    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    // Sadece kayıt olurken yazılır, sonradan güncellenemez (updatable = false)
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public User() {
    }

    // @PrePersist: Bu nesne veritabanına "INSERT" edilmeden hemen saniyeler önce otomatik çalışır
    // Kayıt tarihini bizim yerimize sisteme otomatik yazdırır.
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- GETTER VE SETTER ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}