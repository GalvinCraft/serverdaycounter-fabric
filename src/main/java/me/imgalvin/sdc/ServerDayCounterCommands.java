package me.imgalvin.sdc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.function.LongSupplier;

public final class ServerDayCounterCommands {
    private final LongSupplier dayCountSupplier;

    public ServerDayCounterCommands(LongSupplier dayCountSupplier) {
        this.dayCountSupplier = dayCountSupplier;
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("daycount")
                .executes(this::showDayCount)
                .then(Commands.literal("day")
                        .executes(this::showDayCount))
                .then(Commands.literal("message")
                        .requires(ServerDayCounterPermissions::canManageMessages)
                        .then(createMessageSubcommand("join", ServerDayCounterUtils.MessageType.JOIN))
                        .then(createMessageSubcommand("new_day", ServerDayCounterUtils.MessageType.NEW_DAY))));
    }

    private LiteralArgumentBuilder<CommandSourceStack> createMessageSubcommand(String name, ServerDayCounterUtils.MessageType type) {
        return Commands.literal(name)
                .executes(context -> showMessageTemplate(context.getSource(), type))
                .then(Commands.literal("view")
                        .executes(context -> showMessageTemplate(context.getSource(), type)))
                .then(Commands.literal("set")
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(context -> setMessageTemplate(
                                        context.getSource(),
                                        type,
                                        StringArgumentType.getString(context, "message")
                                ))))
                .then(Commands.literal("reset")
                        .executes(context -> resetMessageTemplate(context.getSource(), type)));
    }

    private int showDayCount(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(createCommandFeedback("Current day count: ", Long.toString(dayCountSupplier.getAsLong())));
        return 1;
    }

    private int showMessageTemplate(CommandSourceStack source, ServerDayCounterUtils.MessageType type) {
        ServerLevel world = getOverworld(source);

        source.sendSystemMessage(createCommandFeedback(
                getMessageLabel(type) + " template: ",
                ServerDayCounterUtils.getOrCreateMessage(world, type)
        ));
        return 1;
    }

    private int setMessageTemplate(CommandSourceStack source, ServerDayCounterUtils.MessageType type, String message) {
        ServerLevel world = getOverworld(source);

        if (!ServerDayCounterUtils.setMessage(message, world, type)) {
            source.sendSystemMessage(
                    Component.empty()
                            .append(Component.literal("Message must include ").withStyle(ChatFormatting.RED))
                            .append(Component.literal(ServerDayCounterUtils.DAY_COUNT_PLACEHOLDER).withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(".").withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        source.sendSystemMessage(createCommandFeedback(getMessageLabel(type) + " template updated: ", message));
        return 1;
    }

    private int resetMessageTemplate(CommandSourceStack source, ServerDayCounterUtils.MessageType type) {
        ServerLevel world = getOverworld(source);

        String defaultMessage = ServerDayCounterUtils.getDefaultMessage(type);
        ServerDayCounterUtils.setMessage(null, world, type);
        source.sendSystemMessage(createCommandFeedback(getMessageLabel(type) + " template reset to default: ", defaultMessage));
        return 1;
    }

    private static ServerLevel getOverworld(CommandSourceStack source) {
        return source.getServer().overworld();
    }

    private static String getMessageLabel(ServerDayCounterUtils.MessageType type) {
        return switch (type) {
            case JOIN -> "Join message";
            case NEW_DAY -> "New day message";
        };
    }

    private static Component createCommandFeedback(String label, String value) {
        return Component.empty()
                .append(Component.literal(label).withStyle(ChatFormatting.GRAY))
                .append(Component.literal(value).withStyle(ChatFormatting.YELLOW));
    }
}

