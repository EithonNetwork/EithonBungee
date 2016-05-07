package net.eithon.plugin.bungee.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.MySql;

import org.junit.Assert;

public class TestSupport {
	public static Database getDatabaseAndTruncateTables() {
		MySql mySql = new MySql("rookgaard.eithon.net", "3307", "DEV_e_bungee", "DEV_e_plugin", "J5FE9EFCD1GX8tjg");
		try {
			Connection connection = mySql.getOrOpenConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM `warp_location` WHERE 1=1");
			statement.executeUpdate("DELETE FROM `player` WHERE 1=1");
			return mySql;
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
		return null;
	}
}
