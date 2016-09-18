package net.eithon.plugin.bungee.test;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import junit.framework.Assert;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.bungee.db.PlayerTable;
import net.eithon.plugin.bungee.db.PlayerRow;

import org.junit.Test;

public class TestDbPlayer {

	@Test
	public void create() {
		try {
			UUID playerId = UUID.randomUUID();
			String playerName = "player1";
			String bungeeServerName = "a";
			Database database = TestSupport.getDatabaseAndTruncateTables();
			PlayerTable handler = new PlayerTable(database);
			PlayerRow dbPlayer = handler.create(playerId, playerName, bungeeServerName);
			assertEquals(playerId.toString(), dbPlayer.player_id);
			assertEquals(playerName, dbPlayer.player_name);
			Assert.assertNull(dbPlayer.left_at);
			assertEquals(bungeeServerName, dbPlayer.bungee_server_name);
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
			Database database = TestSupport.getDatabaseAndTruncateTables();
			PlayerTable handler = new PlayerTable(database);
			PlayerRow dbPlayer = handler.create(playerId, playerName, bungeeServerName);
			dbPlayer = handler.getByPlayerId(playerId);
			Assert.assertNotNull(dbPlayer);
			assertEquals(playerId.toString(), dbPlayer.player_id);
			assertEquals(playerName, dbPlayer.player_name);
			Assert.assertNull(dbPlayer.left_at);
			assertEquals(bungeeServerName, dbPlayer.bungee_server_name);
		} catch (Exception e) {
			Assert.fail();
		}
	}	

	@Test
	public void updateBungeeServerName() {
		try {
			UUID playerId = UUID.randomUUID();
			String playerName = "player1";
			String bungeeServerName = "a";
			Database database = TestSupport.getDatabaseAndTruncateTables();
			PlayerTable handler = new PlayerTable(database);
			PlayerRow dbPlayer = handler.create(playerId, playerName, bungeeServerName);
			dbPlayer = handler.getByPlayerId(playerId);
			bungeeServerName += "2";
			dbPlayer.bungee_server_name = bungeeServerName;
			handler.update(dbPlayer);
			dbPlayer = handler.getByPlayerId(playerId);
			Assert.assertNull(dbPlayer.left_at);
			assertEquals(bungeeServerName, dbPlayer.bungee_server_name);
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void updateLeftAt() {
		try {
			UUID playerId = UUID.randomUUID();
			String playerName = "player1";
			String bungeeServerName = "a";
			Database database = TestSupport.getDatabaseAndTruncateTables();
			PlayerTable handler = new PlayerTable(database);
			PlayerRow dbPlayer = handler.create(playerId, playerName, bungeeServerName);
			dbPlayer = handler.getByPlayerId(playerId);
			final Timestamp time = Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
			dbPlayer.left_at = time;
			handler.update(dbPlayer);
			dbPlayer = handler.getByPlayerId(playerId);
			assertEquals(time, dbPlayer.left_at);
		} catch (Exception e) {
			Assert.fail();
		}
	}	

	@Test
	public void updateBungeeServerNameAfterLeft() {
		try {
			UUID playerId = UUID.randomUUID();
			String playerName = "player1";
			String bungeeServerName = "a";
			Database database = TestSupport.getDatabaseAndTruncateTables();
			PlayerTable handler = new PlayerTable(database);
			PlayerRow dbPlayer = handler.create(playerId, playerName, bungeeServerName);
			final Timestamp time = Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
			dbPlayer.left_at = time;
			bungeeServerName += "2";
			dbPlayer.bungee_server_name = bungeeServerName;
			dbPlayer.left_at = null;
			handler.update(dbPlayer);
			dbPlayer = handler.getByPlayerId(playerId);
			Assert.assertNull(dbPlayer.left_at);
			assertEquals(bungeeServerName, dbPlayer.bungee_server_name);
		} catch (Exception e) {
			Assert.fail();
		}
	}
}
