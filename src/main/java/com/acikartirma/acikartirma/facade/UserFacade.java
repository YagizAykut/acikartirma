package com.acikartirma.acikartirma.facade;

import com.acikartirma.acikartirma.entity.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import com.acikartirma.acikartirma.facadelocal.UserFacadeLocal;

@Stateless
public class UserFacade implements UserFacadeLocal {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    // Yeni kullanıcı kaydeder
    public void create(User user) {
        em.persist(user);
    }

    // Kullanıcı bilgilerini (örneğin bakiyeyi) günceller
    public void update(User user) {
        em.merge(user);
    }

    // Giriş (Login) işlemi için kullanıcı adından kullanıcıyı bulur
    public User findByUsername(String username) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Kullanıcı bulunamazsa null döndür
        }
    }
}