package net.eithon.plugin.bungee.db;

import java.util.List;
import java.util.UUID;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbTable;

public class PlayerTable extends DbTable<PlayerRow>{

	public PlayerTable(final Database database) throws FatalException {
		super(PlayerRow.class, database);
	}
	
	public PlayerRow create(final UUID playerId, final String playerName, final String bungeeServerName) throws FatalException, TryAgainException {
		PlayerRow playerRow = new PlayerRow();
		playerRow.player_id = playerId.toString();
		playerRow.player_name = playerName;
		playerRow.bungee_server_name = bungeeServerName;
		long id = this.jDapper.createOne(playerRow);
		return get(id);
	}

	public PlayerRow getByPlayerId(final UUID playerId) throws FatalException, TryAgainException {
		return this.jDapper.readTheOnlyOneWhere("player_id=?", playerId.toString());
	}

	public PlayerRow getOrCreate(final UUID playerId, final String playerName, final String bungeeServerName) throws FatalException, TryAgainException {
		PlayerRow playerRow = getByPlayerId(playerId);
		if (playerRow != null) return playerRow;
		return create(playerId, playerName, bungeeServerName);
	}

	public PlayerRow createOrUpdate(final UUID playerId, final String playerName, final String bungeeServerName) throws FatalException, TryAgainException {
		PlayerRow playerRow = getByPlayerId(playerId);
		if (playerRow == null) return create(playerId, playerName, bungeeServerName);
		playerRow.bungee_server_name = bungeeServerName;
		playerRow.left_at = null;
		update(playerRow);
		return playerRow;
	}

	public List<PlayerRow> findAll(boolean onlyOnline) throws FatalException, TryAgainException {
		if (onlyOnline) return findAllOnline();
		return findAll();
	}

	public void deleteByServerName(String bungeeServerName) throws FatalException, TryAgainException {
		jDapper.deleteWhere("bungee_server_name=?", bungeeServerName);
	}

	private List<PlayerRow> findAllOnline() throws FatalException, TryAgainException  {
		return jDapper.readSomeWhere("left_at IS NULL");
	}
}
