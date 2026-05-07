package com.acikartirma.acikartirma.facade;

import com.acikartirma.acikartirma.entity.Product;
import com.acikartirma.acikartirma.facadelocal.ProductFacadeLocal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

public class ProductFacadeProxy implements ProductFacadeLocal {

    private final ProductFacadeLocal realService;
    private static final Logger logger = Logger.getLogger(ProductFacadeProxy.class.getName());

    public ProductFacadeProxy(ProductFacadeLocal realService) {
        this.realService = realService;
    }

    @Override
    public void create(Product entity) {
        logger.info("PROXY: Yeni bir ürün ekleniyor: " + entity.getName());
        // Burada ek güvenlik kontrolleri yapılabilir
        realService.create(entity);
    }

    @Override
    public void update(Product entity) {
        logger.info("PROXY: Ürün güncelleniyor: " + entity.getName());
        realService.update(entity);
    }

    @Override
    public void remove(Product entity) {
        logger.warning("PROXY: KRİTİK İŞLEM! Ürün siliniyor: " + entity.getName());
        realService.remove(entity);
    }



    @Override
    public List<Product> findAll() {
        logger.info("PROXY: Tüm ürünler listeleniyor...");
        return realService.findAll();
    }

    @Override
    public List<Product> findExpiredActiveProducts(LocalDateTime currentTime) {
        return realService.findExpiredActiveProducts(currentTime);
    }
}