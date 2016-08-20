package net.eithon.plugin.bungee.test;

import static org.junit.Assert.assertEquals;
import junit.framework.Assert;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.bungee.db.WarpLocationTable;
import net.eithon.plugin.bungee.db.WarpLocationRow;

import org.junit.Test;

public class TestDbWarpLocation {

	@Test
	public void create() {
		try {
			String name = "warp11";
			String bungeeServerName = "a";
			String location = "location 11";
			Database database = TestSupport.getDatabaseAndTruncateTables();
			WarpLocationTable handler = new WarpLocationTable(database);
			WarpLocationRow warpLocation = handler.create(name, bungeeServerName, location);
			assertEquals(name, warpLocation.name);
			assertEquals(bungeeServerName, warpLocation.bungee_server_name);
			assertEquals(location, warpLocation.location);
		} catch (Exception e) {
			Assert.fail();
		}
	}	

	@Test
	public void getByName() {
		try {
			String name = "warp12";
			String bungeeServerName = "a";
			String location = "location 12";
			Database database = TestSupport.getDatabaseAndTruncateTables();
			WarpLocationTable handler = new WarpLocationTable(database);
			WarpLocationRow warpLocation = handler.create(name, bungeeServerName, location);
			warpLocation = handler.getByName(name);
			Assert.assertNotNull(warpLocation);
			assertEquals(name, warpLocation.name);
			assertEquals(bungeeServerName, warpLocation.bungee_server_name);
			assertEquals(location, warpLocation.location);
		} catch (Exception e) {
			Assert.fail();
		}
	}	

	@Test
	public void update() {
		try {
			String name = "warp13";
			String bungeeServerName = "a";
			String location = "location 13";
			Database database = TestSupport.getDatabaseAndTruncateTables();
			WarpLocationTable handler = new WarpLocationTable(database);
			WarpLocationRow warpLocation = handler.create(name, bungeeServerName, location);
			warpLocation = handler.getByName(name);
			bungeeServerName = bungeeServerName + "2";
			warpLocation.bungee_server_name = bungeeServerName;
			location = location + "2";
			warpLocation.location = location;
			handler.update(warpLocation);
			warpLocation = handler.getByName(name);
			assertEquals(bungeeServerName, warpLocation.bungee_server_name);
			assertEquals(location, warpLocation.location);
		} catch (Exception e) {
			Assert.fail();
		}
	}

}
