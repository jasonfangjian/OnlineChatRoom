package edu.rice.comp504.dragon.model.msgstrategy;

import edu.rice.comp504.dragon.Utils;
import edu.rice.comp504.dragon.model.message.Message;
import edu.rice.comp504.dragon.model.user.User;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static edu.rice.comp504.dragon.Utils.toJson;

public class WelcomeMsg extends AMsgStrategy {

    private final Map<String, String> info;
    private final Message[] messages;
    private final User[] users;

    /**
     * comments.
     * @param info io.
     * @param messages m.
     * @param users u.
     */
    public WelcomeMsg(Map<String,String> info, Message[] messages, final User[] users) {
        this.info = info;
        this.messages = messages;
        this.users = users;
    }

    public String getName() {
        return "welcome";
    }

    @Override
    protected String getDisplayMsg(final Message msg) {
        final Map<String, String> map = new HashMap<>();
        final String[] displayMsg = Arrays.stream(messages)
                .map(Message::getKeyValueContext)
                .map(Utils::toJson)
                .toArray(String[]::new);
        final String[] userNames = Arrays.stream(users)
                .map(User::getUsername)
                .toArray(String[]::new);
        map.put("info", toJson(info));
        map.put("historyMsg", toJson(displayMsg));
        map.put("users", toJson(userNames));
        return toJson(map);
    }
}
