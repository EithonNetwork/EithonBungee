package net.eithon.plugin.bungee.logic.ban;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import net.eithon.plugin.bungee.db.ServerBanPojo;

public class ServerBan  {
	private long id;
	private String bungeeServerName;
	private UUID playerId;
	private String playerName;
	private LocalDateTime unbanAt;

	public static ServerBan fromRow(ServerBanPojo row) {
		if (row == null) return null;
		ServerBan model = new ServerBan();
		model.id = row.id;
		model.bungeeServerName = row.bungee_server_name;
		model.playerId = UUID.fromString(row.player_id);
		model.playerName = row.player_name;
		model.unbanAt = row.unban_at.toLocalDateTime();
		return model;
	}
	
	public ServerBanPojo toRow() {
		ServerBanPojo row = new ServerBanPojo();
		row.id = this.id;
		row.bungee_server_name = this.bungeeServerName;
		row.player_id = this.playerId.toString();
		row.player_name = this.playerName;
		row.unban_at = Timestamp.valueOf(this.unbanAt);
		return row;
	}

	public long getId() { return id; }
	public LocalDateTime getUnbanAt() { return unbanAt;	}
	public void setUnbanAt(LocalDateTime unbanAt) { this.unbanAt = unbanAt; }
	public String getPlayerName() { return playerName; }
	public String getBungeeServerName() { return bungeeServerName;}
}
