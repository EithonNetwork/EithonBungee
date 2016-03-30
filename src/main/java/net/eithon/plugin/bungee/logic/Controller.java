package net.eithon.plugin.bungee.logic;

import net.eithon.library.extensions.EithonPlugin;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Controller {
	private TeleportController _teleportController;

	public Controller(EithonPlugin eithonPlugin) {
		this._teleportController = new TeleportController(eithonPlugin);
	}

	public void requestTpToPlayer(Player movingPlayer, OfflinePlayer anchorPlayer) {
		this._teleportController.tpToPlayer(movingPlayer, movingPlayer, anchorPlayer, false);
	}

	public void forcedTpToPlayer(Player movingPlayer, OfflinePlayer anchorPlayer) {
		this._teleportController.tpToPlayer(movingPlayer, movingPlayer, anchorPlayer, true);
	}

	public void requestTpPlayerHere(Player movingPlayer, OfflinePlayer anchorPlayer) {
		this._teleportController.tpPlayerHere(movingPlayer, movingPlayer, anchorPlayer, false);
	}

	public void forcedTpPlayerHere(Player movingPlayer, OfflinePlayer anchorPlayer) {
		this._teleportController.tpPlayerHere(movingPlayer, movingPlayer, anchorPlayer, true);
	}

	public void tpDeny(Player localPlayer) {
		this._teleportController.deny(localPlayer, localPlayer);
	}

	public void tpAccept(Player localPlayer) {
		this._teleportController.accept(localPlayer, localPlayer);
	}
	
	public void handleTeleportEvent(TeleportToPlayerPojo info) {
		this._teleportController.handleTeleportEvent(info);
	}

	public void playerJoined(Player player) {
		this._teleportController.playerJoined(player);
	}

	public void playerQuitted(Player player) {
		this._teleportController.playerQuitted(player);
	}
}
