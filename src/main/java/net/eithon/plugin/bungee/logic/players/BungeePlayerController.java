package net.eithon.plugin.bungee.logic.players;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.core.PlayerCollection;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.bungee.logic.bungeecord.BungeeController;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;

public class BungeePlayerController {
	public static final String BUNGEE_PLAYER_ADDED = "BungeePlayerAdded";
	public static final String BUNGEE_PLAYER_REFRESH = "BungeePlayerRefresh";
	private PlayerCollection<BungeePlayer> _allCurrentPlayers;
	private EithonPlugin _eithonPlugin;
	private String _bungeeServerName;
	private BungeeController _bungeeController;

	public BungeePlayerController(EithonPlugin eithonPlugin, BungeeController bungeeController, String bungeeServerName) {
		this._eithonPlugin = eithonPlugin;
		this._bungeeController = bungeeController;
		this._bungeeServerName = bungeeServerName;
		this._allCurrentPlayers = new PlayerCollection<BungeePlayer>();
		refreshAsync();
	}

	public void refreshAsync() {
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
		boolean refreshServers = false;
		for (Player player : Bukkit.getOnlinePlayers()) {
			BungeePlayer.createOrUpdate(player, this._bungeeServerName);
		}
		synchronized(this._allCurrentPlayers) {
			this._allCurrentPlayers.clear();
			for (BungeePlayer bungeePlayer : BungeePlayer.findAll()) {
				boolean wasDeleted = maybeLeft(this._bungeeServerName, bungeePlayer);
				if (wasDeleted) {
					refreshServers = true;
					continue;
				}
				this._allCurrentPlayers.put(bungeePlayer.getPlayerId(), bungeePlayer);
				verbose("refresh", "Added player %s, server %s", 
						bungeePlayer.getPlayerName(), bungeePlayer.getCurrentBungeeServerName());
			}
		}
		if (refreshServers) broadcastRefresh();
		verbose("refresh", "Leave");
	}

	private boolean maybeLeft(String thisBungeeServerName, BungeePlayer bungeePlayer) {
		if (thisBungeeServerName == null || bungeePlayer.isOnlineOnThisServer()) return false;
		boolean wasUpdated = bungeePlayer.maybeLeft(thisBungeeServerName);
		if (!wasUpdated) return false;
		verbose("refresh", "Removed player %s, server %s", 
				bungeePlayer.getPlayerName(), bungeePlayer.getPreviousBungeeServerName());
		return true;
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
				player.getName(), this._bungeeServerName);
		synchronized(this._allCurrentPlayers) {
			final BungeePlayer bungeePlayer = BungeePlayer.createOrUpdate(player, this._bungeeServerName);
			if (bungeePlayer == null) {
				this._eithonPlugin.getEithonLogger().error("BungePlayerController.addPlayerOnThisServer: " +
						String.format("Could not create a bungee player record for player %s.", player.getName()));
				return;
			}
			this._allCurrentPlayers.put(player, bungeePlayer);
			publishBungeePlayerAdded(player);
		}
	}

	public void bungeePlayerAddedOnOtherServerAsync(final JSONObject data) {
		final BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				bungeePlayerAddedOnOtherServer(BungeePlayerPojo.getFromJson(data));
			}
		};
		runnable.runTaskAsynchronously(this._eithonPlugin);
	}

	private void bungeePlayerAddedOnOtherServer(BungeePlayerPojo info) {
		final String otherServerName = info.getBungeeServerName();
		synchronized(this._allCurrentPlayers) {
			final BungeePlayer bungeePlayer = BungeePlayer.getByPlayerId(info.getPlayerId());
			if (bungeePlayer == null) return;
			final String currentBungeeServerName = bungeePlayer.getCurrentBungeeServerName();
			if (!otherServerName.equalsIgnoreCase(currentBungeeServerName)) {
				this._eithonPlugin.getEithonLogger().error(
						"BungeePlayers.addBungeePlayer(%s,%s): Server name in DB = %s. Will use DB value.",
						info.getPlayerName(), otherServerName,
						bungeePlayer == null? "NULL" : currentBungeeServerName);
			}
			this._allCurrentPlayers.put(info.getPlayerId(), bungeePlayer);
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
			if (!otherServerName.equalsIgnoreCase(currentBungeeServerName)) {
				// Join/leave probably out of sync. Update instead of remove.
				this._eithonPlugin.getEithonLogger().warning(
						"BungeePlayers.removeBungeePlayer(%s,%s): Server name in DB = %s. Will add/update instead of remove.",
						playerName, otherServerName,
						bungeePlayer == null? "NULL" : currentBungeeServerName);
				this._allCurrentPlayers.put(playerId, bungeePlayer);
			} else {
				if (found) this._allCurrentPlayers.remove(playerId);
				bungeePlayer.maybeLeft(this._bungeeServerName);
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

	private void publishBungeePlayerAdded(Player player) {
		BungeePlayerPojo info = new BungeePlayerPojo(player, this._bungeeServerName);
		this._bungeeController.sendDataToAll(BUNGEE_PLAYER_ADDED, info, true);
	}

	void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "BungeePlayerController.%s: %s", method, message);
	}
}
