package net.eithon.plugin.bungee.test;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import junit.framework.Assert;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.bungee.db.DbPlayer;

import org.junit.Test;

public class TestDbPlayer {

	@Test
	public void create() {
		UUID playerId = UUID.randomUUID();
		String bungeeServerName = "a";
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbPlayer dbPlayer = DbPlayer.create(database, playerId, bungeeServerName);
		assertEquals(playerId, dbPlayer.getPlayerId());
		assertEquals(bungeeServerName, dbPlayer.getBungeeServerName());
	}	
	
	@Test
	public void getByPlayerId() {
		UUID playerId = UUID.randomUUID();
		String bungeeServerName = "a";
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbPlayer dbPlayer = DbPlayer.create(database, playerId, bungeeServerName);
		dbPlayer = DbPlayer.getByPlayerId(database, playerId);
		Assert.assertNotNull(dbPlayer);
		assertEquals(playerId, dbPlayer.getPlayerId());
		assertEquals(bungeeServerName, dbPlayer.getBungeeServerName());
	}	
	
	@Test
	public void update() {
		UUID playerId = UUID.randomUUID();
		String bungeeServerName = "a";
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbPlayer dbPlayer = DbPlayer.create(database, playerId, bungeeServerName);
		dbPlayer = DbPlayer.getByPlayerId(database, playerId);
		bungeeServerName = bungeeServerName + "2";
		dbPlayer.updateBungeeServerName(bungeeServerName);
		dbPlayer = DbPlayer.getByPlayerId(database, playerId);
		assertEquals(bungeeServerName, dbPlayer.getBungeeServerName());
	}

}
