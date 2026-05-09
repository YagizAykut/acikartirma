package com.acikartirma.acikartirma.facadelocal;

import com.acikartirma.acikartirma.entity.Notification;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface NotificationFacadeLocal {

    void create(Notification notification);
    void update(Notification notification);
    void remove(Notification notification);
    Notification find(Object id);
    List<Notification> findAll();

    List<Notification> findUnreadNotificationsByUser(Long userId);
}