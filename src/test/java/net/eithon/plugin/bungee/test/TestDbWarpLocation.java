package net.eithon.plugin.bungee.test;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import junit.framework.Assert;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.bungee.db.DbWarpLocation;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.Test;

public class TestDbWarpLocation {

	@Test
	public void create() {
		String name = "warp11";
		String bungeeServerName = "a";
		Location location = new Location(null, 1, 2, 3, 4, 5);
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbWarpLocation warpLocation = DbWarpLocation.create(database, name, bungeeServerName, location);
		assertEquals(name, warpLocation.getName());
		assertEquals(bungeeServerName, warpLocation.getBungeeServerName());
		assertEquals(location, warpLocation.getLocation());
	}	
	
	@Test
	public void getByName() {
		String name = "warp12";
		String bungeeServerName = "a";
		Location location = new Location(null, 1, 2, 3, 4, 5);
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbWarpLocation warpLocation = DbWarpLocation.create(database, name, bungeeServerName, location);
		warpLocation = DbWarpLocation.getByName(database, name);
		Assert.assertNotNull(warpLocation);
		assertEquals(name, warpLocation.getName());
		assertEquals(bungeeServerName, warpLocation.getBungeeServerName());
		assertEquals(location, warpLocation.getLocation());
	}	
	
	@Test
	public void update() {
		String name = "warp13";
		String bungeeServerName = "a";
		Location location = new Location(null, 1, 2, 3, 4, 5);
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbWarpLocation warpLocation = DbWarpLocation.create(database, name, bungeeServerName, location);
		warpLocation = DbWarpLocation.getByName(database, name);
		bungeeServerName = bungeeServerName + "2";
		warpLocation.updateBungeeServerName(bungeeServerName);
		warpLocation = DbWarpLocation.getByName(database, name);
		assertEquals(bungeeServerName, warpLocation.getBungeeServerName());
	}

}
