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
    public Product find(Object id) {
        logger.info("PROXY: Ürün aranıyor. ID: " + id);
        return realService.find(id);
    }

    @Override
    public List<Product> findAll() {
        logger.info("PROXY: Tüm ürünler listeleniyor...");
        return realService.findAll();
    }

    @Override
    public List<Product> findBySeller(Long sellerId) {
        logger.info("PROXY: Satıcının ürünleri listeleniyor. Satıcı ID: " + sellerId);
        return realService.findBySeller(sellerId);
    }

    @Override
    public List<Product> findExpiredActiveProducts(LocalDateTime currentTime) {
        logger.info("PROXY: Süresi dolan ihaleler kontrol ediliyor...");
        return realService.findExpiredActiveProducts(currentTime);
    }
}