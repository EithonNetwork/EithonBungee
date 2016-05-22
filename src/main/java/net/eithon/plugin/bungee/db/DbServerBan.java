package net.eithon.plugin.bungee.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbRecord;
import net.eithon.library.mysql.IDbRecord;
import net.eithon.library.time.TimeMisc;

public class DbServerBan extends DbRecord<DbServerBan> implements IDbRecord<DbServerBan> {
	private String bungeeServerName;
	private UUID playerId;
	private String playerName;
	private LocalDateTime unbanAt;

	public static DbServerBan create(Database database, UUID playerId, String playerName, String bungeeServerName, LocalDateTime unbanAt) {
		DbServerBan bungeePlayer = get(database, playerId, bungeeServerName);
		if (bungeePlayer == null) {
			bungeePlayer = new DbServerBan(database, playerId, playerName, bungeeServerName, unbanAt);
			bungeePlayer.dbCreate();
		}
		return bungeePlayer;	
	}

	public static DbServerBan get(Database database, UUID playerId, String bungeeServerName) {
		return getByWhere(database, "player_id=? AND bungee_server_name=?", playerId.toString(), bungeeServerName);
	}

	public static List<DbServerBan> findAll(Database database) {
		DbServerBan dbPlayer = new DbServerBan(database);
		return dbPlayer.findAll();
	}

	private DbServerBan(Database database, UUID playerId, String playerName, String bungeeServerName, LocalDateTime unbanAt) {
		this(database);
		this.bungeeServerName = bungeeServerName;
		this.playerId = playerId;
		this.playerName = playerName;
		this.unbanAt = unbanAt;
	}

	private DbServerBan(Database database) {
		this(database, -1);
	}

	protected DbServerBan(Database database, long id) {
		super(database, "server_ban", id);
	}

	public UUID getPlayerId() { return this.playerId; }
	public String getPlayerName() { return this.playerName; }
	public String getBungeeServerName() { return this.bungeeServerName; }
	public LocalDateTime getUnbanAt() { return this.unbanAt; }

	@Override
	public String toString() {
		String result = String.format("%s@%s until %s", 
				this.playerName, this.bungeeServerName, TimeMisc.fromLocalDateTime(this.unbanAt));
		return result;
	}

	public void updateUnbanAt(LocalDateTime unbanAt) {
		this.unbanAt = unbanAt;
		dbUpdate();
	}

	private static DbServerBan getByWhere(Database database, String format, Object... arguments) {
		DbServerBan dbServerBan = new DbServerBan(database);
		return dbServerBan.getByWhere(format, arguments);
	}

	@Override
	public DbServerBan fromDb(ResultSet resultSet) throws SQLException {
		super.fromDb(resultSet);
		this.playerId = UUID.fromString(resultSet.getString("player_id"));
		this.playerName = resultSet.getString("player_name");
		this.bungeeServerName = resultSet.getString("bungee_server_name");
		this.unbanAt = resultSet.getTimestamp("unban_at").toLocalDateTime();
		return this;
	}

	@Override
	public HashMap<String, Object> getColumnValues() {
		HashMap<String, Object> columnValues = new HashMap<String, Object>();
		columnValues.put("player_id", this.playerId.toString());
		columnValues.put("player_name", this.playerName);
		columnValues.put("bungee_server_name", this.bungeeServerName);
		columnValues.put("unban_at", TimeMisc.fromLocalDateTime(this.unbanAt));
		return columnValues;
	}

	@Override
	public DbServerBan factory(Database database, long id) {
		return new DbServerBan(database, id);
	}

	@Override
	public String getUpdatedAtColumnName() { return "updated_at"; }
}
