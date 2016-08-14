package net.eithon.plugin.bungee.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import net.eithon.library.mysql.Database;

import org.junit.Assert;

public class TestSupport {
	public static Database getDatabaseAndTruncateTables() {
		Database database = new Database("rookgaard.eithon.net", "3307", "DEV_e_bungee", "DEV_e_plugin", "J5FE9EFCD1GX8tjg");
		
		try {
			Connection connection = database.getOrOpenConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM `warp_location` WHERE 1=1");
			statement.executeUpdate("DELETE FROM `player` WHERE 1=1");
			statement.executeUpdate("DELETE FROM `server_ban` WHERE 1=1");
			return database;
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
		return null;
	}
}
