package me.imgalvin.sdc;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class ServerDayCounterSavedData extends SavedData {
    private static final Codec<ServerDayCounterSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("join_message").forGetter(data -> Optional.ofNullable(data.joinMessage)),
            Codec.STRING.optionalFieldOf("new_day_message").forGetter(data -> Optional.ofNullable(data.newDayMessage))
    ).apply(instance, ServerDayCounterSavedData::new));

    public static final SavedDataType<ServerDayCounterSavedData> TYPE = new SavedDataType<>(
            Identifier.withDefaultNamespace("server_day_counter_messages"),
            ServerDayCounterSavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private String joinMessage;
    private String newDayMessage;

    public ServerDayCounterSavedData() {
    }

    private ServerDayCounterSavedData(Optional<String> joinMessage, Optional<String> newDayMessage) {
        this.joinMessage = joinMessage.orElse(null);
        this.newDayMessage = newDayMessage.orElse(null);
    }

    public String getMessage(ServerDayCounterUtils.MessageType type) {
        return switch (type) {
            case JOIN -> joinMessage;
            case NEW_DAY -> newDayMessage;
        };
    }

    public void setMessage(ServerDayCounterUtils.MessageType type, String message) {
        switch (type) {
            case JOIN -> joinMessage = message;
            case NEW_DAY -> newDayMessage = message;
        }

        setDirty();
    }

    public void clearMessage(ServerDayCounterUtils.MessageType type) {
        setMessage(type, null);
    }
}
