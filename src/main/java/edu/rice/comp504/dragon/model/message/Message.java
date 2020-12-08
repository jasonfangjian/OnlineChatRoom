package edu.rice.comp504.dragon.model.message;

import edu.rice.comp504.dragon.Utils;
import edu.rice.comp504.dragon.model.msgstrategy.AMsgStrategy;
import edu.rice.comp504.dragon.model.user.User;

import java.time.Instant;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class Message {
    private static final String idFormatter = "%d%s";
    private String context;
    private long timeStamp;
    private User user;

    private String room;


    /**
     * Apply strategy design pattern here.
     * I don't think this is necessary, but adding some design pattern may make the project look better.
     * */
    private AMsgStrategy strategy;

    /**
     * attachment of picture or msg operation.
     * picture could use Base64 Encoding.
     * operation used to remove message from client when edit, recall or delete.
     */
    private Map<AttachmentType, String> attachments;


    /**
     * comments.
     * @param context c.
     * @param user u.
     * @param strategy s.
     * @param room r.
     */
    public Message(String context, User user, AMsgStrategy strategy, String room) {
        this.context = context;
        this.timeStamp = Utils.getTimeStamp();
        this.user = user;
        this.strategy = strategy;
        this.room = room;
    }

    /**
     * comments
     * @param context c.
     * @param user u.
     * @param strategy s.
     * @param room r.
     * @param timeStamp t.
     */
    public Message(String context, User user, AMsgStrategy strategy, String room, long timeStamp) {
        this.context = context;
        this.timeStamp = timeStamp;
        this.user = user;
        this.strategy = strategy;
        this.room = room;
    }

    public User getUser() {
        return this.user;
    }

    public String getRoom() {
        return this.room;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public String getContext() {
        return this.context;
    }

    public Map<String, String> getKeyValueContext() {
        return strategy.getMapContext(this);
    }

    public void edit(String newContext) {
        this.context = newContext;
    }

    public void setStrategy(AMsgStrategy newStrategy) {
        strategy = newStrategy;
    }

    public AMsgStrategy getStrategy() {
        return strategy;
    }

    public String getMessageID() {
        return String.format(idFormatter, timeStamp, user.getUsername());
    }

    /**
     * comments.
     * @param type t.
     * @param context c.
     */
    public void appendAttachment(final AttachmentType type, final String context) {
        if (attachments == null) {
            attachments = new EnumMap<AttachmentType, String>(AttachmentType.class);
        }
        attachments.put(type, context);
    }

    /**
     * comments.
     * @param type t.
     */
    public void removeAttachment(final AttachmentType type) {
        attachments.remove(type);
        if (attachments.isEmpty()) {
            attachments = null;
        }
    }

    public boolean isUserMsg() {
        return strategy.getName().equals("UserMsg");
    }


    public Map<AttachmentType, String> getAttachment() {
        return attachments;
    }

    public enum AttachmentType {
        BAN_MSG,UNBAN_MSG,PICTURE, DELETE_MSG, UPDATE_MSG, ADD_USER, REMOVE_USER, REMOVE_REASON, REPORT_USER, ROOM_INFO, ROOM_MSG;
    }
}
