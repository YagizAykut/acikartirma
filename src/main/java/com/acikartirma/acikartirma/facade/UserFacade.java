package com.acikartirma.acikartirma.facade;

import com.acikartirma.acikartirma.entity.User;
import com.acikartirma.acikartirma.facadelocal.UserFacadeLocal;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;

@Stateless
public class UserFacade extends AbstractFacade<User> implements UserFacadeLocal {

    @PersistenceContext
    private EntityManager em;

    // AbstractFacade'in bizden istediği zorunlu metot
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    // AbstractFacade'e hangi Entity ile çalışacağını söylüyoruz
    public UserFacade() {
        super(User.class);
    }

    // DİKKAT: create, edit, remove, findAll metotlarının hepsini sildik!
    // Çünkü onlar artık AbstractFacade'den otomatik geliyor.

    // Sadece User'a ÖZEL olan metotları burada tutuyoruz:
    @Override
    public User findByUsername(String username) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}