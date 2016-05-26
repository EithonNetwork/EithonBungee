package net.eithon.plugin.bungee.logic.joinleave;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class EithonBungeeJoinEvent extends EithonBungeeJoinLeaveEvent {
	private static final HandlerList handlers = new HandlerList();
	private boolean isNewOnServer;

	public EithonBungeeJoinEvent(String thisServerName, String thatServerName, UUID playerId, String playerName, String mainGroup, boolean isNewOnServer) {
		super(thisServerName, thatServerName, playerId, playerName, mainGroup);
		this.isNewOnServer = isNewOnServer;
	}

	public EithonBungeeJoinEvent(String thisServerName, String thatServerName, Player player, String mainGroup) {
		super(thisServerName, thatServerName, player, mainGroup);
	}

	public boolean getIsNewOnServer() { return this.isNewOnServer; }

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
