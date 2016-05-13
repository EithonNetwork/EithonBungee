package net.eithon.plugin.bungee.logic.joinleave;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class EithonBungeeQuitEvent extends EithonBungeeJoinQuitEvent {
	
	public EithonBungeeQuitEvent(String thisServerName, String thatServerName,
			UUID playerId, String playerName, String mainGroup) {
		super(thisServerName, thatServerName, playerId, playerName, mainGroup);
	}

	public EithonBungeeQuitEvent(String thisServerName, String thatServerName,
			Player player, String mainGroup) {
		super(thisServerName, thatServerName, player, mainGroup);
	}

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
