package edu.rice.comp504.dragon.model;

import com.google.gson.Gson;
import edu.rice.comp504.dragon.Utils;
import edu.rice.comp504.dragon.model.chatroom.AChatRoom;
import edu.rice.comp504.dragon.model.chatroom.ChatRoom;
import edu.rice.comp504.dragon.model.chatroom.DirectRoom;
import edu.rice.comp504.dragon.model.chatroom.PrivateRoom;
import edu.rice.comp504.dragon.model.cmd.chatroom.*;
import edu.rice.comp504.dragon.model.message.Message;
import edu.rice.comp504.dragon.model.msgstrategy.UserMsg;
import edu.rice.comp504.dragon.model.user.User;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.rice.comp504.dragon.Utils.toJson;

public class DispatchAdapter {
    /**
     * Keep track of all the users and rooms.
     */
    private static DispatchAdapter instance;
    private PropertyChangeSupport pcs;

    public static final String tagUser = "#theUser";
    public static final String tagRoom = "#theRoom";


    /**
     * comments.
     * @return instance.
     */
    public static DispatchAdapter getInstance() {
        if (instance == null) {
            instance = new DispatchAdapter();
        }
        return instance;
    }

    private DispatchAdapter() {
        this.pcs = new PropertyChangeSupport(this);
    }
    //TODO: We may need some data structure to store socket ids. Not sure now.

    /**
     * comments.
     * @param arr a.
     * @return rt.
     */
    public String[] toStringArr(JSONArray arr) {
        List<String> tmpStringList = new ArrayList<String>();
        for (int i = 0; i < arr.length(); ++i) {
            tmpStringList.add(arr.getString(i));
        }
        return tmpStringList.toArray(new String[tmpStringList.size()]);
    }

