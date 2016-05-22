package net.eithon.plugin.bungee.logic.ban;

import java.time.LocalDateTime;
import java.util.UUID;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.facades.PermissionsFacade;
import net.eithon.plugin.bungee.Config;
import net.eithon.plugin.bungee.db.DbServerBan;
import net.eithon.plugin.bungee.logic.Controller;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BanController {

	private final EithonPlugin _eithonPlugin;
	private final Controller _controller;
	private final String _serverName;

	public BanController(EithonPlugin eithonPlugin, String serverName, Controller controller) {
		this._eithonPlugin = eithonPlugin;
		this._controller = controller;
		this._serverName = serverName;
	}
	
	public void banPlayerOnThisServerAsync(final OfflinePlayer player, LocalDateTime unbanAt) {
		new BukkitRunnable() {
			@Override
			public void run() {
				banPlayerOnThisServer(player, unbanAt);
			}
		}
		.runTaskAsynchronously(this._eithonPlugin);	
	}
	
	private void banPlayerOnThisServer(final OfflinePlayer player, final LocalDateTime unbanAt) {
		final UUID playerId = player.getUniqueId();
		DbServerBan dbServerBan = DbServerBan.get(Config.V.database, playerId, this._serverName);
		if (dbServerBan == null) {
			dbServerBan = DbServerBan.create(Config.V.database, playerId, player.getName(), this._serverName, unbanAt);
		} else {
			if ((unbanAt != null)
					&& !unbanAt.isAfter(dbServerBan.getUnbanAt())) {
				dbServerBan.updateUnbanAt(unbanAt);
			}
		}
	}

	public void banPlayerOnThisServerAsync(final Player player, final long seconds) {
		String permission = String.format("-eithonbungee.access.server.%s", this._serverName);
		PermissionsFacade.addPlayerPermissionAsync(player, permission);
		banPlayerOnThisServerAsync(player, LocalDateTime.now().plusSeconds(seconds));
	}

	public void takeActionIfPlayerIsBannedOnThisServerAsync(final Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				takeActionIfPlayerIsBannedOnThisServer(player);
			}
		}
		.runTaskAsynchronously(this._eithonPlugin);
	}

	public boolean takeActionIfPlayerIsBannedOnThisServer(final Player player) {
		if (!isPlayerBannedOnThisServer(player)) return false;
		final Controller controller = this._controller;
		new BukkitRunnable() {
			@Override
			public void run() {
				controller.connectPlayerToServer(player, Config.V.primaryBungeeServer);
			}
		}
		.runTask(this._eithonPlugin);
		return true;
	}

	public boolean isPlayerBannedOnThisServer(final Player player) {
		final DbServerBan dbServerBan = DbServerBan.get(Config.V.database, player.getUniqueId(), this._serverName);
		if (dbServerBan == null) return false;
		if (dbServerBan.getUnbanAt().isBefore(LocalDateTime.now())) return true;
		dbServerBan.delete();
		String permission = String.format("-eithonbungee.access.server.%s", this._serverName);
		PermissionsFacade.removePlayerPermissionAsync(player, permission);
		return false;
	}

}
