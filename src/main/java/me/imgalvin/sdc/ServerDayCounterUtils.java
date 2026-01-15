package me.imgalvin.sdc;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class ServerDayCounterUtils {
    private static final String MOD_ID = "sdc";

    // Enum for different message types
    public enum MessageType {
        JOIN,
        NEW_DAY
    }

    // Data attachment to store the different strings
    private static final AttachmentType<String> DAY_COUNT_JOIN_MESSAGE = AttachmentRegistry.<String>builder()
            .initializer(() -> "World day: %day_count%")
            .buildAndRegister(Identifier.of(MOD_ID, "join_message"));

    private static final AttachmentType<String> DAY_COUNT_NEW_DAY_MESSAGE = AttachmentRegistry.<String>builder()
            .initializer(() -> "A new day has begun! Day: %day_count%")
            .buildAndRegister(Identifier.of(MOD_ID, "new_day_message"));

    // Set and get methods for the attachment
    public static boolean setMessage(String message, ServerWorld world, MessageType type) {
        if (!new ServerDayCounterUtils().isValidMessage(message)) {
            return false;
        }
        switch (type) {
            case JOIN -> world.setAttached(DAY_COUNT_JOIN_MESSAGE, message);
            case NEW_DAY -> world.setAttached(DAY_COUNT_NEW_DAY_MESSAGE, message);
        }
        return true;
    }

    public static String getMessage(ServerWorld world, MessageType type) {
        return switch (type) {
            case JOIN -> world.getAttached(DAY_COUNT_JOIN_MESSAGE);
            case NEW_DAY -> world.getAttached(DAY_COUNT_NEW_DAY_MESSAGE);
        };
    }

    // Helper and validation methods
    private boolean isValidMessage(String message) {
        return message != null && !message.trim().isEmpty() && message.contains("%day_count%");
    }
}