    /**
     * login.
     * @param session current session.
     * @param jo info from frontend.
     * @return User Obj.
     */
    public User login(Session session, JSONObject jo) {
        final String username = jo.getString("username");
        final boolean invalidUserName = Arrays.stream(pcs.getPropertyChangeListeners(tagUser))
                .anyMatch(listener -> ((User) listener).getUsername().equals(username));
        if (invalidUserName) {
            JSONObject toSend = new JSONObject();
            toSend.put("type", "loginFailed");
            toSend.put("info", "invalidUsername");
            try {
                session.getRemote().sendString(toSend.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        System.out.println(jo.getString("age"));
        int age = -1;
        if (!jo.getString("age").equals("")) {
            age = Integer.parseInt(jo.getString("age"));
        }
        final String school = jo.getString("school");
        List<String> interestList = new ArrayList<>();
        for (int i = 0 ; i < jo.getJSONArray("interestList").length();i++) {
            interestList.add((String)jo.getJSONArray("interestList").get(i));
        }
        final String[] interest = interestList.toArray(new String[interestList.size()]);
        final User user = new User(session, username, age, school, interest);
        pcs.addPropertyChangeListener(tagUser,user);
        //TODO: send JSONObject to frontend
        JSONObject toSend = new JSONObject();
        toSend.put("type","loginResponse");
        toSend.put("username",username);
        PropertyChangeListener[] pcls = pcs.getPropertyChangeListeners(tagRoom);
        List<String> list = Arrays.stream(pcls)
                .map(listener -> (AChatRoom) listener)
                .filter(room -> room.isViewableByUser(user))
                .map(AChatRoom::getRoomName)
                .collect(Collectors.toList());
        //toJson(list);//could use utils
        toSend.put("visibleRooms",list);
        try {
            session.getRemote().sendString(toSend.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return user;
    }

    /**
     * log out.
     * @param user User obj.
     */
    public void logout(final User user) {
        final IChatRoomCmd cmd = new RemoveUserCmd(user, null, "user log out");
        pcs.firePropertyChange(tagRoom, null, cmd);
        pcs.removePropertyChangeListener(tagUser,user);
    }

    public void joinRoom(User user, String roomName) {
        final IChatRoomCmd cmd = new AddUserCmd(user, roomName);
        pcs.firePropertyChange(tagRoom, null, cmd);
    }

    public void inviteUser(final User from, final String roomName, final String[] targets) {
        final IChatRoomCmd cmd = new InviteUser2RoomCmd(roomName, targets, from);
        pcs.firePropertyChange(tagRoom, null, cmd);
    }

    public void leaveRoom(final String roomName, final User user) {
        final IChatRoomCmd cmd = new RemoveUserCmd(user, roomName, "user leaved room");
        pcs.firePropertyChange(tagRoom, null, cmd);
    }

    /**
     * sendMsg.
     * @param sender User obj.
     * @param context Context from frontend.
     * @param roomName RoomName.
     * @param time Timestamp.
     */
    public void sendUserMsg(final User sender, final String context, final String roomName, final long time) {
        final Message msg = new Message(context, sender, UserMsg.getOnly(), roomName, time);

        final IChatRoomCmd cmd = new BroadcastCmd(msg);
        pcs.firePropertyChange(tagRoom, null, cmd);
    }

    public void recallOrDeleteMsg(User user, String roomName, String msgId, String type, long time) {
        final IChatRoomCmd cmd = new EditMsgCmd(user, roomName, msgId, null, type, time);
        pcs.firePropertyChange(tagRoom, null, cmd);
    }

    public void editMsg(User user, String roomName, String msgId, String newContext, long time) {
        final IChatRoomCmd cmd = new EditMsgCmd(user, roomName, msgId, newContext, "editMsg", time);
        pcs.firePropertyChange(tagRoom, null, cmd);
    }


    public void addUserToRoom(final String roomName, final User user) {
        pcs.addPropertyChangeListener(roomName, user);
    }

    public void removeUserFromRoom(final String roomName, final User user) {
        pcs.removePropertyChangeListener(roomName, user);
    }

    /**
     * getUserList.
     * @param roomName string.
     * @return List of User obj.
     */
    public List<User> listUser(final String roomName) {
        return Arrays.stream(pcs.getPropertyChangeListeners(roomName == null ? tagUser : roomName))
                .filter(User.class::isInstance)
                .map(listener -> (User) listener)
                .collect(Collectors.toList());
    }


    /**
     * create room witout restriction.
     * @param admin User.
     * @param roomName String.
     * @param isPrivate boolean.
     */
    public void createRoom(User admin, String roomName, boolean isPrivate) {
        //without restriction
        final boolean invalidRoomName = Arrays.stream(pcs.getPropertyChangeListeners(tagRoom))
                .anyMatch(listener -> ((AChatRoom) listener).getRoomName().equals(roomName));
        if (invalidRoomName) {
            JSONObject toSend = new JSONObject();
            toSend.put("type", "createRoomFailed");
            toSend.put("info", "invalidRoomName");
            try {
                admin.getSession().getRemote().sendString(toSend.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        final AChatRoom room = isPrivate
                ? new PrivateRoom(admin, roomName)
                : new ChatRoom(admin, roomName);
        pcs.addPropertyChangeListener(tagRoom, room);
//        pcs.addPropertyChangeListener(roomName, admin);
        addUserToRoom(room.getRoomName(), admin);
        notifyNewRoom(room);
    }

    /**
     * create room with restriction.
     * @param admin User.
     * @param roomName String.
     * @param ageRange Point.
     * @param schoolList Array.
     * @param interestList Array.
     * @param isPrivate boolean.
     */
    public void createRoom(User admin,
                           String roomName,
                           Point ageRange,
                           String[] schoolList,
                           String[] interestList,
                           boolean isPrivate) {
        //with restriction
        final boolean invalidRoomName = Arrays.stream(pcs.getPropertyChangeListeners(tagRoom))
                .anyMatch(listener -> ((AChatRoom) listener).getRoomName().equals(roomName));
        if (invalidRoomName) {
            JSONObject toSend = new JSONObject();
            toSend.put("type", "createRoomFailed");
            toSend.put("info", "invalidRoomName");
            try {
                admin.getSession().getRemote().sendString(toSend.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        final AChatRoom room = isPrivate
                ? new PrivateRoom(admin, roomName, ageRange, schoolList, interestList)
                : new ChatRoom(admin, roomName, ageRange, schoolList, interestList);
        pcs.addPropertyChangeListener(tagRoom, room);
//        pcs.addPropertyChangeListener(roomName, admin);
        addUserToRoom(room.getRoomName(), admin);
        notifyNewRoom(room);

    }

    /**
     * creat direct room.
     * @param admin User.
     * @param guest String.
     */
    public void createRoom(User admin, String guest) {
        // TODO: changeit to command,;
        // TODO: check room exists or not.
        PropertyChangeListener[] pcls = pcs.getPropertyChangeListeners(tagUser);
        for (PropertyChangeListener pcl : pcls) {
            User u = (User) pcl;
            if (u.getUsername().equals(guest)) {
                final AChatRoom room = new DirectRoom(admin, u);
                pcs.addPropertyChangeListener(tagRoom, room);
                pcs.addPropertyChangeListener(room.getRoomName(), admin);
                pcs.addPropertyChangeListener(room.getRoomName(), u);
                notifyNewRoom(room);
                return;
            }
        }
        //could use stream
        JSONObject toSend = new JSONObject();
        toSend.put("type", "createRoomFailed");
        toSend.put("info", "invalidGuestName");
        try {
            admin.getSession().getRemote().sendString(toSend.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * remove Room.
     * @param room AChatRoom.
     */
    public void removeRoom(final AChatRoom room) {
        notifyRemoveRoom(room);
        Arrays.stream(pcs.getPropertyChangeListeners(room.getRoomName()))
                .forEach(listener -> pcs.removePropertyChangeListener(room.getRoomName(), listener));
        pcs.removePropertyChangeListener(tagRoom, room);
    }

//    public void broadcastMsgPack(final String roomName, final User user, final JSONObject jo) {
//        pcs.firePropertyChange(roomName, user, jo);
//    }

    /**
     * notify create enw room.
     * @param room AChatRoom.
     */
    public void notifyNewRoom(AChatRoom room) {
        JSONObject toSend = new JSONObject();
        toSend.put("roomName", room.getRoomName());
        toSend.put("admin", room.getAdmin().getUsername());
        final Stream<User> stream;
        if (room instanceof PrivateRoom || room instanceof DirectRoom) {
            toSend.put("msgType","newPrivateRoom");
            //List<User> temp = room.getUsers();
            //temp.add(room.getAdmin());
            //stream = temp.stream();
            stream = room.getUsers().stream();
        } else {
            toSend.put("schoolList",room.getRoomInfos().get("schoolList"));
            toSend.put("interest",room.getRoomInfos().get("interestList"));
            String age = room.getRoomInfos().get("ageRange");
            String[] ages = age.split(" ~ ");
            int[] ageInteger = new int[]{Integer.parseInt(ages[0]),Integer.parseInt(ages[1])};
            toSend.put("ageRange",ageInteger);
            toSend.put("msgType","newPublicRoom");
            stream = Arrays.stream(pcs.getPropertyChangeListeners(tagUser))
                    .filter(User.class::isInstance)
                    .map(listener -> (User)listener);
        }
        stream.forEach(user -> {
            try {
                user.getSession().getRemote().sendString(toSend.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * notify remove a room.
     * @param room AChatRoom.
     */
    public void notifyRemoveRoom(AChatRoom room) {
        JSONObject toSend = new JSONObject();
        toSend.put("msgType", "removeRoom");
        toSend.put("roomName", room.getRoomName());
        final Stream<User> stream;
        if (room instanceof PrivateRoom || room instanceof DirectRoom) {
            stream = room.getUsers().stream();
//            System.out.println("removeP");
//            List<User> temp = room.getUsers();
//            System.out.println(toJson(temp));
//            temp.add(room.getAdmin());
//            stream = temp.stream();
        } else {
            stream = Arrays.stream(pcs.getPropertyChangeListeners(tagUser))
                    .filter(User.class::isInstance)
                    .map(listener -> (User)listener);
        }
        stream.forEach(user -> {
            try {
                if (user.getSession().isOpen()) {
                    user.getSession().getRemote().sendString(toSend.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void blockUser(final User user, final String blockId) {
        user.blockUser(blockId);
    }

    public void reportUser(final User user, final String roomName, final String userId, final long time) {
        final IChatRoomCmd cmd = new ReportUserCmd(user, roomName, userId,time);
        pcs.firePropertyChange(tagRoom, null, cmd);
    }//??

    public void unblockUser(final User user, final String blockId) {
        user.unblockUser(blockId);
    }

    public void banUser(final User request, final String roomName, final String userId) {
        final IChatRoomCmd cmd = new BanUserCmd(request, roomName, userId, "mute");
        pcs.firePropertyChange(tagRoom, null, cmd);
    }

    public void unbanUser(final User request, final String roomName, final String userId) {
        final IChatRoomCmd cmd = new BanUserCmd(request, roomName, userId, "unmute");
        pcs.firePropertyChange(tagRoom, null, cmd);
    }

    public void kickOutUser(final User user, final String roomName, final String tarUser) {
        final IChatRoomCmd cmd = new BanUserCmd(user, roomName, tarUser, "kick");
        pcs.firePropertyChange(tagRoom, null, cmd);
    }


//    public JSONObject makeUserListUpdatePack(String info, String roomName, String username) {
//        JSONObject userListUpdate = new JSONObject();
//        userListUpdate.put("type", "userListUpdate");
//        userListUpdate.put("info", info);
//        userListUpdate.put("roomName", roomName);
//        userListUpdate.put("username", username);
//
//        return userListUpdate;
//    }

    public PropertyChangeSupport getPcs() {
        return pcs;
    }

    /**
     * makeMsgListUpdatePack.
     * @param roomName String.
     * @param newMsg Message obj.
     * @return JSONObject.
     */
    public JSONObject makeMsgListUpdatePack(String roomName, Message newMsg) {
        JSONObject msgListUpdate = new JSONObject();
        msgListUpdate.put("type", "addMsg");
        msgListUpdate.put("roomName", roomName);
        msgListUpdate.put("message", newMsg);

        return msgListUpdate;
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }
}
