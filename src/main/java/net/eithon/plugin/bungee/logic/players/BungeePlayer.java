package net.eithon.plugin.bungee.logic.players;

import java.time.LocalDateTime;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

class BungeePlayer {

	private long id;
	private String bungeeServerName;
	private UUID playerId;
	private String playerName;
	private LocalDateTime leftAt;

	/*public static BungeePlayer getByPlayerId(UUID playerId) {
		PlayerController dbPlayer = PlayerController.getByPlayerId(Config.V.database, playerId);
		if (dbPlayer == null) return null;
		if (deleteIfOld(dbPlayer)) return null;
		if (dbPlayer.getPlayerName() == null) {
			String playerName = getPlayerNameById(playerId);
			if (playerName != null) dbPlayer.updatePlayerName(playerName);
		}
		return new BungeePlayer(dbPlayer);
	}

	public static List<BungeePlayer> findAll(boolean onlyOnline) {
		return PlayerController.findAll(Config.V.database, onlyOnline).stream().map(dbPlayer -> new BungeePlayer(playerRow)).collect(Collectors.toList());
	}

	public static BungeePlayer createOrUpdate(OfflinePlayer player, String bungeeServerName) {
		UUID playerId = player.getUniqueId();
		PlayerController dbPlayer = PlayerController.getByPlayerId(Config.V.database, playerId);
		if (dbPlayer == null) {
			dbPlayer = PlayerController.create(Config.V.database, playerId, player.getName(), bungeeServerName);
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
		this.playerRow.updateBungeeServerName(bungeeServerName, null);
	}
	*/
	
	public boolean isOld() {
		if (isOnline()) return false;
		if (this.leftAt.plusSeconds(60).isAfter(LocalDateTime.now()))  return false;
		return true;
	}

	public boolean isSameServer(String serverName) {
		return serverName.equalsIgnoreCase(this.bungeeServerName);
	}

	public String getCurrentBungeeServerName() { if (hasLeft()) return null; else return getBungeeServerName(); }
	public String getPreviousBungeeServerName2() { if (!hasLeft()) return null; return getBungeeServerName(); }
	public String getAnyBungeeServerName() { return getBungeeServerName(); }
	
	private boolean hasLeft() { return this.leftAt != null; }

	public String getBungeeServerName() {
		return bungeeServerName;
	}

	public void setBungeeServerName(String bungeeServerName) {
		this.bungeeServerName = bungeeServerName;
	}

	public UUID getPlayerId() {
		return this.playerId;
	}

	public void setPlayerId(UUID playerId) {
		this.playerId = playerId;
	}

	public String getPlayerName() {
		return this.playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public LocalDateTime getLeftAt() {
		return leftAt;
	}

	public void setLeftAt(LocalDateTime leftAt) {
		this.leftAt = leftAt;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isOnline() {
		return this.leftAt == null;
	}

	public boolean isOnlineOnServer(String bungeeServerName) {
		if (!isSameServer(bungeeServerName)) return false;
		return isOnline();
	}
}
