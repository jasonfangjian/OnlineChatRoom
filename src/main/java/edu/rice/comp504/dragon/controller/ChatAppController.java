package edu.rice.comp504.dragon.controller;

import edu.rice.comp504.dragon.model.DispatchAdapter;
import edu.rice.comp504.dragon.model.user.User;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

import static spark.Spark.*;

/**
 * The chat app edu.rice.comp504.dragon.controller communicates with all the clients on the web socket.
 */
public class ChatAppController {
    public static Map<Session, User> userNameMap = new ConcurrentHashMap<>();
    static int nextUserId = 1;

    /**
     * Chat App entry point.
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        port(getHerokuAssignedPort());
        staticFiles.location("/public");

        webSocket("/chatapp", WebSocketController.class);
        init();
    }

    private static void loginUser(final Session session, final JSONObject json) {
        final User user = DispatchAdapter.getInstance().login(session, json);
        if (user != null) {
            userNameMap.put(session, user);
        }
    }

    /**
     * Unpack the received message and use dispatchAdapter to respond.
     */
    public static void handler(Session session, String msgPack) {
        JSONObject jo = new JSONObject(msgPack);
        String type = jo.getString("type");
        System.out.println(msgPack);
        switch (type) {
            case "login":
                loginUser(session, jo);
                break;
            case "joinRoom":
                DispatchAdapter.getInstance().joinRoom(userNameMap.get(session), jo.getString("roomName"));
                break;
            case "leaveRoom":
                DispatchAdapter.getInstance().leaveRoom(jo.getString("roomName"),
                        userNameMap.get(session));
                break;
            case "sendUserMsg":
                DispatchAdapter.getInstance().sendUserMsg(userNameMap.get(session),
                        jo.getString("context"),
                        jo.getString("roomName"),
                        jo.getLong("timeStamp"));
                break;
            case "recallMsg":
            case "deleteMsg":
                DispatchAdapter.getInstance().recallOrDeleteMsg(userNameMap.get(session),
                        jo.getString("roomName"),
                        jo.getString("msgId"),
                        type,
                        jo.getLong("timeStamp"));
                break;
            case "editMsg":
                DispatchAdapter.getInstance().editMsg(userNameMap.get(session),
                        jo.getString("roomName"),
                        jo.getString("msgId"),
                        jo.getString("newContext"),
                        jo.getLong("timeStamp"));
                break;
            case "logout":
                DispatchAdapter.getInstance().logout(userNameMap.get(session));
                break;
            case "createRoom":
                if (jo.getString("restriction").equals("true")) {
                    ArrayList<String> school = new ArrayList<>();
                    ArrayList<String> interest = new ArrayList<>();
                    for (int i = 0 ; i < jo.getJSONArray("schoolList").length();i++) {
                        school.add((String)jo.getJSONArray("schoolList").get(i));
                    }
                    for (int i = 0 ; i < jo.getJSONArray("interestList").length();i++) {
                        interest.add((String)jo.getJSONArray("interestList").get(i));
                    }
                    String low = (String)jo.getJSONArray("ageRange").get(0);
                    String hi = (String)jo.getJSONArray("ageRange").get(1);
                    Point age = null;
                    if (!low.equals("") && !hi.equals("")) {
                        age = new Point(Integer.parseInt(low),Integer.parseInt(hi));
                    }
                    DispatchAdapter.getInstance().createRoom(userNameMap.get(session),
                            jo.getString("roomName"),age,
                            school.toArray(new String[school.size()]),
                            interest.toArray(new String[interest.size()]),
                            jo.getBoolean("isPrivate"));
                } else if (jo.getString("restriction").equals("false")) {
                    DispatchAdapter.getInstance().createRoom(userNameMap.get(session),
                            jo.getString("roomName"),jo.getBoolean("isPrivate"));
                } else {
                    DispatchAdapter.getInstance().createRoom(userNameMap.get(session),jo.getString("Guest"));
                }
                break;
            case "invite":
                ArrayList<String> target = new ArrayList<>();
                for (int i = 0 ; i < jo.getJSONArray("target").length();i++) {
                    target.add((String)jo.getJSONArray("target").get(i));
                }
                DispatchAdapter.getInstance().inviteUser(userNameMap.get(session),jo.getString("roomName"),target.toArray(new String[target.size()]));
                break;
            case "block":
                DispatchAdapter.getInstance().blockUser(userNameMap.get(session),jo.getString("tarUser"));
                break;
            case "unblock":
                DispatchAdapter.getInstance().unblockUser(userNameMap.get(session),jo.getString("tarUser"));
                break;
            case "ban":
                DispatchAdapter.getInstance().banUser(userNameMap.get(session),jo.getString("roomName"),jo.getString("tarUser"));
                break;
            case"unban":
                DispatchAdapter.getInstance().unbanUser(userNameMap.get(session),jo.getString("roomName"),jo.getString("tarUser"));
                break;
            case"kickout":
                DispatchAdapter.getInstance().kickOutUser(userNameMap.get(session),jo.getString("roomName"),jo.getString("tarUser"));
                break;
            default:
                break;
        }
    }

    /**
     * Get the heroku assigned port number.
     *
     * @return The heroku assigned port number
     */
    private static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; // return default port if heroku-port isn't set.
    }
}
