package edu.rice.comp504.dragon.model.chatroom;

import edu.rice.comp504.dragon.model.DispatchAdapter;
import edu.rice.comp504.dragon.model.cmd.chatroom.IChatRoomCmd;
import edu.rice.comp504.dragon.model.message.Message;
import edu.rice.comp504.dragon.model.user.User;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

public abstract class AChatRoom implements PropertyChangeListener {
    protected final User admin;
    protected String roomName;

    private static final int messageMaxNum = 100;
    private List<String> messagesList;
    private Map<String, Message> messageMap;

    protected static final String[] systemBanWords = new String[]{"hate"};
    protected Set<String> banWords;

    /**
     * comments.
     * @return return.
     */
    public Message[] getStoredMsg() {
        return messageMap.values().stream()
                .sorted((m1, m2) -> (int) (m1.getTimeStamp() - m2.getTimeStamp()))
                .toArray(Message[]::new);
    }

    protected AChatRoom(final User admin, final String roomName) {
        this.admin = admin;
        this.roomName = roomName;
        this.messagesList = new LinkedList<>();
        this.messageMap = new HashMap<>();
        banWords = new HashSet<>();
        banWords.addAll(Arrays.asList(systemBanWords));
    }

    public User getAdmin() {
        return admin;
    }

    /**
     * comments.
     * @return rt.
     */
    public String getRoomName() {
        return this.roomName;
    }

    /**
     * comments.
     * @param msg msg.
     * @return rt.
     */
    public Message appendMsg(final Message msg) {
        if (!msgCheck(msg)) {
            msg.edit("*BLOCKED*");
            DispatchAdapter.getInstance().banUser(getAdmin(),getRoomName(),msg.getUser().getUsername());
        }
        if (messagesList.size() >= messageMaxNum) {
            final String id = messagesList.remove(0);
            messageMap.remove(id);
        }
        messagesList.add(msg.getMessageID());
        messageMap.put(msg.getMessageID(), msg);
        return msg;
    }

    /**
     * comments.
     * @param msgId string.
     * @return rt.
     */
    public Message removeMessage(final String msgId) {
        if (!messageMap.containsKey(msgId)) {
            return null;
        }
        final Message msg = messageMap.remove(msgId);
        messagesList.remove(msgId);
        return msg;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("into room propertyChange");
        assert evt.getNewValue() instanceof IChatRoomCmd;
        final IChatRoomCmd cmd = (IChatRoomCmd) evt.getNewValue();
        cmd.execute(this);
    }

    public abstract boolean canUserJoin(User user);

    public abstract List<User> getUsers();

    public abstract boolean msgCheck(final Message msg);

    public abstract Map<String, String> getRoomInfos();

    public abstract boolean isViewableByUser(final User user);
}
