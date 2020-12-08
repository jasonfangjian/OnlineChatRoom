package edu.rice.comp504.dragon.model.cmd.user;

import edu.rice.comp504.dragon.model.chatroom.PrivateRoom;
import edu.rice.comp504.dragon.model.message.Message;
import edu.rice.comp504.dragon.model.msgstrategy.Invitation;
import edu.rice.comp504.dragon.model.user.User;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static edu.rice.comp504.dragon.Utils.toJson;

public class InviteUserCmd implements IUserCmd{

    private final List<String> targetUsers;
    private final PrivateRoom room;
    private final User from;
    private final Message invitation;

    /**
     * comments
     * @param targetUsers t.
     * @param room r.
     * @param from f.
     */
    public InviteUserCmd(final String[] targetUsers, final PrivateRoom room, final User from) {
        this.room = room;
        this.targetUsers = Arrays.asList(targetUsers);
        this.from = from;
        this.invitation = new Message(null, from, Invitation.getOnly(),room.getRoomName());
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public void execute(User user) {
        if (!targetUsers.contains(user.getUsername())) {
//            JSONObject toSend = new JSONObject();
//            toSend.put("type", "inviteUserFailed");
//            toSend.put("info", "invalidUserName");
//            try {
//                room.getAdmin().getSession().getRemote().sendString(toSend.toString());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            return;
        }
        room.inviteUser(user);

        try {
//            System.out.println("invititation");
//            System.out.println(invitation.getStrategy());
//            System.out.println(toJson(invitation.getStrategy()));
//            System.out.println(toJson(invitation.getUser()));
//            System.out.println(toJson(invitation.getRoom()));
//            System.out.println(toJson(invitation.getContext()));
            user.getSession().getRemote().sendString(toJson(invitation.getKeyValueContext()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject toSend = new JSONObject();
        toSend.put("type", "inviteUserSuccess");
        toSend.put("info", user.getUsername());
        try {
//            user.getSession().getRemote().sendString(toJson(invitation.getKeyValueContext()));
            room.getAdmin().getSession().getRemote().sendString(toSend.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
