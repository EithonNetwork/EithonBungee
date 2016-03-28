package net.eithon.plugin.bungee.logic;

import java.util.UUID;

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

	public static BungeePlayer getOrCreateByPlayerId(UUID playerId) {
		DbPlayer dbPlayer = DbPlayer.getByPlayerId(Config.V.database, playerId);
		if (dbPlayer == null) {
			String playerName = getPlayerNameById(playerId);
			dbPlayer = DbPlayer.create(Config.V.database, playerId, playerName, null);
		} else if (dbPlayer.getPlayerName() == null) {
			String playerName = getPlayerNameById(playerId);
			if (playerName != null) dbPlayer.updatePlayerName(playerName);
		}
		return new BungeePlayer(dbPlayer);
	}

	private static String getPlayerNameById(UUID playerId) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
		String playerName = null;
		if (player != null) playerName = player.getName();
		return playerName;
	}

	public static BungeePlayer getOrCreateByOfflinePlayer(OfflinePlayer player) {
		return getOrCreateByPlayerId(player.getUniqueId());
	}

	public static BungeePlayer getByOfflinePlayer(OfflinePlayer player) {
		return getByPlayerId(player.getUniqueId());
	}

	public void update(String bungeeServerName) {
		this.dbPlayer.updateBungeeServerName(bungeeServerName);
	}

	public void maybeDelete(String bungeeServerName) {
		this.dbPlayer.refresh();
		String currentBungeeServerName = getBungeeServerName();
		if (currentBungeeServerName == null) return;
		if (!currentBungeeServerName.equalsIgnoreCase(bungeeServerName)) return;
		this.dbPlayer.delete();
	}

	public String getBungeeServerName() { return this.dbPlayer.getBungeeServerName(); }
	public OfflinePlayer getOfflinePlayer() { return this.offlinePlayer; }
}
