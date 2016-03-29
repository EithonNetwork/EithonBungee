package net.eithon.plugin.bungee.logic;

import net.eithon.library.extensions.EithonPlugin;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Controller {
	private TeleportController _teleportController;

	public Controller(EithonPlugin eithonPlugin) {
		this._teleportController = new TeleportController(eithonPlugin);
	}

	public void forcedTpToPlayer(Player sourcePlayer, OfflinePlayer targetPlayer) {
		this._teleportController.tpToPlayer(sourcePlayer, sourcePlayer, targetPlayer, true);
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
