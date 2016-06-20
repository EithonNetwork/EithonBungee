package net.eithon.plugin.bungee.logic.joinleave;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class EithonBungeeJoinEvent extends EithonBungeeJoinLeaveEvent {
	private static final HandlerList handlers = new HandlerList();
	private boolean isNewOnServer;
	private boolean firstJoinToday;

	public EithonBungeeJoinEvent(String thisServerName, String thatServerName, UUID playerId, String playerName, String mainGroup) {
		super(thisServerName, thatServerName, playerId, playerName, mainGroup);
		this.isNewOnServer = false;
		this.firstJoinToday = false;
	}

	public EithonBungeeJoinEvent(String thisServerName, String thatServerName, Player player, String mainGroup) {
		super(thisServerName, thatServerName, player, mainGroup);
	}

	public boolean getIsNewOnServer() { return this.isNewOnServer; }
	public boolean getIsFirstJoinToday() { return this.firstJoinToday; }
	public void setIsNewOnServer() { this.isNewOnServer = true; }
	public void setIsFirstJoinToday() { this.firstJoinToday = true; }

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	@Override
	public String toString() {
		return String.format("%s, isNewOnServer=%s, firstJoinToday=%s", 
				super.toString(),
				this.isNewOnServer ? "TRUE" : "FALSE", 
						this.firstJoinToday ? "TRUE" : "FALSE");
	}
}
