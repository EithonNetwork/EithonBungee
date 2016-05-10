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
import org.bukkit.scheduler.BukkitScheduler;

public class BungeePlayers {
	private PlayerCollection<BungeePlayer> _allCurrentPlayers;
	private EithonPlugin _eithonPlugin;
	private String _bungeeServerName;
	private int _localPlayers;
	private BungeeController _bungeeController;

	public BungeePlayers(EithonPlugin eithonPlugin, BungeeController bungeeController) {
		this._eithonPlugin = eithonPlugin;
		this._bungeeController = bungeeController;
		this._bungeeServerName = null;
		this._localPlayers = 0;
		this._allCurrentPlayers = new PlayerCollection<BungeePlayer>();
		delayedRefresh();
	}

	void delayedRefresh() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				refresh();
			}
		}, TimeMisc.secondsToTicks(1));
	}

	private void refresh() {
		synchronized(this._allCurrentPlayers) {
			this._allCurrentPlayers.clear();
			for (BungeePlayer bungeePlayer : BungeePlayer.findAll()) {
				this._allCurrentPlayers.put(bungeePlayer.getOfflinePlayer(), bungeePlayer);
			}
			this._localPlayers = Bukkit.getOnlinePlayers().size();
		}
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

	private void addPlayerOnThisServer(final OfflinePlayer player) {
		addPlayerOnThisServer(player, 0);
	}

	private void addPlayerOnThisServer(final OfflinePlayer player, final int retries) {
		if (retries >= 5) {
			this._eithonPlugin.getEithonLogger().error("BungeePlayers.addPlayerOnThisServer: Could not find the bungee server name. Giving up after 5 retries.");
			return;
		}
		String bungeeServerName = getBungeeServerName();
		if (bungeeServerName != null) {
			final BungeePlayer bungeePlayer = BungeePlayer.createOrUpdate(player, bungeeServerName);
			this._localPlayers++;
			if (this._localPlayers == 1) {
				refresh();
				return;
			}
			synchronized(this._allCurrentPlayers) {
				this._allCurrentPlayers.put(player, bungeePlayer);
			}
			return;
		}
		final BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				addPlayerOnThisServer(player, retries+1);
			}
		};
		runnable.runTaskLaterAsynchronously(this._eithonPlugin, TimeMisc.secondsToTicks(1));
	}

	public void addPlayerOnOtherServerAsync(final OfflinePlayer player, final String otherServerName) {
		final BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				addPlayerOnOtherServer(player, otherServerName);
			}
		};
		runnable.runTaskAsynchronously(this._eithonPlugin);
	}

	private void addPlayerOnOtherServer(final OfflinePlayer player, final String otherServerName) {
		final BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayer(player);
		if ((bungeePlayer == null) || !otherServerName.equalsIgnoreCase(bungeePlayer.getBungeeServerName())) {
			this._eithonPlugin.getEithonLogger().error(
					"BungeePlayers.addPlayerOnOtherServer(%s,%s): Server name in DB = %s. Fail.",
					player.getName(), otherServerName,
					bungeePlayer == null? "NULL" : bungeePlayer.getBungeeServerName());
			return;
		}
		synchronized(this._allCurrentPlayers) {
			this._allCurrentPlayers.put(player, bungeePlayer);
		}
	}

	public void removePlayerOnThisServerAsync(final OfflinePlayer player) {
		final BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				removePlayerOnThisServer(player);
			}
		};
		runnable.runTaskAsynchronously(this._eithonPlugin);
	}

	private void removePlayerOnThisServer(final OfflinePlayer player) {
		this._localPlayers--;
		final BungeePlayer bungeePlayer;
		synchronized(this._allCurrentPlayers) {
			bungeePlayer = this._allCurrentPlayers.get(player);
			if (bungeePlayer == null) return;
			this._allCurrentPlayers.remove(player);
		}
		bungeePlayer.maybeDelete(getBungeeServerName());
	}

	public void removePlayerOnOtherServerAsync(final OfflinePlayer player, final String otherServerName) {
		final BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				removePlayerOnOtherServer(player, otherServerName);
			}
		};
		runnable.runTaskAsynchronously(this._eithonPlugin);
	}

	private void removePlayerOnOtherServer(final OfflinePlayer player, final String otherServerName) {
		synchronized(this._allCurrentPlayers) {
			final BungeePlayer bungeePlayer = this._allCurrentPlayers.get(player);
			if (bungeePlayer == null)  return;
			if (!bungeePlayer.getBungeeServerName().equalsIgnoreCase(otherServerName)) {
				// Join/leave probably out of sync. Update instead of remove.
				this._allCurrentPlayers.put(player, bungeePlayer);
			} else {
				this._allCurrentPlayers.remove(player);
			}
		}
	}

	public List<String> getNames() {
		synchronized(this._allCurrentPlayers) {
			return this._allCurrentPlayers.values()
					.stream()
					.map(bp -> bp.getOfflinePlayer().getName())
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

	void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "BungeePlayers.%s: %s", method, message);
	}
}
