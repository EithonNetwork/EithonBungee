package net.eithon.plugin.bungee.test;

import static org.junit.Assert.assertEquals;
import junit.framework.Assert;
import net.eithon.library.db.Database;
import net.eithon.plugin.bungee.db.DbWarpLocation;

import org.junit.Test;

public class TestDbWarpLocation {

	@Test
	public void create() {
		String name = "warp11";
		String bungeeServerName = "a";
		String location = "location 11";
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
		String location = "location 12";
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
		String location = "location 13";
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbWarpLocation warpLocation = DbWarpLocation.create(database, name, bungeeServerName, location);
		warpLocation = DbWarpLocation.getByName(database, name);
		bungeeServerName = bungeeServerName + "2";
		location = location + "2";
		warpLocation.update(bungeeServerName, location);
		warpLocation = DbWarpLocation.getByName(database, name);
		assertEquals(bungeeServerName, warpLocation.getBungeeServerName());
		assertEquals(location, warpLocation.getLocation());
	}

}
