package net.eithon.plugin.bungee.db;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbTable;

public class ServerBanTable extends DbTable<ServerBanRow> {

	public ServerBanTable(final Database database) throws FatalException {
		super(ServerBanRow.class, database);
	}
	
	public ServerBanRow create(UUID playerId, String playerName, String bungeeServerName, LocalDateTime unbanAt) throws FatalException, TryAgainException {
		ServerBanRow row = new ServerBanRow();
		row.player_id = playerId.toString();
		row.player_name = playerName;
		row.bungee_server_name = bungeeServerName;
		row.unban_at = Timestamp.valueOf(unbanAt);
		long id = this.jDapper.createOne(row);
		return get(id);
	}

	public ServerBanRow get(final UUID playerId, String serverName) throws FatalException, TryAgainException {
		return this.jDapper.readTheOnlyOneWhere("player_id=? AND bungee_server_name = ?",
				playerId.toString(), serverName);
	}
}
