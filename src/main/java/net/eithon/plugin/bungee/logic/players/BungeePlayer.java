package net.eithon.plugin.bungee.logic.players;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.eithon.plugin.bungee.Config;
import net.eithon.plugin.bungee.db.DbPlayer;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class BungeePlayer {
	private OfflinePlayer offlinePlayer;
	private DbPlayer dbPlayer;

	private BungeePlayer(DbPlayer dbPlayer) {
		this.dbPlayer = dbPlayer;
		this.offlinePlayer = Bukkit.getOfflinePlayer(dbPlayer.getPlayerId());
	}

	public static BungeePlayer getByPlayerId(UUID playerId) {
		DbPlayer dbPlayer = DbPlayer.getByPlayerId(Config.V.database, playerId);
		if (dbPlayer == null) return null;
		if (dbPlayer.getPlayerName() == null) {
			String playerName = getPlayerNameById(playerId);
			if (playerName != null) dbPlayer.updatePlayerName(playerName);
		}
		return new BungeePlayer(dbPlayer);
	}

	public static List<BungeePlayer> findAll() {
		return DbPlayer.findAll(Config.V.database).stream().map(dbPlayer -> new BungeePlayer(dbPlayer)).collect(Collectors.toList());
	}

	public static BungeePlayer createOrUpdate(OfflinePlayer player, String bungeeServerName) {
		UUID playerId = player.getUniqueId();
		DbPlayer dbPlayer = DbPlayer.getByPlayerId(Config.V.database, playerId);
		if (dbPlayer == null) {
			dbPlayer = DbPlayer.create(Config.V.database, playerId, player.getName(), bungeeServerName);
		} else {
			if (!player.getName().equals(dbPlayer.getPlayerName())) {
				dbPlayer.updatePlayerName(player.getName());
			}
			if ((bungeeServerName != null)
					&& !bungeeServerName.equalsIgnoreCase(dbPlayer.getBungeeServerName())) {
				dbPlayer.updateBungeeServerName(bungeeServerName);
			}
		}
		return new BungeePlayer(dbPlayer);
	}

	private static String getPlayerNameById(UUID playerId) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
		String playerName = null;
		if (player != null) playerName = player.getName();
		return playerName;
	}

	public static BungeePlayer getByOfflinePlayer(OfflinePlayer player) {
		return getByPlayerId(player.getUniqueId());
	}

	public void update(String bungeeServerName) {
		this.dbPlayer.updateBungeeServerName(bungeeServerName);
	}

	public boolean deleteIfServerNameMatches(String bungeeServerName) {
		this.dbPlayer.refresh();
		String currentBungeeServerName = getBungeeServerName();
		if (currentBungeeServerName == null) return false;
		if (!currentBungeeServerName.equalsIgnoreCase(bungeeServerName)) return false;
		this.dbPlayer.delete();
		return true;
	}

	public String getBungeeServerName() { return this.dbPlayer.getBungeeServerName(); }
	private OfflinePlayer getOfflinePlayer() { return this.offlinePlayer; }
	public String getPlayerName() { return this.dbPlayer.getPlayerName(); }
	public UUID getPlayerId() { return this.dbPlayer.getPlayerId(); }
	public boolean isOnline() { return getOfflinePlayer().isOnline(); }
	public void refresh() { this.dbPlayer.refresh(); }
}
