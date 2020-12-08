package edu.rice.comp504.dragon.model.chatroom;

import edu.rice.comp504.dragon.model.message.Message;
import edu.rice.comp504.dragon.model.user.User;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DirectRoom extends AChatRoom {

    private final User guest;

    public DirectRoom(final User admin, final User guest) {
        super(admin, String.format("DM(%s,%s)", admin.getUsername(), guest.getUsername()));
        this.guest = guest;
    }


    @Override
    public boolean canUserJoin(User user) {
        return false;
    }

    @Override
    public List<User> getUsers() {
        return Arrays.asList(admin, guest);
    }

    @Override
    public boolean msgCheck(Message msg) {
        // todo: check banWords
        return true;
    }

    @Override
    public Map<String, String> getRoomInfos() {
        return null;
    }

    @Override
    public boolean isViewableByUser(User user) {
        return user.equals(admin) || user.equals(guest);
    }
}
