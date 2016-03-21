package net.eithon.plugin.bungee.logic;

import java.util.UUID;

import net.eithon.plugin.bungee.Config;
import net.eithon.plugin.bungee.db.DbPlayer;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class BungeePlayer {
	private OfflinePlayer offlinePlayer;
	private DbPlayer dbPlayer;
	
	private BungeePlayer(DbPlayer dbPlayer) {
		this.dbPlayer = dbPlayer;
		this.offlinePlayer = Bukkit.getOfflinePlayer(dbPlayer.getPlayerId());
	}
	
	public static BungeePlayer getByPlayerId(UUID playerId) {
		DbPlayer dbPlayer = DbPlayer.getByPlayerId(Config.V.database, playerId);
		if (dbPlayer == null) {
			dbPlayer = DbPlayer.create(Config.V.database, playerId, null);
		}
		return new BungeePlayer(dbPlayer);
	}
	
	public static BungeePlayer getByOfflinePlayer(OfflinePlayer player) {
		return getByPlayerId(player.getUniqueId());
	}
	
	public void update(String bungeeServerName) {
		this.dbPlayer.update(bungeeServerName);
	}
	
	public String getBungeeServerName() { return this.dbPlayer.getBungeeServerName(); }
	public OfflinePlayer getOfflinePlayer() { return this.offlinePlayer; }
}
