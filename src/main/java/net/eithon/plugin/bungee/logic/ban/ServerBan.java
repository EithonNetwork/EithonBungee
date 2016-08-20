package net.eithon.plugin.bungee.logic.ban;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import net.eithon.library.mysql.IRowMapper;
import net.eithon.plugin.bungee.db.ServerBanRow;

public class ServerBan implements IRowMapper<ServerBan, ServerBanRow>{
	private long id;
	private String bungeeServerName;
	private UUID playerId;
	private String playerName;
	private LocalDateTime unbanAt;

	public long getId() { return id; }
	public LocalDateTime getUnbanAt() { return unbanAt;	}
	public void setUnbanAt(LocalDateTime unbanAt) { this.unbanAt = unbanAt; }
	public String getPlayerName() { return playerName; }
	public String getBungeeServerName() { return bungeeServerName;}

	public static ServerBan createFromRow(ServerBanRow row) {
		if (row == null) return null;
		return new ServerBan().fromRow(row);
	}
	
	public ServerBan fromRow(ServerBanRow row) {
		this.id = row.id;
		this.bungeeServerName = row.bungee_server_name;
		this.playerId = UUID.fromString(row.player_id);
		this.playerName = row.player_name;
		this.unbanAt = row.unban_at.toLocalDateTime();
		return this;
	}

	public ServerBanRow toRow() {
		ServerBanRow row = new ServerBanRow();
		row.id = this.id;
		row.bungee_server_name = this.bungeeServerName;
		row.player_id = this.playerId.toString();
		row.player_name = this.playerName;
		row.unban_at = Timestamp.valueOf(this.unbanAt);
		return row;
	}

	public ServerBan factory() {
		return new ServerBan();
	}
}
