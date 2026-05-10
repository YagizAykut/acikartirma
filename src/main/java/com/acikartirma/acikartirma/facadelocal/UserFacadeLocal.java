package com.acikartirma.acikartirma.facadelocal;

import com.acikartirma.acikartirma.entity.User;
import jakarta.ejb.Local;
import java.util.List;

@Local
@SuppressWarnings("unused")
public interface UserFacadeLocal {
    void create(User user);
    void update(User user);
    void remove(User user);
    User find(Object id);
    List<User> findAll();
    User findByUsername(String username);
}