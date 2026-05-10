package com.acikartirma.acikartirma.facade;

import com.acikartirma.acikartirma.entity.Product;
import com.acikartirma.acikartirma.facadelocal.ProductFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
@SuppressWarnings("unused")
public class ProductFacadeProxy implements ProductFacadeLocal {

    @EJB(beanName = "ProductFacade")
    private ProductFacadeLocal realFacade;

    @Override
    public void create(Product product) {
        System.out.println("[PROXY LOG] Yeni ürün ihaleye çıkarılıyor: " + product.getName());
        realFacade.create(product);
    }

    @Override
    public void update(Product product) {
        System.out.println("[PROXY LOG] Ürün veritabanında güncelleniyor: " + product.getName());
        realFacade.update(product);
    }

    @Override
    public void remove(Product product) {
        System.out.println("[PROXY LOG] Ürün sistemden siliniyor: " + product.getName());
        realFacade.remove(product);
    }

    @Override
    public Product find(Object id) {
        return realFacade.find(id);
    }

    @Override
    public List<Product> findAll() {
        System.out.println("[PROXY LOG] Tüm ürünler listelenmek üzere çekiliyor...");
        return realFacade.findAll();
    }

    @Override
    public List<Product> findExpiredActiveProducts(LocalDateTime now) {
        return realFacade.findExpiredActiveProducts(now);
    }

    // ARAYÜZDE OLUP BURADA EKSİK OLAN METOT EKLENDİ
    @Override
    public List<Product> findBySeller(Long sellerId) {
        return realFacade.findBySeller(sellerId);
    }
}