package net.eithon.plugin.bungee.logic.ban;

import java.time.LocalDateTime;
import java.util.UUID;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.facades.PermissionsFacade;
import net.eithon.library.mysql.Database;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.bungee.Config;
import net.eithon.plugin.bungee.EithonBungeeApi;
import net.eithon.plugin.bungee.EithonBungeePlugin;
import net.eithon.plugin.bungee.db.ServerBanTable;
import net.eithon.plugin.bungee.db.ServerBanRow;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BanController {

	private final EithonBungeePlugin _eithonPlugin;
	private ServerBanTable serverBanLogic;

	public BanController(EithonBungeePlugin eithonPlugin, Database database) throws FatalException {
		this._eithonPlugin = eithonPlugin;
		this.serverBanLogic = new ServerBanTable(database);
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
				try {
					banPlayer(sender, player, serverName, unbanAt);
				} catch (FatalException | TryAgainException e) {
					e.printStackTrace();
				}
			}
		}
		.runTaskAsynchronously(this._eithonPlugin);	
	}

	private void banPlayer(
			final CommandSender sender, 
			final OfflinePlayer player, 
			final String serverName, 
			LocalDateTime unbanAt) throws FatalException, TryAgainException {
		verbose("banPlayerOnThisServer", "Player %s, unban at %s", player.getName(), unbanAt.toString());
		final UUID playerId = player.getUniqueId();
		ServerBan serverBan = ServerBan.createFromRow(serverBanLogic.get(playerId, serverName));
		if (serverBan == null) {
			verbose("banPlayerOnThisServer", "Create record");
			serverBan = ServerBan.createFromRow(serverBanLogic.create(playerId, player.getName(), serverName, unbanAt));
		} else {
			if ((unbanAt != null)
					&& !unbanAt.isAfter(serverBan.getUnbanAt())) {
				updateUnbanAt(serverBan, unbanAt);
			} else unbanAt = serverBan.getUnbanAt();
		}
		if (sender == null) return;
		Config.M.bannedPlayer.sendMessage(sender, player.getName(), TimeMisc.fromLocalDateTime(unbanAt));
	}

	private void updateUnbanAt(ServerBan serverBan, LocalDateTime unbanAt) throws FatalException, TryAgainException {
		serverBan.setUnbanAt(unbanAt);
		serverBanLogic.update(serverBan.toRow());
	}

	public void takeActionIfPlayerIsBannedOnThisServerAsync(final Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					takeActionIfPlayerIsBannedOnThisServer(player);
				} catch (FatalException | TryAgainException e) {
					e.printStackTrace();
				}
			}
		}
		.runTaskAsynchronously(this._eithonPlugin);
	}

	public boolean takeActionIfPlayerIsBannedOnThisServer(final Player player) throws FatalException, TryAgainException {
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

	public boolean isPlayerBannedOnThisServer(final Player player) throws FatalException, TryAgainException {
		verbose("isPlayerBannedOnThisServer", "Player %s", player.getName());
		final ServerBanRow row = serverBanLogic.get(player.getUniqueId(), Config.V.thisBungeeServerName);
		final ServerBan serverBan = ServerBan.createFromRow(row);
		if (serverBan == null) return false;
		if (serverBan.getUnbanAt().isAfter(LocalDateTime.now())) return true;
		unbanPlayer(null, player, Config.V.thisBungeeServerName);
		return false;
	}

	public void unbanPlayerAsync(final CommandSender sender, final OfflinePlayer player, String serverName) {
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					unbanPlayer(sender, player, serverName);
				} catch (FatalException | TryAgainException e) {
					e.printStackTrace();
				}
			}
		}
		.runTaskAsynchronously(this._eithonPlugin);
	}

	public void unbanPlayer(final CommandSender sender, final OfflinePlayer player, String serverName) throws FatalException, TryAgainException {
		final ServerBanRow row = serverBanLogic.get(player.getUniqueId(), serverName);
		final ServerBan serverBan = ServerBan.createFromRow(row);
		if (serverBan == null) {
			if (sender == null) return;
			Config.M.playerNotBanned.sendMessage(sender, player.getName(), serverName);
			return;
		}
		serverBanLogic.delete(serverBan.getId());
		String permission = String.format("-eithonbungee.access.server.%s", serverName);
		verbose("banPlayerOnThisServerAsync", "Player %s, remove permission %s", player.getName(), permission);
		PermissionsFacade.removePlayerPermissionAsync(player, permission);
		Config.M.unbannedPlayer.sendMessage(sender, player.getName(), serverName);
	}

	public void listBannedPlayersAsync(CommandSender sender) {
		try {
			for (ServerBanRow row : serverBanLogic.findAll()) {
				ServerBan serverBan = ServerBan.createFromRow(row);
				sender.sendMessage(String.format("%s: %s (%s)",
						serverBan.getPlayerName(), serverBan.getBungeeServerName(), 
						TimeMisc.fromLocalDateTime(serverBan.getUnbanAt())));
			}
		} catch (FatalException | TryAgainException e) {
			e.printStackTrace();
		}
	}

	private void verbose(String method, String format, Object... args) {
		this._eithonPlugin.dbgVerbose("BanController", method, format, args);	
	}
}
