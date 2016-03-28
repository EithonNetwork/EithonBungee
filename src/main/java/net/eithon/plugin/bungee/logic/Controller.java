package net.eithon.plugin.bungee.logic;

import net.eithon.library.extensions.EithonPlugin;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Controller {
	private EithonPlugin _eithonPlugin;
	private TeleportController _teleportController;

	public Controller(EithonPlugin eithonPlugin) {
		this._eithonPlugin = eithonPlugin;
		this._teleportController = new TeleportController(eithonPlugin);
	}

	public void forcedTpToPlayer(Player sourcePlayer, OfflinePlayer targetPlayer) {
		this._teleportController.forcedTpToPlayer(sourcePlayer, targetPlayer);
	}
	
	public void prepareTeleport(TeleportToPlayerPojo info) {
		this._teleportController.prepareTeleport(info);
	}

	public void playerJoined(Player player) {
		this._teleportController.playerJoined(player);
	}

	public void playerQuitted(Player player) {
		this._teleportController.playerQuitted(player);
	}
}
