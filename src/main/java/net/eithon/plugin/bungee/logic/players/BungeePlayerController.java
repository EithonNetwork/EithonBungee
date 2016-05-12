package net.eithon.plugin.bungee.logic.players;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.core.PlayerCollection;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.bungee.logic.bungeecord.BungeeController;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;

public class BungeePlayerController {
	public static final String BUNGEE_PLAYER = "BungeePlayer";
	public static final String BUNGEE_PLAYER_REFRESH = "BungeePlayerRefresh";
	private PlayerCollection<BungeePlayer> _allCurrentPlayers;
	private EithonPlugin _eithonPlugin;
	private String _bungeeServerName;
	private BungeeController _bungeeController;

	public BungeePlayerController(EithonPlugin eithonPlugin, BungeeController bungeeController) {
		this._eithonPlugin = eithonPlugin;
		this._bungeeController = bungeeController;
		this._bungeeServerName = null;
		this._allCurrentPlayers = new PlayerCollection<BungeePlayer>();
		delayedRefresh();
	}

	private void delayedRefresh() {
		final BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				refresh();
			}
		};
		runnable.runTaskLaterAsynchronously(this._eithonPlugin, TimeMisc.secondsToTicks(1));
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
		String thisBungeeServerName = getBungeeServerName();
		if (thisBungeeServerName == null) {
			delayedRefresh();
			return;
		}
		boolean refreshServers = false;
		synchronized(this._allCurrentPlayers) {
			this._allCurrentPlayers.clear();
			for (Player player : Bukkit.getOnlinePlayers()) {
				BungeePlayer.createOrUpdate(player, getBungeeServerName());
			}
			for (BungeePlayer bungeePlayer : BungeePlayer.findAll()) {
				boolean wasDeleted = deleteIfOffline(thisBungeeServerName, bungeePlayer);
				if (wasDeleted) {
					refreshServers = true;
					continue;
				}
				this._allCurrentPlayers.put(bungeePlayer.getPlayerId(), bungeePlayer);
				verbose("refresh", "Added player %s, server %s", 
						bungeePlayer.getPlayerName(), bungeePlayer.getBungeeServerName());
			}
		}
		if (refreshServers) broadcastRefresh();
		verbose("refresh", "Leave");
	}

	private boolean deleteIfOffline(String thisBungeeServerName, BungeePlayer bungeePlayer) {
		if (thisBungeeServerName == null || bungeePlayer.isOnline()) return false;
		boolean wasDeleted = bungeePlayer.deleteIfServerNameMatches(thisBungeeServerName);
		if (!wasDeleted) return false;
		verbose("refresh", "Removed player %s, server %s", 
				bungeePlayer.getPlayerName(), bungeePlayer.getBungeeServerName());
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
		verbose("addPlayerOnThisServer", "player=%s", player.getName());
		String bungeeServerName = getBungeeServerName();
		verbose("addPlayerOnThisServer", "Local bungeeServerName=%s", bungeeServerName);
		if (bungeeServerName == null) return;
		synchronized(this._allCurrentPlayers) {
			final BungeePlayer bungeePlayer = BungeePlayer.createOrUpdate(player, bungeeServerName);
			this._allCurrentPlayers.put(player, bungeePlayer);
			broadcastAddBungeePlayer(player);
		}
	}

	public void removePlayerOnThisServerAsync(final Player player) {
		final BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				removePlayerOnThisServer(player);
			}
		};
		runnable.runTaskAsynchronously(this._eithonPlugin);
	}

	private void removePlayerOnThisServer(final Player player) {
		verbose("removePlayerOnThisServer", "player=%s", player.getName());
		final BungeePlayer bungeePlayer;
		synchronized(this._allCurrentPlayers) {
			bungeePlayer = this._allCurrentPlayers.get(player);
			if (bungeePlayer == null) return;
			this._allCurrentPlayers.remove(player);
		}
		bungeePlayer.deleteIfServerNameMatches(getBungeeServerName());
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

	public BungeePlayer getBungeePlayer(OfflinePlayer player) {
		verbose("getBungeePlayer", "Player = %s", player.getName());
		BungeePlayer cachedBungeePlayer = null;
		synchronized(this._allCurrentPlayers) {
			cachedBungeePlayer = this._allCurrentPlayers.get(player);
			if (cachedBungeePlayer != null) {
				verbose("getBungeePlayer", "Found on server %s", cachedBungeePlayer.getBungeeServerName());
				return cachedBungeePlayer;
			}
		}
		BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayer(player);
		if (bungeePlayer == null) return null;
		this._allCurrentPlayers.put(player, bungeePlayer);
		verbose("getBungeePlayer", "Found on server %s", bungeePlayer.getBungeeServerName());
		return bungeePlayer;
	}

	public String getBungeeServerNameOrInformSender(CommandSender sender, OfflinePlayer player) {
		BungeePlayer bungeePlayer = getBungeePlayerOrInformSender(sender, player);
		if (bungeePlayer == null) return null;
		return bungeePlayer.getBungeeServerName();
	}

	public String getBungeeServerName(OfflinePlayer player) {
		BungeePlayer bungeePlayer = getBungeePlayer(player);
		if (bungeePlayer == null) return null;
		return bungeePlayer.getBungeeServerName();		
	}

	public String getBungeeServerName(UUID playerId) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
		if (player == null) return null;
		BungeePlayer bungeePlayer = getBungeePlayer(player);
		if (bungeePlayer == null) return null;
		return bungeePlayer.getBungeeServerName();		
	}

	private String getBungeeServerName() {
		if (this._bungeeServerName != null) return this._bungeeServerName;
		this._bungeeServerName = this._bungeeController.getBungeeServerName();
		return this._bungeeServerName;
	}

	private void broadcastRefresh() {
		verbose("broadcastRefresh", "Enter");
		this._bungeeController.sendDataToAll(BUNGEE_PLAYER_REFRESH, null, true);
		verbose("broadcastRefresh", "Leave");
	}

	private void broadcastAddBungeePlayer(Player player) {
		String bungeeServerName = getBungeeServerName();
		BungeePlayerPojo info = new BungeePlayerPojo(player, bungeeServerName);
		this._bungeeController.sendDataToAll(BUNGEE_PLAYER, info, true);
	}

	public void addBungeePlayerAsync(final JSONObject data) {
		final BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				addBungeePlayer(BungeePlayerPojo.getFromJson(data));
			}
		};
		runnable.runTaskAsynchronously(this._eithonPlugin);
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
			final BungeePlayer bungeePlayer = this._allCurrentPlayers.get(playerId);
			if (bungeePlayer == null)  return;
			if (!bungeePlayer.getBungeeServerName().equalsIgnoreCase(otherServerName)) {
				// Join/leave probably out of sync. Update instead of remove.
				this._eithonPlugin.getEithonLogger().warning(
						"BungeePlayers.removeBungeePlayer(%s,%s): Server name in DB = %s. Will add/update instead of remove.",
						playerName, otherServerName,
						bungeePlayer == null? "NULL" : bungeePlayer.getBungeeServerName());
				this._allCurrentPlayers.put(playerId, bungeePlayer);
			} else {
				this._allCurrentPlayers.remove(playerId);
			}
		}
	}

	private void addBungeePlayer(BungeePlayerPojo info) {
		final String otherServerName = info.getBungeeServerName();
		final BungeePlayer bungeePlayer = BungeePlayer.getByPlayerId(info.getPlayerId());
		if ((bungeePlayer == null) || !otherServerName.equalsIgnoreCase(bungeePlayer.getBungeeServerName())) {
			this._eithonPlugin.getEithonLogger().error(
					"BungeePlayers.addBungeePlayer(%s,%s): Server name in DB = %s. Will use DB value.",
					info.getPlayerName(), otherServerName,
					bungeePlayer == null? "NULL" : bungeePlayer.getBungeeServerName());
		}
		synchronized(this._allCurrentPlayers) {
			this._allCurrentPlayers.put(info.getPlayerId(), bungeePlayer);
		}
	}

	void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "BungeePlayerController.%s: %s", method, message);
	}
}
