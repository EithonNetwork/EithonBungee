package net.eithon.plugin.bungee;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.EithonPublicMessageEvent;
import net.eithon.plugin.bungee.logic.Controller;
import net.eithon.plugin.bungee.logic.bungeecord.EithonBungeeEvent;
import net.eithon.plugin.bungee.logic.joinleave.EithonBungeeJoinEvent;
import net.eithon.plugin.bungee.logic.joinleave.EithonBungeeLeaveEvent;
import net.eithon.plugin.bungee.logic.joinleave.EithonBungeeSwitchEvent;
import net.eithon.plugin.bungee.logic.joinleave.JoinLeaveController;
import net.eithon.plugin.bungee.logic.players.BungeePlayerController;
import net.eithon.plugin.bungee.logic.teleport.TeleportController;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.json.simple.JSONObject;

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
		verbose("onEithonBungeeEvent", "event name=%s", event.getName());
		JSONObject data = event.getData();
		switch(event.getName()) {
		case Controller.HEARTBEAT:
			this._controller.handleHeartbeat(data);
			break;
		case TeleportController.TELEPORT_TO_PLAYER:
			try {
				this._controller.handleTeleportEvent(data);
			} catch (FatalException | TryAgainException e) {
				e.printStackTrace();
			}
			break;
		case Controller.MESSAGE_TO_PLAYER:
			this._controller.handleMessageEvent(data);
			break;
		case JoinLeaveController.JOIN_EVENT:
			this._controller.publishJoinEventOnThisServer(data);
			break;
		case JoinLeaveController.SWITCH_EVENT:
			this._controller.publishSwitchEventOnThisServer(data);
			break;
		case JoinLeaveController.LEAVE_EVENT:
			this._controller.publishLeaveEventOnThisServer(data);
			break;
		case BungeePlayerController.BUNGEE_PLAYER_REFRESH:
			this._controller.refreshBungeePlayer();
			break;
		case TeleportController.WARP_LOCATION_REFRESH:
			this._controller.refreshWarpLocations();
			break;
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		this._eithonPlugin.dbgMinor("onPlayerJoinEvent: Player=%s", player.getName());
		event.setJoinMessage("");
		this._controller.playerJoined(event.getPlayer());
	}

	@EventHandler
	public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		this._eithonPlugin.dbgMinor("onPlayerRespawnEvent: Player=%s", player.getName());
		this._controller.takeActionIfPlayerIsBannedOnThisServer(player);
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		this._eithonPlugin.dbgMinor("onPlayerQuitEvent: Player=%s", player.getName());
		event.setQuitMessage("");
		this._controller.playerLeftThisServer(player);
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerKickEvent(PlayerKickEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		this._eithonPlugin.dbgMinor("onPlayerKickEvent: Player=%s", player.getName());
		this._controller.playerLeftThisServer(player);
	}

	// Player joined a bungee server
	@EventHandler(ignoreCancelled=true)
	public void onEithonBungeeJoinEvent(EithonBungeeJoinEvent event) {
		final String playerName = event.getPlayerName();
		this._eithonPlugin.dbgMinor("onEithonBungeeJoinEvent: Player=%s", playerName);
		if (event.getPlayerId() == null) return;
		this._controller.playerJoinedOnOtherServer(event.getPlayerId(), playerName, event.getThatServerName());
		if (event.getIsNewOnServer() && this._controller.serverIsThePrimaryBungeeServer(event.getThatServerName())) {
			Config.M.joinedServerFirstTime.broadcastToThisServer(playerName);
			Config.M.pleaseWelcomeNewPlayer.broadcastToThisServer(playerName);			
		} else {
			this._controller.broadcastPlayerJoined(event.getThatServerName(), event.getPlayerId(), playerName, event.getMainGroup());
		}
	}

	// Player joined a bungee server
	@EventHandler(ignoreCancelled=true)
	public void onEithonBungeeSwitchEvent(EithonBungeeSwitchEvent event) {
		final String playerName = event.getPlayerName();
		this._eithonPlugin.dbgMinor("onEithonBungeeSwitchEvent: Player=%s", playerName);
		if (event.getPlayerId() == null) return;
		this._controller.playerJoinedOnOtherServer(event.getPlayerId(), event.getPlayerName(), event.getThatServerName());
		this._controller.broadcastPlayerSwitched(event.getPreviousServerName(), event.getThatServerName(), event.getPlayerId(), playerName, event.getMainGroup());
	}

	// Player quit on any bungee server
	@EventHandler(ignoreCancelled=true)
	public void onEithonBungeeLeaveEvent(EithonBungeeLeaveEvent event) {
		if (event.getPlayerId() == null) return;
		this._eithonPlugin.dbgMinor("onEithonBungeeLeaveEvent: Player=%s", event.getPlayerName());
		this._controller.eithonBungeeLeaveReceived(event.getThatServerName(), event.getPlayerId(), event.getPlayerName(), event.getMainGroup());
	}

	// Message to be broadcasted
	@EventHandler
	public void onEithonPublicMessageEvent(EithonPublicMessageEvent event) {
		this._eithonPlugin.dbgMinor("onEithonPublicMessageEvent: Player=%s", event.getMessage());
		this._controller.publicMessage(event.getMessage(), event.getUseTitle());
	}

	void verbose(String method, String format, Object... args) {
		this._eithonPlugin.dbgVerbose("EventListener", method, format, args);
	}
}
