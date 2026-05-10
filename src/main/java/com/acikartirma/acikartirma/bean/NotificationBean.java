package com.acikartirma.acikartirma.bean;

import com.acikartirma.acikartirma.entity.Notification;
import com.acikartirma.acikartirma.facadelocal.NotificationFacadeLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
@SuppressWarnings("unused")
public class NotificationBean implements Serializable {

    @EJB
    private NotificationFacadeLocal notificationFacade;

    @Inject
    private UserBean userBean;

    private List<Notification> unreadNotifications;

    public NotificationBean() {
    }

    @PostConstruct
    public void init() {
        loadNotifications();
    }

    public void loadNotifications() {
        if (userBean.isLoggedIn()) {
            unreadNotifications = notificationFacade.findUnreadNotificationsByUser(userBean.getCurrentUser().getId());
        }
    }

    public void markAsRead(Notification notification) {
        notification.setRead(true);
        notificationFacade.update(notification);
        loadNotifications();
    }

    public void markAllAsRead() {
        if (unreadNotifications != null && !unreadNotifications.isEmpty()) {
            for (Notification n : unreadNotifications) {
                n.setRead(true);
                notificationFacade.update(n);
            }
            loadNotifications();
        }
    }

    public List<Notification> getUnreadNotifications() { return unreadNotifications; }
    public void setUnreadNotifications(List<Notification> unreadNotifications) { this.unreadNotifications = unreadNotifications; }
}