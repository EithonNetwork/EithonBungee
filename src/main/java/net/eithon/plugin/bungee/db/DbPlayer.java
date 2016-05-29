package net.eithon.plugin.bungee.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbRecord;
import net.eithon.library.mysql.IDbRecord;
import net.eithon.library.time.TimeMisc;

public class DbPlayer extends DbRecord<DbPlayer> implements IDbRecord<DbPlayer> {
	private String bungeeServerName;
	private UUID playerId;
	private String playerName;
	private LocalDateTime leftAt;

	public static DbPlayer create(Database database, UUID playerId, String playerName, String bungeeServerName) {
		DbPlayer bungeePlayer = getByPlayerId(database, playerId);
		if (bungeePlayer == null) {
			bungeePlayer = new DbPlayer(database, playerId, playerName, bungeeServerName);
			bungeePlayer.dbCreate();
		}
		return bungeePlayer;	
	}

	public static DbPlayer getByPlayerId(Database database, UUID playerId) {
		return getByWhere(database, "player_id=?", playerId.toString());
	}

	public static List<DbPlayer> findAll(Database database, boolean onlyOnline) {
		DbPlayer dbPlayer = new DbPlayer(database);
		if (onlyOnline) return dbPlayer.findAllOnline();
		return dbPlayer.findAll();
	}

	public static void deleteByServerName(Database database, String bungeeServerName) {
		DbPlayer dbPlayer = new DbPlayer(database);
		dbPlayer.deleteByWhere("bungee_server_name='%s'", bungeeServerName);
	}

	private List<DbPlayer> findAllOnline()  {
		return findByWhere("left_at IS NULL");
	}	

	private DbPlayer(Database database, UUID playerId, String playerName, String bungeeServerName) {
		this(database);
		this.bungeeServerName = bungeeServerName;
		this.playerId = playerId;
		this.playerName = playerName;
	}

	private DbPlayer(Database database) {
		this(database, -1);
	}

	protected DbPlayer(Database database, long id) {
		super(database, "player", id);
	}

	public UUID getPlayerId() { return this.playerId; }
	public String getPlayerName() { return this.playerName; }
	public String getBungeeServerName() { return this.bungeeServerName; }
	public LocalDateTime getPlayerLeftServerAt() { return this.leftAt; }

	@Override
	public String toString() {
		if (this.leftAt == null) return String.format("%s@%s", this.playerName, this.bungeeServerName);
		return String.format("%s has left server %s", this.playerName, this.bungeeServerName);
	}

	public void updateBungeeServerName(String bungeeServerName, LocalDateTime time) {
		this.bungeeServerName = bungeeServerName;
		this.leftAt = time;
		dbUpdate();
	}

	public void updatePlayerName(String playerName) {
		this.playerName = playerName;
		dbUpdate();
	}

	public void updateLeftAt(LocalDateTime time) {
		this.leftAt = time;
		dbUpdate();
	}

	private static DbPlayer getByWhere(Database database, String format, Object... arguments) {
		DbPlayer dbPlayer = new DbPlayer(database);
		return dbPlayer.getByWhere(format, arguments);
	}

	@Override
	public DbPlayer fromDb(ResultSet resultSet) throws SQLException {
		super.fromDb(resultSet);
		this.playerId = UUID.fromString(resultSet.getString("player_id"));
		this.playerName = resultSet.getString("player_name");
		this.bungeeServerName = resultSet.getString("bungee_server_name");
		this.leftAt = null;
		final Timestamp timestamp = resultSet.getTimestamp("left_at");
		if (timestamp != null) this.leftAt = timestamp.toLocalDateTime();
		return this;
	}

	@Override
	public HashMap<String, Object> getColumnValues() {
		HashMap<String, Object> columnValues = new HashMap<String, Object>();
		columnValues.put("player_id", this.playerId.toString());
		columnValues.put("player_name", this.playerName);
		columnValues.put("bungee_server_name", this.bungeeServerName);
		columnValues.put("left_at", TimeMisc.fromLocalDateTime(this.leftAt));
		return columnValues;
	}

	@Override
	public DbPlayer factory(Database database, long id) {
		return new DbPlayer(database, id);
	}

	@Override
	public String getUpdatedAtColumnName() { return "updated_at"; }
}
