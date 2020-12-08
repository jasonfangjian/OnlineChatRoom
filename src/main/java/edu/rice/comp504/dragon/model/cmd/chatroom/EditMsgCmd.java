package edu.rice.comp504.dragon.model.cmd.chatroom;

import edu.rice.comp504.dragon.Utils;
import edu.rice.comp504.dragon.model.DispatchAdapter;
import edu.rice.comp504.dragon.model.chatroom.AChatRoom;
import edu.rice.comp504.dragon.model.cmd.user.IUserCmd;
import edu.rice.comp504.dragon.model.cmd.user.SendMsgCmd;
import edu.rice.comp504.dragon.model.message.Message;
import edu.rice.comp504.dragon.model.msgstrategy.AMsgStrategy;
import edu.rice.comp504.dragon.model.msgstrategy.DeleteMsg;
import edu.rice.comp504.dragon.model.msgstrategy.EditMsg;
import edu.rice.comp504.dragon.model.msgstrategy.RecallMsg;
import edu.rice.comp504.dragon.model.user.User;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;

public class EditMsgCmd implements IChatRoomCmd{
    private final String roomName;
    private final String msgId;
    private final User user;
    private final String newContext;
    private final EditType type;
    private final long time;

    public EditMsgCmd(final User user,
                      final String roomName,
                      final String msgId,
                      final String newContext,
                      final String type) {
        this(user,roomName,msgId,newContext,type, Utils.getTimeStamp());
    }

    /**
     * comments.
     * @param user u.
     * @param roomName r.
     * @param msgId m.
     * @param newContext n.
     * @param type t.
     * @param time t.
     */
    public EditMsgCmd(final User user,
                      final String roomName,
                      final String msgId,
                      final String newContext,
                      final String type,
                      final long time) {
        this.roomName = roomName;
        this.msgId = msgId;
        this.user = user;
        this.newContext = newContext;
        this.type = EditType.valueOf(type);
        this.time = time;
    }

    @Override
    public String getName() {
        return "broadcast";
    }

    @Override
    public void execute(AChatRoom room) {
        if (!room.getRoomName().equals(roomName)) {
            return;
        }
        if (!type.accessCheck.hasAccess(room, user, msgId)) {
            //todo: replace with cmd?
            System.out.println(msgId);
            JSONObject json = new JSONObject();
            json.put("msgType", type.name() + "Error");
            json.put("target", msgId);
            json.put("reason", "permission denied");
            try {
                user.getSession().getRemote().sendString(json.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        System.out.println(msgId);
        room.removeMessage(msgId);
        final Message newMsg = new Message(null, this.user, type.getStrategy(), roomName, time);
        newMsg.appendAttachment(Message.AttachmentType.DELETE_MSG, msgId);
        newMsg.appendAttachment(Message.AttachmentType.UPDATE_MSG, newContext);
        room.appendMsg(newMsg);
        final IUserCmd cmd = new SendMsgCmd(newMsg);
        DispatchAdapter.getInstance().firePropertyChange(room.getRoomName(), null, cmd);
    }

    private enum EditType {
        recallMsg(RecallMsg::getOnly, (room, usr, msgId) -> msgId.endsWith(usr.getUsername())),
        editMsg(EditMsg::getOnly, (room, usr, msgId) -> msgId.endsWith(usr.getUsername())),
        deleteMsg(DeleteMsg::getOnly, (room, usr, msgId) -> room.getAdmin().equals(usr));
        private interface StrategyFactor {
            AMsgStrategy getStrategy();
        }

        private interface EditAccessCheck {
            boolean hasAccess(final AChatRoom room, final User user, final String msgId);
        }

        EditType(final StrategyFactor factor, final EditAccessCheck accessCheck) {
            this.factor = factor;
            this.accessCheck = accessCheck;
        }

        AMsgStrategy getStrategy() {
            return factor.getStrategy();
        }

        final StrategyFactor factor;
        final EditAccessCheck accessCheck;
    }
}
