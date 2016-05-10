package net.eithon.plugin.bungee;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.bungee.logic.Controller;
import net.eithon.plugin.bungee.logic.bungeecord.EithonBungeeEvent;
import net.eithon.plugin.bungee.logic.bungeecord.EithonBungeeJoinEvent;
import net.eithon.plugin.bungee.logic.bungeecord.EithonBungeeQuitEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {
	private Controller _controller;
	
	public EventListener(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
	}

	// Handle teleport events
	@EventHandler
	public void onEithonBungeeEvent(EithonBungeeEvent event) {
		if (event.getName().equalsIgnoreCase("TeleportToPlayer")) {
			onBungeeTeleportToPlayer(event);
		}
		if (event.getName().equalsIgnoreCase("MessageToPlayer")) {
			onBungeeMessageToPlayer(event);
		}
	}

	private void onBungeeTeleportToPlayer(EithonBungeeEvent event) {
		this._controller.handleTeleportEvent(event.getData());
	}

	private void onBungeeMessageToPlayer(EithonBungeeEvent event) {
		this._controller.handleMessageEvent(event.getData());
	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		this._controller.playerJoined(event.getPlayer());
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		this._controller.playerLeft(player);
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerKickEvent(PlayerKickEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		this._controller.playerLeft(player);
	}

	// Player joined on any bungee server
	@EventHandler
	public void onEithonBungeeJoinEvent(EithonBungeeJoinEvent event) {
		this._controller.bungeePlayerJoined(event.getPlayer(), event.getThatServerName());
		
	}

	// Player quit on any bungee server
	@EventHandler
	public void onEithonBungeeQuitEvent(EithonBungeeQuitEvent event) {
		this._controller.bungeePlayerLeft(event.getPlayer(), event.getThatServerName());
	}
	
	
}
