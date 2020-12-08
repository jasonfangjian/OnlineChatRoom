package edu.rice.comp504.dragon.controller;

import edu.rice.comp504.dragon.model.DispatchAdapter;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * Create a web socket for the server.
 */
@WebSocket
public class WebSocketController {

    /**
     * Open edu.rice.comp504.dragon.model.user's session.
     * @param user The edu.rice.comp504.dragon.model.user whose session is opened.
     */
    @OnWebSocketConnect
    public void onConnect(Session user) {
        System.out.printf("new session opened");
    }

    /**
     * Close the edu.rice.comp504.dragon.model.user's session.
     * @param user The use whose session is closed.
     */
    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        System.out.println("webSocket onClose");
        //System.out.println(ChatAppController.userNameMap.get(user).getUsername());
        DispatchAdapter.getInstance().logout(ChatAppController.userNameMap.get(user));
        ChatAppController.userNameMap.remove(user);
    }

    /**
     * Send a edu.rice.comp504.dragon.model.message.
     * @param session  The session edu.rice.comp504.dragon.model.user sending the edu.rice.comp504.dragon.model.message.
     * @param message The message to be sent.
     */
    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        if (message.equals("ping")) {
            System.out.println("ping");
            return;
        }
        ChatAppController.handler(session, message);
    }
}
