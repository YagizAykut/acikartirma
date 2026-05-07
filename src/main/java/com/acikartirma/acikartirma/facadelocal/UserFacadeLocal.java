package com.acikartirma.acikartirma.facadelocal;

import com.acikartirma.acikartirma.entity.User;
import jakarta.ejb.Local;

@Local
public interface UserFacadeLocal {
    void create(User user);
    void update(User user);
    User findByUsername(String username);
}