package com.acikartirma.acikartirma.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint("/auction")
@SuppressWarnings("unused")
public class AuctionWebSocket {

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    public static void broadcast(String message) {
        broadcast(message, null);
    }

    public static void broadcast(String message, String senderUsername) {
        String fullMessage = (senderUsername != null ? senderUsername : "") + "|" + message;
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(fullMessage);
                    } catch (IOException e) {
                        System.out.println("WebSocket Mesaj Gönderme Hatası: " + e.getMessage());
                    }
                }
            }
        }
    }
}