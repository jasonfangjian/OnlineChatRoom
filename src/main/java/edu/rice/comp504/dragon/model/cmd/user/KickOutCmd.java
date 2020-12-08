package edu.rice.comp504.dragon.model.cmd.user;


import edu.rice.comp504.dragon.model.chatroom.AChatRoom;
import edu.rice.comp504.dragon.model.cmd.chatroom.RemoveUserCmd;
import edu.rice.comp504.dragon.model.user.User;

public class KickOutCmd implements IUserCmd {

    private final AChatRoom room;
    private final String targetId;

    public KickOutCmd(final AChatRoom room, final String targetId) {
        this.room = room;
        this.targetId = targetId;
    }

    @Override
    public String getName() {
        return "sendMsg";
    }

    @Override
    public void execute(User user) {
        if (!user.getUsername().equals(targetId)) {
            return;
        }
        new RemoveUserCmd(user, room.getRoomName(), "admin kick out").execute(room);
    }
}
