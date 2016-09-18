package net.eithon.plugin.bungee.test;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import junit.framework.Assert;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.bungee.db.ServerBanTable;
import net.eithon.plugin.bungee.db.ServerBanRow;

import org.junit.Test;

public class TestDbServerBan {

	@Test
	public void create() {
		try {
			UUID playerId = UUID.randomUUID();
			String playerName = "player1";
			String bungeeServerName = "a";
			LocalDateTime unbanAt = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);
			Database database = TestSupport.getDatabaseAndTruncateTables();
			ServerBanTable handler = new ServerBanTable(database);
			ServerBanRow dbServerBan = handler.create(playerId, playerName, bungeeServerName, unbanAt);
			assertEquals(playerId.toString(), dbServerBan.player_id);
			assertEquals(playerName, dbServerBan.player_name);
			assertEquals(bungeeServerName, dbServerBan.bungee_server_name);
			assertEquals(Timestamp.valueOf(unbanAt), dbServerBan.unban_at);
		} catch (Exception e) {
			Assert.fail();
		}
	}	

	@Test
	public void getByPlayerId() {
		try {
			UUID playerId = UUID.randomUUID();
			String playerName = "player1";
			String bungeeServerName = "a";
			LocalDateTime unbanAt = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);
			Database database = TestSupport.getDatabaseAndTruncateTables();
			ServerBanTable handler = new ServerBanTable(database);
			ServerBanRow dbServerBan = handler.create(playerId, playerName, bungeeServerName, unbanAt);
			dbServerBan = handler.get(playerId, bungeeServerName);
			Assert.assertNotNull(dbServerBan);
			assertEquals(playerId.toString(), dbServerBan.player_id);
			assertEquals(playerName, dbServerBan.player_name);
			assertEquals(bungeeServerName, dbServerBan.bungee_server_name);
			assertEquals(Timestamp.valueOf(unbanAt), dbServerBan.unban_at);
		} catch (Exception e) {
			Assert.fail();
		}
	}	

	@Test
	public void update() {
		try {
			UUID playerId = UUID.randomUUID();
			String playerName = "player1";
			String bungeeServerName = "a";
			LocalDateTime unbanAt = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);
			Database database = TestSupport.getDatabaseAndTruncateTables();
			ServerBanTable handler = new ServerBanTable(database);
			ServerBanRow dbServerBan = handler.create(playerId, playerName, bungeeServerName, unbanAt);
			dbServerBan = handler.get(playerId, bungeeServerName);
			unbanAt = unbanAt.plusSeconds(1);
			dbServerBan.unban_at = Timestamp.valueOf(unbanAt);
			handler.update(dbServerBan);
			dbServerBan = handler.get(playerId, bungeeServerName);
			assertEquals(Timestamp.valueOf(unbanAt), dbServerBan.unban_at);
		} catch (Exception e) {
			Assert.fail();
		}
	}

}
