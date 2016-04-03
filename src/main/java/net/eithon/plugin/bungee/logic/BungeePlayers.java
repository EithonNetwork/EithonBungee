package net.eithon.plugin.bungee.logic;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitScheduler;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.core.PlayerCollection;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;

public class BungeePlayers {
	private PlayerCollection<BungeePlayer> _bungeePlayers;
	private EithonPlugin _eithonPlugin;

	public BungeePlayers(EithonPlugin eithonPlugin) {
		this._eithonPlugin = eithonPlugin;
		this._bungeePlayers = new PlayerCollection<BungeePlayer>();
		delayedRefresh();
	}

	private void delayedRefresh() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				refresh();
			}
		}, TimeMisc.secondsToTicks(1));
	}

	private void refresh() {
		synchronized(this._bungeePlayers) {
			this._bungeePlayers.clear();
			for (BungeePlayer bungeePlayer : BungeePlayer.findAll()) {
				this._bungeePlayers.put(bungeePlayer.getOfflinePlayer(), bungeePlayer);
			};
		}
	}

	public void put(EithonPlayer player, String thatServerName) {
		verbose("put", "Player = %s, BungeeServer=%s", player.getName(), thatServerName);
		BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayer(player.getOfflinePlayer());
		if ((bungeePlayer == null) || !bungeePlayer.getBungeeServerName().equalsIgnoreCase(thatServerName)) {
			verbose("remove", "Server name in DB = %s. Will refresh.", 
					bungeePlayer == null? "Null" : bungeePlayer.getBungeeServerName());
			delayedRefresh();
			return;
		}
		verbose("put", "Stored");
		synchronized(this._bungeePlayers) {
			this._bungeePlayers.put(player, bungeePlayer);
		}
	}

	public void remove(EithonPlayer player, String thatServerName) {
		verbose("remove", "Player = %s, BungeeServer=%s", player.getName(), thatServerName);
		synchronized(this._bungeePlayers) {
			BungeePlayer bungeePlayer = this._bungeePlayers.get(player);
			if ((bungeePlayer == null) || !bungeePlayer.getBungeeServerName().equalsIgnoreCase(thatServerName)) {
				verbose("remove", "Server name in cache = %s. Will refresh.", 
						bungeePlayer == null? "Null" : bungeePlayer.getBungeeServerName());
				delayedRefresh();
				return;
			}
			verbose("remove", "Removed");
			this._bungeePlayers.remove(player);
		}
	}

	public List<String> getNames() {
		synchronized(this._bungeePlayers) {
			return this._bungeePlayers.values()
					.stream()
					.map(bp -> bp.getOfflinePlayer().getName())
					.collect(Collectors.toList());
		}
	}

	public BungeePlayer getBungeePlayer(OfflinePlayer player) {
		verbose("getBungeePlayer", "Player = %s", player.getName());
		BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayer(player);
		BungeePlayer cachedBungeePlayer = null;
		synchronized(this._bungeePlayers) {
			cachedBungeePlayer = this._bungeePlayers.get(player);
		}
		if (bungeePlayer == null) {
			verbose("getBungeePlayer", "Cached server name differ from name int database. Will refresh.");
			if (cachedBungeePlayer != null) delayedRefresh();
			verbose("getBungeePlayer", "Null");
			return null;
		}
		if ((cachedBungeePlayer == null) 
				|| !cachedBungeePlayer.getBungeeServerName().equalsIgnoreCase(bungeePlayer.getBungeeServerName())) {
			verbose("getBungeePlayer", "Cached server name differ from name int database. Will refresh.");
			delayedRefresh();
		}
		verbose("getBungeePlayer", "Found on server %s", bungeePlayer.getBungeeServerName());
		return bungeePlayer;
	}

	void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "BungeePlayers.%s: %s", method, message);
	}
}
