package net.eithon.plugin.bungee.logic.players;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.eithon.library.core.PlayerCollection;
import net.eithon.plugin.bungee.Config;
import net.eithon.plugin.bungee.EithonBungeePlugin;
import net.eithon.plugin.bungee.db.DbPlayer;
import net.eithon.plugin.bungee.logic.bungeecord.BungeeController;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BungeePlayerController {
	public static final String BUNGEE_PLAYER_REFRESH = "BungeePlayerRefresh";
	private PlayerCollection<BungeePlayer> _allCurrentPlayers;
	private EithonBungeePlugin _eithonPlugin;
	private boolean _refreshIsRunning = false;
	private BungeeController _bungeeController;

	public BungeePlayerController(EithonBungeePlugin eithonPlugin, BungeeController bungeeController) {
		this._eithonPlugin = eithonPlugin;
		this._bungeeController = bungeeController;
		this._allCurrentPlayers = new PlayerCollection<BungeePlayer>();
		refreshAsync();
	}

	public void purgePlayers() {
		DbPlayer.deleteByServerName(Config.V.database, Config.V.thisBungeeServerName);
	}

	public void refreshAsync() {
		if (this._refreshIsRunning) return;
		final BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				refresh();
			}
		};
		runnable.runTaskAsynchronously(this._eithonPlugin);
	}

	private void refresh() {
		verbose("refresh", "Enter");
		synchronized(this._allCurrentPlayers) {
			if (this._refreshIsRunning) return;
			boolean refreshServers = false;
			try {
				this._refreshIsRunning = true;
				final List<BungeePlayer> allBungeePlayers = BungeePlayer.findAll(false);
				this._allCurrentPlayers.clear();
				for (BungeePlayer bungeePlayer : allBungeePlayers) {
					final String playerName = bungeePlayer.getPlayerName();
					if (bungeePlayer.maybeDelete(Config.V.thisBungeeServerName)) {
						verbose("refresh", "Removed player %s, server %s", 
								playerName, Config.V.thisBungeeServerName);
						refreshServers = true;
						continue;
					}
					final String currentBungeeServerName = bungeePlayer.getCurrentBungeeServerName();
					if ((currentBungeeServerName != null) 
							&& this._eithonPlugin.getApi().serverHeartIsBeating(currentBungeeServerName)) {
						this._allCurrentPlayers.put(bungeePlayer.getPlayerId(), bungeePlayer);
						verbose("refresh", "Added player %s, server %s", 
								playerName, currentBungeeServerName);
					}
				}
				for (Player player : Bukkit.getOnlinePlayers()) {
					BungeePlayer bungeePlayer = BungeePlayer.createOrUpdate(player, Config.V.thisBungeeServerName);
					if (this._allCurrentPlayers.hasInformation(player)) continue;
					this._allCurrentPlayers.put(bungeePlayer.getPlayerId(), bungeePlayer);
					verbose("refresh", "Added player %s, current server", 
							bungeePlayer.getPlayerName(), bungeePlayer.getCurrentBungeeServerName());
					refreshServers = true;			
				}
				if (refreshServers) broadcastRefresh();
			} finally {
				this._refreshIsRunning = false;
			}
		}
		verbose("refresh", "Leave");
	}

	public void addPlayerOnThisServerAsync(final Player player) {
		final BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				addPlayerOnThisServer(player);
			}
		};
		runnable.runTaskAsynchronously(this._eithonPlugin);
	}

	private void addPlayerOnThisServer(final Player player) {
		verbose("addPlayerOnThisServer", "player=%s, Local bungeeServerName=%s",
				player.getName(), Config.V.thisBungeeServerName);
		synchronized(this._allCurrentPlayers) {
			final BungeePlayer bungeePlayer = BungeePlayer.createOrUpdate(player, Config.V.thisBungeeServerName);
			if (bungeePlayer == null) {
				this._eithonPlugin.logError("BungePlayerController.addPlayerOnThisServer: " +
						String.format("Could not create a bungee player record for player %s.", player.getName()));
				return;
			}
			this._allCurrentPlayers.put(player, bungeePlayer);
		}
	}

	public void bungeePlayerAddedOnOtherServerAsync(
			final UUID playerId, 
			final String playerName, 
			final String otherServerName) {
		final BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				bungeePlayerAddedOnOtherServer(playerId, playerName, otherServerName);
			}
		};
		runnable.runTaskAsynchronously(this._eithonPlugin);
	}

	private void bungeePlayerAddedOnOtherServer(
			final UUID playerId, 
			final String playerName, 
			final String otherServerName) {
		synchronized(this._allCurrentPlayers) {
			final BungeePlayer bungeePlayer = BungeePlayer.getByPlayerId(playerId);
			if (bungeePlayer == null) return;
			final String currentBungeeServerName = bungeePlayer.getCurrentBungeeServerName();
			if (!otherServerName.equalsIgnoreCase(currentBungeeServerName)) {
				this._eithonPlugin.logError(
						"BungeePlayers.addBungeePlayer(%s,%s): Server name in DB = %s. Will use DB value.",
						playerName, otherServerName,
						bungeePlayer == null? "NULL" : currentBungeeServerName);
			}
			this._allCurrentPlayers.put(playerId, bungeePlayer);
		}
	}

	public void removePlayerAsync(
			final UUID playerId,
			final String playerName,
			final String otherServerName) {
		final BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				removeBungeePlayer(playerId, playerName, otherServerName);
			}
		};
		runnable.runTaskAsynchronously(this._eithonPlugin);
	}

	private void removeBungeePlayer(
			final UUID playerId,
			final String playerName, 
			final String otherServerName) {
		synchronized(this._allCurrentPlayers) {
			BungeePlayer bungeePlayer = this._allCurrentPlayers.get(playerId);
			boolean found = bungeePlayer != null;
			if (!found) {
				bungeePlayer = BungeePlayer.getByPlayerId(playerId);
				if (bungeePlayer == null) return;
			} else bungeePlayer.refresh();
			final String currentBungeeServerName = bungeePlayer.getCurrentBungeeServerName();
			if (((currentBungeeServerName != null) 
					&& !currentBungeeServerName.equalsIgnoreCase(otherServerName))) {
				// Join/leave probably out of sync. Update instead of remove.
				this._eithonPlugin.logWarn(
						"BungeePlayers.removeBungeePlayer(%s,%s): Server name in DB = %s. Will add/update instead of remove.",
						playerName, otherServerName,
						bungeePlayer == null? "NULL" : currentBungeeServerName);
				this._allCurrentPlayers.put(playerId, bungeePlayer);
			} else {
				if (found) this._allCurrentPlayers.remove(playerId);
				bungeePlayer.maybeDelete(Config.V.thisBungeeServerName);
			}
		}
	}

	public List<String> getNames() {
		synchronized(this._allCurrentPlayers) {
			return this._allCurrentPlayers.values()
					.stream()
					.map(bp -> bp.getPlayerName())
					.filter(n -> (n != null))
					.collect(Collectors.toList());
		}
	}

	public BungeePlayer getBungeePlayerOrInformSender(CommandSender sender, OfflinePlayer player) {
		BungeePlayer bungeePlayer = getBungeePlayer(player);
		if (bungeePlayer != null) return bungeePlayer;
		if (sender != null) sender.sendMessage(String.format("Could not find player %s on any server.", player.getName()));
		return null;
	}

	private BungeePlayer getBungeePlayer(OfflinePlayer player) {
		verbose("getBungeePlayer", "Player = %s", player.getName());
		BungeePlayer cachedBungeePlayer = null;
		synchronized(this._allCurrentPlayers) {
			cachedBungeePlayer = this._allCurrentPlayers.get(player);
			if (cachedBungeePlayer != null) {
				verbose("getBungeePlayer", cachedBungeePlayer.toString());
				return cachedBungeePlayer;
			}
			BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayer(player);
			if (bungeePlayer == null) return null;
			this._allCurrentPlayers.put(player, bungeePlayer);
			verbose("getBungeePlayer", bungeePlayer.toString());
			return bungeePlayer;
		}
	}

	public String getCurrentBungeeServerNameOrInformSender(CommandSender sender, OfflinePlayer player) {
		BungeePlayer bungeePlayer = getBungeePlayerOrInformSender(sender, player);
		if (bungeePlayer == null) return null;
		final String currentBungeeServerName = bungeePlayer.getCurrentBungeeServerName();
		if (currentBungeeServerName == null) {
			if (sender != null) sender.sendMessage(String.format("Could not find player %s on any server.", player.getName()));
			return null;
		}
		return currentBungeeServerName;
	}

	public String getCurrentBungeeServerName(OfflinePlayer player) {
		BungeePlayer bungeePlayer = getBungeePlayer(player);
		if (bungeePlayer == null) return null;
		return bungeePlayer.getCurrentBungeeServerName();		
	}

	public String getCurrentBungeeServerName(UUID playerId) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
		if (player == null) return null;
		return getCurrentBungeeServerName(player);		
	}

	public String getPreviousBungeeServerName(OfflinePlayer player) {
		if (player == null) return null;
		BungeePlayer bungeePlayer = getBungeePlayer(player);
		if (bungeePlayer == null) return null;
		return bungeePlayer.getPreviousBungeeServerName();		
	}

	public String getAnyBungeeServerName(OfflinePlayer player) {
		if (player == null) return null;
		BungeePlayer bungeePlayer = getBungeePlayer(player);
		if (bungeePlayer == null) return null;
		return bungeePlayer.getAnyBungeeServerName();		
	}

	public String getPreviousBungeeServerName(UUID playerId) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
		if (player == null) return null;
		return getPreviousBungeeServerName(player);		
	}

	private void broadcastRefresh() {
		verbose("broadcastRefresh", "Enter");
		this._bungeeController.sendDataToAll(BUNGEE_PLAYER_REFRESH, null, true);
		verbose("broadcastRefresh", "Leave");
	}

	private void verbose(String method, String format, Object... args) {
		this._eithonPlugin.dbgVerbose("BungeePlayerController", method, format, args);	
	}
}
