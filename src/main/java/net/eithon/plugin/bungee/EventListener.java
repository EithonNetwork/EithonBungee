package net.eithon.plugin.bungee;

import net.eithon.library.bungee.EithonBungeeEvent;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.move.EithonPlayerMoveOneBlockEvent;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.bungee.logic.Controller;
import net.eithon.plugin.bungee.logic.TeleportToPlayerPojo;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {
	private EithonPlugin _eithonPlugin = null;
	private Controller _controller;

	public EventListener(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
		this._eithonPlugin = eithonPlugin;
	}

	private void debug(String method, String message) {
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "%s: %s", method, message);
	}

	@EventHandler
	public void onEithonBungeeEvent(EithonBungeeEvent event) {
		if (event.getName().equalsIgnoreCase("TeleportToPlayer")) {
			TeleportToPlayerPojo info = TeleportToPlayerPojo.createFromJsonObject(event.getData());
			this._controller.prepareTeleport(info);
		}
	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		this._controller.playerJoined(event.getPlayer());
	}

	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		this._controller.playerQuitted(player);
	}
	
	
}
