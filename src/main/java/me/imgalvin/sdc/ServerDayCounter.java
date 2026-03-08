package me.imgalvin.sdc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class ServerDayCounter implements ModInitializer {

	private long dayCount = 0; // Track the day count
	private boolean initialized = false;

	@Override
	public void onInitialize() {
		// Check for stored message data when the server starts
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);

		// Register the tick event to update the day count
		ServerTickEvents.END_SERVER_TICK.register(this::onWorldTick);

		// Send a welcome message with the day count to players when they join
		ServerPlayConnectionEvents.JOIN.register((handler, _, _) -> {
			ServerPlayer player = handler.player;

			// If the day count is not initialized yet, just skip
			if (!initialized) {
				return;
			}

			// Suppress resource warning since we're not actually opening a new world here, just accessing the existing one.
			@SuppressWarnings("resource")
			ServerLevel world = player.level().getServer().overworld();

			String template = ServerDayCounterUtils.getOrCreateMessage(world, ServerDayCounterUtils.MessageType.JOIN);
			player.sendSystemMessage(
					Component.literal(ServerDayCounterUtils.formatMessage(template, dayCount)).withStyle(ChatFormatting.YELLOW),
					false
			);
		});

		// Register the command when the mod initializes
		ServerDayCounterCommands commands = new ServerDayCounterCommands(() -> dayCount);
		CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> commands.register(dispatcher));
	}

	private void onServerStarted(MinecraftServer minecraftServer) {
		// Load the current day count and any stored message templates on startup
		ServerLevel world = minecraftServer.overworld();

		initializeWorldState(world);
	}

	private void onWorldTick(MinecraftServer minecraftServer) {
		// Get world
		ServerLevel world = minecraftServer.overworld();

		// Fallback if startup happened before the overworld was ready
		if (!initialized) {
			initializeWorldState(world);
		}

		// Get the game time in ticks and calculate the day
		long newDayCount = world.getDefaultClockTime() / 24000;
		if (newDayCount == dayCount) {
			return;
		}

		// Check if it's a new day and send a message to all players
		dayCount = newDayCount;
		String template = ServerDayCounterUtils.getOrCreateMessage(world, ServerDayCounterUtils.MessageType.NEW_DAY);
		world.getServer().getPlayerList().broadcastSystemMessage(
				Component.literal(ServerDayCounterUtils.formatMessage(template, dayCount)).withStyle(ChatFormatting.AQUA),
				false
		);
	}

	private void initializeWorldState(ServerLevel world) {
		dayCount = world.getDefaultClockTime() / 24000;
		ServerDayCounterUtils.getOrCreateMessage(world, ServerDayCounterUtils.MessageType.JOIN);
		ServerDayCounterUtils.getOrCreateMessage(world, ServerDayCounterUtils.MessageType.NEW_DAY);
		initialized = true;
	}
}
