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