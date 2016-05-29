package net.eithon.plugin.bungee.logic.players;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.eithon.plugin.bungee.Config;
import net.eithon.plugin.bungee.db.DbPlayer;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

class BungeePlayer {
	private OfflinePlayer offlinePlayer;
	private DbPlayer dbPlayer;

	private BungeePlayer(DbPlayer dbPlayer) {
		this.dbPlayer = dbPlayer;
		this.offlinePlayer = Bukkit.getOfflinePlayer(dbPlayer.getPlayerId());
	}

	public static BungeePlayer getByPlayerId(UUID playerId) {
		DbPlayer dbPlayer = DbPlayer.getByPlayerId(Config.V.database, playerId);
		if (dbPlayer == null) return null;
		if (deleteIfOld(dbPlayer)) return null;
		if (dbPlayer.getPlayerName() == null) {
			String playerName = getPlayerNameById(playerId);
			if (playerName != null) dbPlayer.updatePlayerName(playerName);
		}
		return new BungeePlayer(dbPlayer);
	}

	private static boolean deleteIfOld(DbPlayer dbPlayer) {
		final LocalDateTime playerLeftServerAt = dbPlayer.getPlayerLeftServerAt();
		if (playerLeftServerAt == null) return false;
		if (playerLeftServerAt.plusSeconds(60).isAfter(LocalDateTime.now()))  return false;
		dbPlayer.delete();
		return true;
	}

	public static List<BungeePlayer> findAll(boolean onlyOnline) {
		return DbPlayer.findAll(Config.V.database, onlyOnline).stream().map(dbPlayer -> new BungeePlayer(dbPlayer)).collect(Collectors.toList());
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
			if (bungeeServerName != null) {
				dbPlayer.updateBungeeServerName(bungeeServerName, null);
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
		this.dbPlayer.updateBungeeServerName(bungeeServerName, null);
	}

	public boolean maybeDelete(String bungeeServerName) {
		if (!bungeeServerName.equalsIgnoreCase(this.dbPlayer.getBungeeServerName())) return false;
		Player player = Bukkit.getServer().getPlayer(this.dbPlayer.getPlayerId());
		if (player != null) return false;
		if (deleteIfOld(this.dbPlayer)) {
			this.dbPlayer = null;
			return true;
		}
		this.dbPlayer.updateLeftAt(LocalDateTime.now());
		return false;
	}

	public String getCurrentBungeeServerName() { if (hasLeft()) return null; else return this.dbPlayer.getBungeeServerName(); }
	public String getPreviousBungeeServerName() { if (!hasLeft()) return null; return this.dbPlayer.getBungeeServerName(); }
	public String getAnyBungeeServerName() { return this.dbPlayer.getBungeeServerName(); }
	private OfflinePlayer getOfflinePlayer() { return this.offlinePlayer; }
	public String getPlayerName() { return this.dbPlayer.getPlayerName(); }
	public UUID getPlayerId() { return this.dbPlayer.getPlayerId(); }
	public boolean isOnlineOnThisServer() { return getOfflinePlayer().isOnline(); }
	public void refresh() { this.dbPlayer.refresh(); }

	private boolean hasLeft() { return this.dbPlayer.getPlayerLeftServerAt() != null; }
}
