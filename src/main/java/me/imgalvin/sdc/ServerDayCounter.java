package me.imgalvin.sdc;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents ;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class ServerDayCounter implements ModInitializer {

	private long dayCount = 0; // Track the day count
	private boolean initialized = false;

	@Override
	public void onInitialize() {
		// Register the tick event to update the day count
		ServerTickEvents.END_SERVER_TICK.register(this::onWorldTick);

		// Send a welcome message with the day count to players when they join
		ServerPlayConnectionEvents.JOIN.register((handler, _, _) -> {
			ServerPlayer player = handler.player;

			// If the day count is not initialized yet, just skip
			if (!initialized) {
				return;
			}
			player.sendSystemMessage(Component.literal("World day: " + dayCount).withStyle(ChatFormatting.YELLOW), false);
		});

		// Register the command when the mod initializes
		CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> {
			registerDayCountCommand(dispatcher);
		});
	}

	private void onWorldTick(MinecraftServer minecraftServer) {
		// Get world
		ServerLevel world = minecraftServer.overworld();

		// Get the game time in ticks and calculate the date
		long gameTime = world.getDefaultClockTime();
		long newDayCount = gameTime / 24000;

		// Check if it's a new day and send a message to all players
		if (newDayCount != dayCount && initialized) {
			dayCount = newDayCount;
			world.getServer().getPlayerList().broadcastSystemMessage(Component.literal("A new day has begun! Day: " + dayCount).withStyle(ChatFormatting.AQUA), false);
		}

		// Botch job but this prevents null day count on server start being broadcasted to new players
		if (!initialized) {
			dayCount = newDayCount;
			initialized = true;
			world.getServer().getPlayerList().broadcastSystemMessage(Component.literal("World day: " + dayCount).withStyle(ChatFormatting.YELLOW), false);
		}
	}

	private void registerDayCountCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
		// Register the command
		dispatcher.register(Commands.literal("daycount")
				.executes(context -> {
					// Send the current day count back to the player/console
					context.getSource().sendSystemMessage(Component.literal("Current day count: " + dayCount));
					return 1;
				})
		);
	}
}
