package net.eithon.plugin.bungee.test;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import junit.framework.Assert;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.bungee.db.DbServerBan;

import org.junit.Test;

public class TestDbServerBan {

	@Test
	public void create() {
		UUID playerId = UUID.randomUUID();
		String playerName = "player1";
		String bungeeServerName = "a";
		LocalDateTime unbanAt = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbServerBan dbServerBan = DbServerBan.create(database, playerId, playerName, bungeeServerName, unbanAt);
		assertEquals(playerId, dbServerBan.getPlayerId());
		assertEquals(playerName, dbServerBan.getPlayerName());
		assertEquals(bungeeServerName, dbServerBan.getBungeeServerName());
		assertEquals(unbanAt, dbServerBan.getUnbanAt());
	}	
	
	@Test
	public void getByPlayerId() {
		UUID playerId = UUID.randomUUID();
		String playerName = "player1";
		String bungeeServerName = "a";
		LocalDateTime unbanAt = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbServerBan dbServerBan = DbServerBan.create(database, playerId, playerName, bungeeServerName, unbanAt);
		dbServerBan = DbServerBan.get(database, playerId, bungeeServerName);
		Assert.assertNotNull(dbServerBan);
		assertEquals(playerId, dbServerBan.getPlayerId());
		assertEquals(playerName, dbServerBan.getPlayerName());
		assertEquals(bungeeServerName, dbServerBan.getBungeeServerName());
		assertEquals(unbanAt, dbServerBan.getUnbanAt());
	}	
	
	@Test
	public void update() {
		UUID playerId = UUID.randomUUID();
		String playerName = "player1";
		String bungeeServerName = "a";
		LocalDateTime unbanAt = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbServerBan dbServerBan = DbServerBan.create(database, playerId, playerName, bungeeServerName, unbanAt);
		dbServerBan = DbServerBan.get(database, playerId, bungeeServerName);
		unbanAt = unbanAt.plusSeconds(1);
		dbServerBan.updateUnbanAt(unbanAt);
		dbServerBan = DbServerBan.get(database, playerId, bungeeServerName);
		assertEquals(unbanAt, dbServerBan.getUnbanAt());
	}

}
