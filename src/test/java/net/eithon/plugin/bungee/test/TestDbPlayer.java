package net.eithon.plugin.bungee.test;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import junit.framework.Assert;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.bungee.db.DbPlayer;

import org.junit.Test;

public class TestDbPlayer {

	@Test
	public void create() {
		UUID playerId = UUID.randomUUID();
		String playerName = "player1";
		String bungeeServerName = "a";
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbPlayer dbPlayer = DbPlayer.create(database, playerId, playerName, bungeeServerName);
		assertEquals(playerId, dbPlayer.getPlayerId());
		assertEquals(playerName, dbPlayer.getPlayerName());
		Assert.assertNull(dbPlayer.getPlayerLeftServerAt());
		assertEquals(bungeeServerName, dbPlayer.getBungeeServerName());
	}	
	
	@Test
	public void getByPlayerId() {
		UUID playerId = UUID.randomUUID();
		String playerName = "player1";
		String bungeeServerName = "a";
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbPlayer dbPlayer = DbPlayer.create(database, playerId, playerName, bungeeServerName);
		dbPlayer = DbPlayer.getByPlayerId(database, playerId);
		Assert.assertNotNull(dbPlayer);
		assertEquals(playerId, dbPlayer.getPlayerId());
		assertEquals(playerName, dbPlayer.getPlayerName());
		Assert.assertNull(dbPlayer.getPlayerLeftServerAt());
		assertEquals(bungeeServerName, dbPlayer.getBungeeServerName());
	}	
	
	@Test
	public void updateBungeeServerName() {
		UUID playerId = UUID.randomUUID();
		String playerName = "player1";
		String bungeeServerName = "a";
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbPlayer dbPlayer = DbPlayer.create(database, playerId, playerName, bungeeServerName);
		dbPlayer = DbPlayer.getByPlayerId(database, playerId);
		bungeeServerName = bungeeServerName + "2";
		dbPlayer.updateBungeeServerName(bungeeServerName, null);
		dbPlayer = DbPlayer.getByPlayerId(database, playerId);
		Assert.assertNull(dbPlayer.getPlayerLeftServerAt());
		assertEquals(bungeeServerName, dbPlayer.getBungeeServerName());
	}
	
	@Test
	public void updateLeftAt() {
		UUID playerId = UUID.randomUUID();
		String playerName = "player1";
		String bungeeServerName = "a";
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbPlayer dbPlayer = DbPlayer.create(database, playerId, playerName, bungeeServerName);
		dbPlayer = DbPlayer.getByPlayerId(database, playerId);
		LocalDateTime time = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		dbPlayer.updateLeftAt(time);
		dbPlayer = DbPlayer.getByPlayerId(database, playerId);
		assertEquals(time, dbPlayer.getPlayerLeftServerAt());
	}	
	
	@Test
	public void updateBungeeServerNameAfterLeft() {
		UUID playerId = UUID.randomUUID();
		String playerName = "player1";
		String bungeeServerName = "a";
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbPlayer dbPlayer = DbPlayer.create(database, playerId, playerName, bungeeServerName);
		LocalDateTime time = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		dbPlayer.updateLeftAt(time);
		dbPlayer = DbPlayer.getByPlayerId(database, playerId);
		bungeeServerName = bungeeServerName + "2";
		dbPlayer.updateBungeeServerName(bungeeServerName, null);
		dbPlayer = DbPlayer.getByPlayerId(database, playerId);
		Assert.assertNull(dbPlayer.getPlayerLeftServerAt());
		assertEquals(bungeeServerName, dbPlayer.getBungeeServerName());
	}
}
