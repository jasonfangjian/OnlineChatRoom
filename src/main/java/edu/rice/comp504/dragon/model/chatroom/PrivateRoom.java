package edu.rice.comp504.dragon.model.chatroom;

import edu.rice.comp504.dragon.model.user.User;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class PrivateRoom extends ChatRoom{

    private final Set<String> inviteList;

    public PrivateRoom(User admin, String roomName) {
        super(admin, roomName);
        this.inviteList = new HashSet<>();
    }

    public PrivateRoom(User admin, String roomName, Point ageRange, String[] schoolList, String[] interestList) {
        super(admin, roomName, ageRange, schoolList, interestList);
        this.inviteList = new HashSet<>();
    }

    @Override
    public boolean canUserJoin(final User user) {
        return inviteList.contains(user.getUsername()) && checkRestriction(user);
    }

    public void inviteUser(final User user) {
        this.inviteList.add(user.getUsername());
    }

    public void inviteUser(final String userName) {
        this.inviteList.add(userName);
    }

    public void revokeInvite(final String userName) {
        this.inviteList.remove(userName);
    }

    public void revokeInvite(final User user) {
        this.inviteList.remove(user.getUsername());
    }

    public Set<String> getInviteList() {
        return inviteList;
    }

    @Override
    public boolean isViewableByUser(User user) {
        return inviteList.contains(user.getUsername()) || getUsers().contains(user);
    }
}

