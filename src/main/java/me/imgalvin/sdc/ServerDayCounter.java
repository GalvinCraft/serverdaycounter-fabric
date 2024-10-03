package me.imgalvin.sdc;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents ;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ServerDayCounter implements ModInitializer {

	private long dayCount = 0; // Track the day count

	@Override
	public void onInitialize() {
		// Register the tick event to update the day count
		ServerTickEvents.END_WORLD_TICK.register(this::onWorldTick);

		// Send a welcome message with the day count to players when they join
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.player;
			player.sendMessage(Text.literal("World day: " + dayCount).formatted(Formatting.YELLOW), false);
		});

		// Register the command when the mod initializes
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerDayCountCommand(dispatcher);
		});
	}

	private void onWorldTick(ServerWorld world) {
		// Get the game time in ticks and calculate the date
		long gameTime = world.getTimeOfDay();
		long newDayCount = gameTime / 24000;

		// Check if it's a new day and send a message to all players
		if (newDayCount != dayCount) {
			dayCount = newDayCount;
			world.getServer().getPlayerManager().broadcast(Text.literal("A new day has begun! Day: " + dayCount).formatted(Formatting.AQUA), false);
		}
	}

	private void registerDayCountCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
		// Register a command called "daycount"
		dispatcher.register(CommandManager.literal("daycount")
				.executes(context -> {
					// Send the current day count back to the player/console
					context.getSource().sendFeedback(() -> Text.literal("Current day count: " + dayCount), false);
					return 1;
				})
		);
	}
}
