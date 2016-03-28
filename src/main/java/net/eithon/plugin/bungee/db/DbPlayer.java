package net.eithon.plugin.bungee.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbRecord;
import net.eithon.library.mysql.IDbRecord;

public class DbPlayer extends DbRecord<DbPlayer> implements IDbRecord<DbPlayer> {
	private String bungeeServerName;
	private UUID playerId;

	public static DbPlayer create(Database database, UUID playerId, String bungeeServerName) {
		DbPlayer bungeePlayer = getByPlayerId(database, playerId);
		if (bungeePlayer == null) {
			bungeePlayer = new DbPlayer(database, playerId, bungeeServerName);
			bungeePlayer.dbCreate();
		}
		return bungeePlayer;	
	}

	public static DbPlayer getByPlayerId(Database database, UUID playerId) {
		return getByWhere(database, "player_id=?", playerId.toString());
	}

	private DbPlayer(Database database, UUID playerId, String bungeeServerName) {
		this(database);
		this.bungeeServerName = bungeeServerName;
		this.playerId = playerId;
	}

	private DbPlayer(Database database) {
		this(database, -1);
	}

	protected DbPlayer(Database database, long id) {
		super(database, "player", id);
	}

	public String getBungeeServerName() { return this.bungeeServerName; }
	public UUID getPlayerId() { return this.playerId; }

	@Override
	public String toString() {
		String result = String.format("%s (%s)", this.playerId, this.bungeeServerName);
		return result;
	}

	public void update(String bungeeServerName) {
		this.bungeeServerName = bungeeServerName;
		dbUpdate();
	}

	private static DbPlayer getByWhere(Database database, String format, Object... arguments) {
		DbPlayer similar = new DbPlayer(database);
		return similar.getByWhere(format, arguments);
	}

	@Override
	public DbPlayer fromDb(ResultSet resultSet) throws SQLException {
		this.playerId = UUID.fromString(resultSet.getString("player_id"));
		this.bungeeServerName = resultSet.getString("bungee_server_name");
		return this;
	}

	@Override
	public HashMap<String, Object> getColumnValues() {
		HashMap<String, Object> columnValues = new HashMap<String, Object>();
		columnValues.put("player_id", this.playerId.toString());
		columnValues.put("bungee_server_name", this.bungeeServerName);
		return columnValues;
	}

	@Override
	public DbPlayer factory(Database database, long id) {
		return new DbPlayer(database, id);
	}
}
