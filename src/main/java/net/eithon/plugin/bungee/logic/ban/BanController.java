package net.eithon.plugin.bungee.logic.ban;

import java.time.LocalDateTime;
import java.util.UUID;

import net.eithon.library.facades.PermissionsFacade;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.bungee.Config;
import net.eithon.plugin.bungee.EithonBungeeApi;
import net.eithon.plugin.bungee.EithonBungeePlugin;
import net.eithon.plugin.bungee.db.DbServerBan;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BanController {

	private final EithonBungeePlugin _eithonPlugin;

	public BanController(EithonBungeePlugin eithonPlugin) {
		this._eithonPlugin = eithonPlugin;
	}

	public void banPlayerOnThisServerAsync(
			final CommandSender sender, 
			final OfflinePlayer player, 
			final long seconds) {
		banPlayerAsync(sender, player, Config.V.thisBungeeServerName, seconds);
	}

	public void banPlayerAsync(
			final CommandSender sender, 
			final OfflinePlayer player, 
			final String serverName,
			final long seconds) {
		String permission = String.format("-eithonbungee.access.server.%s", serverName);
		verbose("banPlayerOnThisServerAsync", "Player %s, add permission %s", player.getName(), permission);
		PermissionsFacade.addPlayerPermissionAsync(player, permission);
		banPlayerAsync(sender, player, serverName, LocalDateTime.now().plusSeconds(seconds));
	}

	public void banPlayerAsync(
			final CommandSender sender, 
			final OfflinePlayer player, 
			final String serverName,
			LocalDateTime unbanAt) {
		new BukkitRunnable() {
			@Override
			public void run() {
				banPlayer(sender, player, serverName, unbanAt);
			}
		}
		.runTaskAsynchronously(this._eithonPlugin);	
	}

	private void banPlayer(
			final CommandSender sender, 
			final OfflinePlayer player, 
			final String serverName, 
			LocalDateTime unbanAt) {
		verbose("banPlayerOnThisServer", "Player %s, unban at %s", player.getName(), unbanAt.toString());
		final UUID playerId = player.getUniqueId();
		DbServerBan dbServerBan = DbServerBan.get(Config.V.database, playerId, serverName);
		if (dbServerBan == null) {
			verbose("banPlayerOnThisServer", "Create record");
			dbServerBan = DbServerBan.create(Config.V.database, playerId, player.getName(), serverName, unbanAt);
		} else {
			if ((unbanAt != null)
					&& !unbanAt.isAfter(dbServerBan.getUnbanAt())) {
				dbServerBan.updateUnbanAt(unbanAt);
			} else unbanAt = dbServerBan.getUnbanAt();
		}
		if (sender == null) return;
		Config.M.bannedPlayer.sendMessage(sender, player.getName(), TimeMisc.fromLocalDateTime(unbanAt));
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
		final EithonBungeeApi api = this._eithonPlugin.getApi();
		new BukkitRunnable() {
			@Override
			public void run() {
				api.connectPlayerToServerOrInformSender(null, player, Config.V.primaryBungeeServer);
			}
		}
		.runTask(this._eithonPlugin);
		return true;
	}

	public boolean isPlayerBannedOnThisServer(final Player player) {
		verbose("isPlayerBannedOnThisServer", "Player %s", player.getName());
		final DbServerBan dbServerBan = DbServerBan.get(Config.V.database, player.getUniqueId(), Config.V.thisBungeeServerName);
		if (dbServerBan == null) return false;
		if (dbServerBan.getUnbanAt().isAfter(LocalDateTime.now())) return true;
		unbanPlayer(null, player, Config.V.thisBungeeServerName);
		return false;
	}

	public void unbanPlayerAsync(final CommandSender sender, final OfflinePlayer player, String serverName) {
		new BukkitRunnable() {
			@Override
			public void run() {
				unbanPlayer(sender, player, serverName);
			}
		}
		.runTaskAsynchronously(this._eithonPlugin);
	}

	public void unbanPlayer(final CommandSender sender, final OfflinePlayer player, String serverName) {
		final DbServerBan dbServerBan = DbServerBan.get(Config.V.database, player.getUniqueId(), serverName);
		if (dbServerBan == null) {
			if (sender == null) return;
			Config.M.playerNotBanned.sendMessage(sender, player.getName(), serverName);
			return;
		}
		dbServerBan.delete();
		String permission = String.format("-eithonbungee.access.server.%s", serverName);
		verbose("banPlayerOnThisServerAsync", "Player %s, remove permission %s", player.getName(), permission);
		PermissionsFacade.removePlayerPermissionAsync(player, permission);
		Config.M.unbannedPlayer.sendMessage(sender, player.getName(), serverName);
	}

	public void listBannedPlayersAsync(CommandSender sender) {
		for (DbServerBan dbServerBan : DbServerBan.findAll(Config.V.database)) {
			sender.sendMessage(String.format("%s: %s (%s)",
					dbServerBan.getPlayerName(), dbServerBan.getBungeeServerName(), 
					TimeMisc.fromLocalDateTime(dbServerBan.getUnbanAt())));
		}
	}

	private void verbose(String method, String format, Object... args) {
		this._eithonPlugin.dbgVerbose("BanController", method, format, args);	
	}
}
