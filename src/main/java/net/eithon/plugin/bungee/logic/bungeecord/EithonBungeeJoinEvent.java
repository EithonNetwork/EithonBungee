package net.eithon.plugin.bungee.logic.bungeecord;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class EithonBungeeJoinEvent extends EithonBungeeJoinQuitEvent {
	private static final HandlerList handlers = new HandlerList();

	public EithonBungeeJoinEvent(String thisServerName, String thatServerName, UUID playerId, String playerName, String mainGroup) {
		super(thisServerName, thatServerName, playerId, playerName, mainGroup);
	}

	public EithonBungeeJoinEvent(String thisServerName, String thatServerName, Player player, String mainGroup) {
		super(thisServerName, thatServerName, player, mainGroup);
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
