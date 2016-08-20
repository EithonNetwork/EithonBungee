package net.eithon.plugin.bungee.logic.players;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.eithon.library.core.PlayerCollection;
import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.bungee.Config;
import net.eithon.plugin.bungee.EithonBungeePlugin;
import net.eithon.plugin.bungee.db.PlayerController;
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
	private PlayerController playerController;

	public BungeePlayerController(EithonBungeePlugin eithonPlugin, BungeeController bungeeController, Database database) throws FatalException {
		this.playerController = new PlayerController(database);
		this._eithonPlugin = eithonPlugin;
		this._bungeeController = bungeeController;
		this._allCurrentPlayers = new PlayerCollection<BungeePlayer>();
		refreshAsync();
	}

	public void purgePlayers() throws FatalException, TryAgainException {
		playerController.deleteByServerName(Config.V.database, Config.V.thisBungeeServerName);
	}

	public void refreshAsync() {
		if (this._refreshIsRunning) return;
		final BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				try {
					refresh();
				} catch (FatalException | TryAgainException e) {
					e.printStackTrace();
				}
			}
		};
		runnable.runTaskAsynchronously(this._eithonPlugin);
	}

	private void refresh() throws FatalException, TryAgainException {
		verbose("refresh", "Enter");
		synchronized(this._allCurrentPlayers) {
			if (this._refreshIsRunning) return;
			boolean refreshServers = false;
			try {
				this._refreshIsRunning = true;
				final List<BungeePlayer> allBungeePlayers = 
						playerController.findAll()
						.stream()
						.map(row -> BungeePlayerMapper.rowToModel(row))
						.collect(Collectors.toList());
				this._allCurrentPlayers.clear();
				for (BungeePlayer bungeePlayer : allBungeePlayers) {
					final String playerName = bungeePlayer.getPlayerName();
					Player player = Bukkit.getPlayer(bungeePlayer.getPlayerId());
					if (player != null) continue;
					// Player is not online on this server
					if (bungeePlayer.isSameServer(Config.V.thisBungeeServerName)) {
						// This server was the last one noted for this player
						if (!bungeePlayer.isOnline()) {
							if (bungeePlayer.isOld()) {
								verbose("refresh", "Removed player %s, server %s", 
										playerName, Config.V.thisBungeeServerName);
								playerController.delete(bungeePlayer.getId());
								refreshServers = true;
							}
							continue;
						}
						verbose("refresh", "Updated player %s, not any longer on server %s", 
								playerName, Config.V.thisBungeeServerName);
						bungeePlayer.setLeftAt(LocalDateTime.now());
						playerController.update(BungeePlayerMapper.modelToRow(bungeePlayer));
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
					BungeePlayer bungeePlayer = createOrUpdateBungeePlayer(player);
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
				try {
					addPlayerOnThisServer(player);
				} catch (FatalException | TryAgainException e) {
					e.printStackTrace();
				}
			}
		};
		runnable.runTaskAsynchronously(this._eithonPlugin);
	}

	private void addPlayerOnThisServer(final Player player) throws FatalException, TryAgainException {
		verbose("addPlayerOnThisServer", "player=%s, Local bungeeServerName=%s",
				player.getName(), Config.V.thisBungeeServerName);
		synchronized(this._allCurrentPlayers) {
			final BungeePlayer bungeePlayer = createOrUpdateBungeePlayer(player);
			if (bungeePlayer == null) {
				this._eithonPlugin.logError("BungePlayerController.addPlayerOnThisServer: " +
						String.format("Could not create a bungee player record for player %s.", player.getName()));
				return;
			}
			this._allCurrentPlayers.put(player, bungeePlayer);
		}
	}

	private BungeePlayer createOrUpdateBungeePlayer(final Player player)
			throws FatalException, TryAgainException {
		return BungeePlayerMapper.rowToModel(
				playerController.createOrUpdate(player.getUniqueId(), player.getName(), Config.V.thisBungeeServerName));
	}

	public void bungeePlayerAddedOnOtherServerAsync(
			final UUID playerId, 
			final String playerName, 
			final String otherServerName) {
		final BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				try {
					bungeePlayerAddedOnOtherServer(playerId, playerName, otherServerName);
				} catch (FatalException | TryAgainException e) {
					e.printStackTrace();
				}
			}
		};
		runnable.runTaskAsynchronously(this._eithonPlugin);
	}

	private void bungeePlayerAddedOnOtherServer(
			final UUID playerId, 
			final String playerName, 
			final String otherServerName) throws FatalException, TryAgainException {
		synchronized(this._allCurrentPlayers) {
			final BungeePlayer bungeePlayer = getBungeePlayerByPlayerId(playerId);
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

	private BungeePlayer getBungeePlayerByPlayerId(final UUID playerId)
			throws FatalException, TryAgainException {
		return BungeePlayerMapper.rowToModel(
				playerController.getByPlayerId(playerId));
	}

	public void removePlayerAsync(
			final UUID playerId,
			final String playerName,
			final String otherServerName) {
		final BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				try {
					removeBungeePlayer(playerId, playerName, otherServerName);
				} catch (FatalException | TryAgainException e) {
					e.printStackTrace();
				}
			}
		};
		runnable.runTaskAsynchronously(this._eithonPlugin);
	}

	private void removeBungeePlayer(
			final UUID playerId,
			final String playerName, 
			final String otherServerName) throws FatalException, TryAgainException {
		synchronized(this._allCurrentPlayers) {
			BungeePlayer bungeePlayer = this._allCurrentPlayers.get(playerId);
			boolean found = bungeePlayer != null;
			if (!found) {
				bungeePlayer = getBungeePlayerByPlayerId(playerId);
				if (bungeePlayer == null) return;
			} else {
				bungeePlayer = getBungeePlayerByPlayerId(playerId);
			}
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
				if (bungeePlayer.isOnline()) {
					bungeePlayer.setLeftAt(LocalDateTime.now());
					playerController.update(BungeePlayerMapper.modelToRow(bungeePlayer));
				} else {
					if (bungeePlayer.isOld()) playerController.delete(bungeePlayer.getId());
				}
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

	public BungeePlayer getBungeePlayerOrInformSender(CommandSender sender, OfflinePlayer player) throws FatalException, TryAgainException {
		BungeePlayer bungeePlayer = getBungeePlayer(player);
		if (bungeePlayer != null) return bungeePlayer;
		if (sender != null) sender.sendMessage(String.format("Could not find player %s on any server.", player.getName()));
		return null;
	}

	private BungeePlayer getBungeePlayer(OfflinePlayer player) throws FatalException, TryAgainException {
		verbose("getBungeePlayer", "Player = %s", player.getName());
		BungeePlayer cachedBungeePlayer = null;
		synchronized(this._allCurrentPlayers) {
			cachedBungeePlayer = this._allCurrentPlayers.get(player);
			if (cachedBungeePlayer != null) {
				verbose("getBungeePlayer", cachedBungeePlayer.toString());
				return cachedBungeePlayer;
			}
			BungeePlayer bungeePlayer = getBungeePlayerByPlayerId(player.getUniqueId());
			if (bungeePlayer == null) return null;
			this._allCurrentPlayers.put(player, bungeePlayer);
			verbose("getBungeePlayer", bungeePlayer.toString());
			return bungeePlayer;
		}
	}

	public String getCurrentBungeeServerNameOrInformSender(CommandSender sender, OfflinePlayer player) throws FatalException, TryAgainException {
		BungeePlayer bungeePlayer = getBungeePlayerOrInformSender(sender, player);
		if (bungeePlayer == null) return null;
		final String currentBungeeServerName = bungeePlayer.getCurrentBungeeServerName();
		if (currentBungeeServerName == null) {
			if (sender != null) sender.sendMessage(String.format("Could not find player %s on any server.", player.getName()));
			return null;
		}
		return currentBungeeServerName;
	}

	public String getCurrentBungeeServerName(OfflinePlayer player) throws FatalException, TryAgainException {
		BungeePlayer bungeePlayer = getBungeePlayer(player);
		if (bungeePlayer == null) return null;
		return bungeePlayer.getCurrentBungeeServerName();		
	}

	public String getCurrentBungeeServerName(UUID playerId) throws FatalException, TryAgainException {
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
		if (player == null) return null;
		return getCurrentBungeeServerName(player);		
	}

	public String getPreviousBungeeServerName(OfflinePlayer player) throws FatalException, TryAgainException {
		if (player == null) return null;
		BungeePlayer bungeePlayer = getBungeePlayer(player);
		if (bungeePlayer == null) return null;
		return bungeePlayer.getPreviousBungeeServerName2();		
	}

	public String getAnyBungeeServerName(OfflinePlayer player) throws FatalException, TryAgainException {
		if (player == null) return null;
		BungeePlayer bungeePlayer = getBungeePlayer(player);
		if (bungeePlayer == null) return null;
		return bungeePlayer.getAnyBungeeServerName();		
	}

	public String getPreviousBungeeServerName(UUID playerId) throws FatalException, TryAgainException {
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
