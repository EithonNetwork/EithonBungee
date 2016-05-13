package net.eithon.plugin.bungee;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.bungee.logic.Controller;
import net.eithon.plugin.bungee.logic.bungeecord.EithonBungeeEvent;
import net.eithon.plugin.bungee.logic.joinleave.EithonBungeeJoinEvent;
import net.eithon.plugin.bungee.logic.joinleave.EithonBungeeLeaveEvent;
import net.eithon.plugin.bungee.logic.joinleave.JoinLeaveController;
import net.eithon.plugin.bungee.logic.players.BungeePlayerController;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {
	private Controller _controller;
	private EithonPlugin _eithonPlugin;
	
	public EventListener(EithonPlugin eithonPlugin, Controller controller) {
		this._eithonPlugin = eithonPlugin;
		this._controller = controller;
	}

	// Handle teleport events
	@EventHandler(ignoreCancelled=true)
	public void onEithonBungeeEvent(EithonBungeeEvent event) {
		if (event.getName().equalsIgnoreCase("TeleportToPlayer")) {
			this._controller.handleTeleportEvent(event.getData());
		}
		if (event.getName().equalsIgnoreCase("MessageToPlayer")) {
			this._controller.handleMessageEvent(event.getData());
		}
		if (event.getName().equalsIgnoreCase(BungeePlayerController.BUNGEE_PLAYER)) {
			this._controller.addBungeePlayer(event.getData());
		}
		if (event.getName().equalsIgnoreCase(JoinLeaveController.JOIN_EVENT)) {
			this._controller.publishJoinEventOnThisServer(event.getData());
		}
		if (event.getName().equalsIgnoreCase(JoinLeaveController.LEAVE_EVENT)) {
			this._controller.publishLeaveEventOnThisServer(event.getData());
		}
		if (event.getName().equalsIgnoreCase(BungeePlayerController.BUNGEE_PLAYER_REFRESH)) {
			this._controller.refreshBungeePlayer();
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		verbose("onPlayerJoinEvent", "Player=%s", player.getName());
		String joinMessage = this._controller.getJoinMessage(player);
		if (joinMessage != null) event.setJoinMessage(joinMessage);
		treatFirstTimeUsersSpecial(event, player);
		this._controller.playerJoined(event.getPlayer());
	}

	private boolean treatFirstTimeUsersSpecial(PlayerJoinEvent event, Player player) {
		if (player.hasPlayedBefore()) {
			return false;
		}
		event.setJoinMessage(Config.M.joinedServerFirstTime.getMessageWithColorCoding(player.getName()));
		Config.M.pleaseWelcomeNewPlayer.broadcastMessage(player.getName());
		return true;
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		String quitMessage = this._controller.getQuitMessage(player);
		if (quitMessage != null) event.setQuitMessage(quitMessage);
		this._controller.playerLeft(player);
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerKickEvent(PlayerKickEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		this._controller.playerLeft(player);
	}

	// Player joined on any bungee server
	@EventHandler(ignoreCancelled=true)
	public void onEithonBungeeJoinEvent(EithonBungeeJoinEvent event) {
		verbose("onEithonBungeeJoinEvent", "Player=%s", event.getPlayerName());
		if (event.getPlayerId() == null) return;
		this._controller.broadcastPlayerJoined(event.getThatServerName(), event.getPlayerId(), event.getPlayerName(), event.getMainGroup());		
	}

	// Player quit on any bungee server
	@EventHandler(ignoreCancelled=true)
	public void onEithonBungeeQuitEvent(EithonBungeeLeaveEvent event) {
		verbose("onEithonBungeeQuitEvent", "Player=%s", event.getPlayerName());
		if (event.getPlayerId() == null) return;
		this._controller.broadcastPlayerQuitted(event.getThatServerName(), event.getPlayerId(), event.getPlayerName(), event.getMainGroup());
		this._controller.removeBungeePlayer(event.getPlayerId(), event.getPlayerName(), event.getThatServerName());
	}
	
	void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "EventListener.%s: %s", method, message);
	}
}
