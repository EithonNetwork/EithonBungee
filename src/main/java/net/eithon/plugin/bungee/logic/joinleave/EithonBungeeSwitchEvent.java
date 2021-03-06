package net.eithon.plugin.bungee.logic.joinleave;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class EithonBungeeSwitchEvent extends EithonBungeeJoinLeaveEvent {
	private static final HandlerList handlers = new HandlerList();
	private String _previousServerName;

	public EithonBungeeSwitchEvent(String thisServerName, String fromServerName, String toServerName, UUID playerId, String playerName, String mainGroup) {
		super(thisServerName, toServerName, playerId, playerName, mainGroup);
		this._previousServerName = fromServerName;
	}

	public EithonBungeeSwitchEvent(String thisServerName, String thatServerName, Player player, String mainGroup) {
		super(thisServerName, thatServerName, player, mainGroup);
	}

	public String getPreviousServerName() { return this._previousServerName; }

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
