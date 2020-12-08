package edu.rice.comp504.dragon.model.msgstrategy;

import edu.rice.comp504.dragon.model.message.Message;

import java.util.HashMap;
import java.util.Map;

import edu.rice.comp504.dragon.Utils;

public abstract class AMsgStrategy {
    public abstract String getName();

    protected abstract String getDisplayMsg(final Message msg);

    /**
     * comments
     * @param msg m.
     * @return rt.
     */
    public final Map<String, String> getMapContext(Message msg) {
        final Map<String, String> map = new HashMap<>();
        map.put("msgType", getName());
        map.put("msg", getDisplayMsg(msg));
        map.put("msgId", msg.getMessageID());
        map.put("room", msg.getRoom());
        map.put("time", Utils.epoch2LocalTime(msg.getTimeStamp()));
        map.put("from", msg.getUser().getUsername());
//        try {
//            msg.getAttachment().forEach((key, value) -> map.put(key.name(), value));
//        } catch (Exception e) {
        if (msg.getAttachment() != null) {
            msg.getAttachment().forEach((key, value) -> map.put(key.name(), value));
        }
        return map;
    }
}
