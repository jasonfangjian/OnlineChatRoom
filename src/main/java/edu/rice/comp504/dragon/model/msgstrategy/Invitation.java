package edu.rice.comp504.dragon.model.msgstrategy;

import edu.rice.comp504.dragon.model.message.Message;

public class Invitation extends AMsgStrategy {
    private static AMsgStrategy only;

    /**
     * comments.
     * @return rt.
     */
    public static AMsgStrategy getOnly() {
        if (only == null) {
            only = new Invitation();
        }
        return only;
    }


//    private final static String formatter = "%s invite you join room \"%s\"";

    @Override
    public String getName() {
        return "invitation";
    }

    @Override
    protected String getDisplayMsg(Message msg) {
//        return String.format(formatter, msg.getUser().getUsername(), msg.getRoom());
        return String.format("invite you join", msg.getUser().getUsername(), msg.getRoom());
    }
}
