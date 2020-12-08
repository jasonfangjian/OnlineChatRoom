package edu.rice.comp504.dragon.model.cmd.chatroom;

import edu.rice.comp504.dragon.model.DispatchAdapter;
import edu.rice.comp504.dragon.model.chatroom.AChatRoom;
import edu.rice.comp504.dragon.model.cmd.user.IUserCmd;
import edu.rice.comp504.dragon.model.cmd.user.SendMsgCmd;
import edu.rice.comp504.dragon.model.message.Message;
import edu.rice.comp504.dragon.model.msgstrategy.AMsgStrategy;
import edu.rice.comp504.dragon.model.msgstrategy.JoinRoom;
import edu.rice.comp504.dragon.model.msgstrategy.WelcomeMsg;
import edu.rice.comp504.dragon.model.user.User;
import org.json.JSONObject;

import com.google.gson.Gson;

import java.io.IOException;

public class AddUserCmd implements IChatRoomCmd{

    private final User user;
    private final String targetRoom;

    public AddUserCmd(final User user, final String targetRoom) {
        this.user = user;
        this.targetRoom = targetRoom;
    }

    @Override
    public String getName() {
        return "add-user";
    }

    @Override
    public void execute(AChatRoom room) {
        System.out.println("into AddUserCmd execute");
        if (!targetRoom.equals(room.getRoomName())) {
            return;
        }
        if (!room.canUserJoin(user)) {
            // todo: send reject reason
            JSONObject toSend = new JSONObject();
            toSend.put("type", "joinRoomFailed");
            toSend.put("info", "cannot join this room");
            try {
                user.getSession().getRemote().sendString(toSend.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        DispatchAdapter.getInstance().addUserToRoom(room.getRoomName(), user);

        Gson gson = new Gson();
        System.out.println("users of " + room.getRoomName() + " are: ");
        for (User user : room.getUsers()) {
            System.out.println(user.getUsername());
        }

        final Message newMsg = new Message(null, user, JoinRoom.getOnly(), room.getRoomName());
        newMsg.appendAttachment(Message.AttachmentType.ADD_USER, user.getUsername());
        IUserCmd cmd = new SendMsgCmd(room.appendMsg(newMsg));
        DispatchAdapter.getInstance().firePropertyChange(room.getRoomName(), null, cmd);

        final AMsgStrategy strategy = new WelcomeMsg(room.getRoomInfos(),
                room.getStoredMsg(),
                room.getUsers().toArray(User[]::new));
        final Message response = new Message(null,room.getAdmin(), strategy, room.getRoomName());
        new SendMsgCmd(response).execute(user);

    }
}
