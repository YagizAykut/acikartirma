package com.acikartirma.acikartirma.facade;

import com.acikartirma.acikartirma.entity.Notification;
import com.acikartirma.acikartirma.facadelocal.NotificationFacadeLocal;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
@SuppressWarnings("unused")
public class NotificationFacade extends AbstractFacade<Notification> implements NotificationFacadeLocal {

    @PersistenceContext
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public NotificationFacade() {
        super(Notification.class);
    }

    @Override
    public void update(Notification notification) {
        this.edit(notification);
    }

    @Override
    public List<Notification> findUnreadNotificationsByUser(Long userId) {
        return em.createQuery("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.read = false ORDER BY n.createdAt DESC", Notification.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}