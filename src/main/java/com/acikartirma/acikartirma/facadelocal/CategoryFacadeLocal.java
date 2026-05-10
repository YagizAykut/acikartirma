package com.acikartirma.acikartirma.facadelocal;

import com.acikartirma.acikartirma.entity.Category;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface CategoryFacadeLocal {
    void create(Category category);
    void update(Category category);
    void remove(Category category);
    Category find(Object id);
    List<Category> findAll();
    List<Category> findRange(int[] range);
    int count();
}