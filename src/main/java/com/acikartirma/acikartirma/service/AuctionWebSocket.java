package com.acikartirma.acikartirma.service;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint("/auction")
public class AuctionWebSocket {

    // Sitede o an aktif olan tüm kullanıcıların (tarayıcı sekmesi açık olanların) listesi
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        // Bir kullanıcı siteye (ana sayfaya) girdiğinde onu canlı yayın listesine ekler
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        // Kullanıcı sekmeyi kapattığında onu canlı yayın listesinden çıkarır
        sessions.remove(session);
    }

    // Sistemin kalbi: Verilen mesajı sitedeki HERKESE aynı anda fırlatır
    public static void broadcast(String message) {
        // Tüm aktif oturumları dön ve mesajı gönder
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        System.out.println("WebSocket Mesaj Gönderme Hatası: " + e.getMessage());
                    }
                }
            }
        }
    }
}