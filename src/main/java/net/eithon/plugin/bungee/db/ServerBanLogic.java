package net.eithon.plugin.bungee.db;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbLogic;

public class ServerBanLogic extends DbLogic<ServerBanPojo> {

	public ServerBanLogic(final Database database) throws FatalException {
		super(ServerBanPojo.class, database);
	}
	
	public ServerBanPojo create(UUID playerId, String playerName, String bungeeServerName, LocalDateTime unbanAt) throws FatalException, TryAgainException {
		ServerBanPojo row = new ServerBanPojo();
		row.player_id = playerId.toString();
		row.player_name = playerName;
		row.bungee_server_name = bungeeServerName;
		row.unban_at = Timestamp.valueOf(unbanAt);
		long id = this.jDapper.createOne(row);
		return get(id);
	}

	public ServerBanPojo get(final UUID playerId, String serverName) throws FatalException, TryAgainException {
		return this.jDapper.readTheOnlyOneWhere("player_id=? AND bungee_server_name = ?",
				playerId.toString(), serverName);
	}
}
