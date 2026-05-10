package com.acikartirma.acikartirma.facade;

import com.acikartirma.acikartirma.entity.User;
import com.acikartirma.acikartirma.facadelocal.UserFacadeLocal;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

@Stateless
@SuppressWarnings("unused")
public class UserFacade extends AbstractFacade<User> implements UserFacadeLocal {

    @PersistenceContext
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UserFacade() {
        super(User.class);
    }

    @Override
    public void update(User user) {
        this.edit(user);
    }

    // @Override etiketini sildik, isim uyuşmazlığı varsa hata vermez
    public User findByUsername(String username) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    // @Override etiketini sildik, isim uyuşmazlığı varsa hata vermez
    public User validateLogin(String username, String password) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username AND u.password = :password", User.class)
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}