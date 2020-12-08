package edu.rice.comp504.dragon;

import com.google.gson.Gson;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Utils {
    private static final Gson gson = new Gson();

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static long getTimeStamp() {
        return Instant.now().toEpochMilli();
    }

    public static String epoch2LocalTime(final long epochMilli) {
        final Instant instant = Instant.ofEpochMilli(epochMilli);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toString();
    }
}
