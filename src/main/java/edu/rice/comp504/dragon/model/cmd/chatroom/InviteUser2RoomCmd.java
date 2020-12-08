package edu.rice.comp504.dragon.model.cmd.chatroom;

import edu.rice.comp504.dragon.model.DispatchAdapter;
import edu.rice.comp504.dragon.model.chatroom.AChatRoom;
import edu.rice.comp504.dragon.model.chatroom.PrivateRoom;
import edu.rice.comp504.dragon.model.cmd.user.IUserCmd;
import edu.rice.comp504.dragon.model.cmd.user.InviteUserCmd;
import edu.rice.comp504.dragon.model.user.User;
import org.json.JSONObject;
import java.beans.PropertyChangeListener;
import java.io.IOException;


import static edu.rice.comp504.dragon.model.DispatchAdapter.tagUser;
public class InviteUser2RoomCmd implements IChatRoomCmd{

    private final String[] targetUsers;
    private final String targetRoom;
    private final User from;

    /**
     * comments
     * @param targetRoom t.
     * @param targetUsers t.
     * @param from f.
     */
    public InviteUser2RoomCmd(final String targetRoom, final String[] targetUsers, final User from) {
        this.targetUsers = targetUsers;
        this.targetRoom = targetRoom;
        this.from = from;
    }
    
    @Override
    public String getName() {
        return "invite2room";
    }

    @Override
    public void execute(AChatRoom room) {
//        if (!room.getRoomName().equals(targetRoom) || !(room instanceof PrivateRoom)) {
        if (!room.getRoomName().equals(targetRoom) && !(room instanceof PrivateRoom)) {
            return;
        }
        PrivateRoom privateRoom = (PrivateRoom) room;
        IUserCmd cmd = new InviteUserCmd(targetUsers, privateRoom, from);
        PropertyChangeListener[] pcls = DispatchAdapter.getInstance().getPcs().getPropertyChangeListeners(tagUser);
        boolean flag = false;
        //User tar = null;
        for (PropertyChangeListener pcl : pcls) {
            User u = (User)pcl;
            if (targetUsers[0].equals(u.getUsername()) && !targetUsers[0].equals(from.getUsername())) {
                flag = true;
                //tar = u;
                break;
            }
        }
        if (!flag) {
            JSONObject toSend = new JSONObject();
            toSend.put("type", "inviteFailed");
            toSend.put("info", "invalidUsername");
            try {
                from.getSession().getRemote().sendString(toSend.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //DispatchAdapter.getInstance().addUserToRoom(room.getRoomName(),tar);
            DispatchAdapter.getInstance().firePropertyChange(tagUser, null, cmd);
        }
    }
}
