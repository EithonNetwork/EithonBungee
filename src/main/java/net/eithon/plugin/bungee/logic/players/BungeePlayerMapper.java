package net.eithon.plugin.bungee.logic.players;

import java.sql.Timestamp;
import java.util.UUID;

import net.eithon.plugin.bungee.db.PlayerPojo;

public class BungeePlayerMapper {

	public static BungeePlayer rowToModel(PlayerPojo row)  {
		if (row == null) return null;
		BungeePlayer model = new BungeePlayer();
		model.setBungeeServerName(row.bungee_server_name);
		model.setId(row.id);
		model.setLeftAt(row.left_at.toLocalDateTime());
		model.setPlayerId(UUID.fromString(row.player_id));
		model.setPlayerName(row.player_name);
		return model;
	}
	
	public static PlayerPojo modelToRow(BungeePlayer model)  {
		if (model == null) return null;
		PlayerPojo row = new PlayerPojo();
		row.bungee_server_name = model.getBungeeServerName();
		row.id = model.getId();
		row.left_at = Timestamp.valueOf(model.getLeftAt());
		row.player_id = model.getPlayerId().toString();
		row.player_name = model.getPlayerName();
		return row;
	}
}
