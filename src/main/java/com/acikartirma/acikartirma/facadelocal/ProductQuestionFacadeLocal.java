package com.acikartirma.acikartirma.facadelocal;

import com.acikartirma.acikartirma.entity.ProductQuestion;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface ProductQuestionFacadeLocal {
    void create(ProductQuestion productQuestion);
    void update(ProductQuestion productQuestion);
    void remove(ProductQuestion productQuestion);
    ProductQuestion find(Object id);
    List<ProductQuestion> findAll();
    List<ProductQuestion> findQuestionsByProduct(Long productId);
}