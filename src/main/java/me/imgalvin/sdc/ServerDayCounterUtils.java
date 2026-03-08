package me.imgalvin.sdc;

import net.minecraft.server.level.ServerLevel;

public final class ServerDayCounterUtils {
    public static final String DAY_COUNT_PLACEHOLDER = "%day_count%";

    private static final String DEFAULT_JOIN_MESSAGE = "World day: " + DAY_COUNT_PLACEHOLDER;
    private static final String DEFAULT_NEW_DAY_MESSAGE = "A new day has begun! Day: " + DAY_COUNT_PLACEHOLDER;

    // Enum for different message types
    public enum MessageType {
        JOIN,
        NEW_DAY
    }

    private ServerDayCounterUtils() {
    }

    // Set and get methods for the saved world data
    public static boolean setMessage(String message, ServerLevel world, MessageType type) {
        ServerDayCounterSavedData savedData = getSavedData(world);

        if (message == null) {
            savedData.clearMessage(type);
            return true;
        }

        if (!isValidMessage(message)) {
            return false;
        }

        savedData.setMessage(type, message);
        return true;
    }

    public static String getMessage(ServerLevel world, MessageType type) {
        String message = getSavedData(world).getMessage(type);
        return message != null ? message : getDefaultMessage(type);
    }


    public static String getDefaultMessage(MessageType type) {
        return switch (type) {
            case JOIN -> DEFAULT_JOIN_MESSAGE;
            case NEW_DAY -> DEFAULT_NEW_DAY_MESSAGE;
        };
    }

    public static String formatMessage(String message, long dayCount) {
        return message.replace(DAY_COUNT_PLACEHOLDER, Long.toString(dayCount));
    }

    private static ServerDayCounterSavedData getSavedData(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(ServerDayCounterSavedData.TYPE);
    }

    // Helper and validation methods
    private static boolean isValidMessage(String message) {
        return message != null && !message.trim().isEmpty() && message.contains(DAY_COUNT_PLACEHOLDER);
    }
}
