package net.eithon.plugin.bungee.logic.bungeecord;

import net.eithon.library.extensions.EithonPlayer;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class EithonBungeeQuitEvent extends EithonBungeeJoinQuitEvent {
	private static final HandlerList handlers = new HandlerList();

	public EithonBungeeQuitEvent(String thisServerName, String thatServerName, EithonPlayer player, String mainGroup) {
		super(thisServerName, thatServerName, player, mainGroup);
	}

	public EithonBungeeQuitEvent(String thisServerName, String thatServerName, Player player, String mainGroup) {
		this(thisServerName, thatServerName, new EithonPlayer(player), mainGroup);
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
