package com.acikartirma.acikartirma.facadelocal;

import com.acikartirma.acikartirma.entity.Product;
import jakarta.ejb.Local;
import java.time.LocalDateTime;
import java.util.List;

@Local
public interface ProductFacadeLocal {
    void create(Product product);
    void update(Product product);
    void remove(Product product);
    Product find(Object id);
    List<Product> findAll();

    // EKLENEN YENİ METOTLARIN İMZALARI BURADA
    List<Product> findBySeller(Long sellerId);
    List<Product> findExpiredActiveProducts(LocalDateTime now);
}