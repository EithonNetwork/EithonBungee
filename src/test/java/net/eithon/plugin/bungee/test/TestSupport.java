package net.eithon.plugin.bungee.test;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;

import org.junit.Assert;

public class TestSupport {
	public static Database getDatabaseAndTruncateTables() {
		Database database = new Database("rookgaard.eithon.net", "3307", "DEV_e_bungee", "DEV_e_plugin", "J5FE9EFCD1GX8tjg");
		
		try {
			database.executeUpdate("DELETE FROM `warp_location` WHERE 1=1");
			database.executeUpdate("DELETE FROM `player` WHERE 1=1");
			database.executeUpdate("DELETE FROM `server_ban` WHERE 1=1");
			return database;
		} catch (FatalException | TryAgainException e) {
			e.printStackTrace();
			Assert.fail();
		}
		return null;
	}
}
