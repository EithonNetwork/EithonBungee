package net.eithon.plugin.bungee.logic;

import net.eithon.library.extensions.EithonPlugin;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Controller {
	private EithonPlugin _eithonPlugin;

	public Controller(EithonPlugin eithonPlugin) {
		this._eithonPlugin = eithonPlugin;
	}

	public void forcedTpToPlayer(Player sourcePlayer, OfflinePlayer targetPlayer) {
		this._eithonPlugin.getEithonLogger().info("Source player: %s, arget player: %s", 
				sourcePlayer.getName(), targetPlayer.getName());
		if (targetPlayer.isOnline()) {
			forcedTpToOnlinePlayer(sourcePlayer, targetPlayer.getPlayer());
		}
	}

	private void forcedTpToOnlinePlayer(Player sourcePlayer, Player targetPlayer) {
		sourcePlayer.teleport(targetPlayer);
	}
}
