package net.eithon.plugin.bungee;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.MySql;
import net.eithon.library.plugin.ConfigurableCommand;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;

public class Config {
	public static void load(EithonPlugin plugin)
	{
		Configuration config = plugin.getConfiguration();
		V.load(config);
		C.load(config);
		M.load(config);

	}
	
	public static class V {
		public static Database database;
		public static long maxAllowedTeleportDelayInSeconds;

		static void load(Configuration config) {
			database = null;
			maxAllowedTeleportDelayInSeconds = config.getSeconds("MaxAllowedTeleportDelayTimeSpan", 30);
			database = getDatabase(config);
		}

		private static Database getDatabase(Configuration config) {
			final String databaseHostname = config.getString("database.Hostname", null);
			final String databasePort = config.getString("database.Port", null);
			final String databaseName = config.getString("database.Name", null);
			final String databaseUsername = config.getString("database.Username", null);
			final String databasePassword = config.getString("database.Password", null);
			return new MySql(databaseHostname, databasePort, databaseName,
					databaseUsername, databasePassword);
		}

	}
	public static class C {
		public static ConfigurableCommand setSpeed;
		public static ConfigurableCommand stopFly;

		static void load(Configuration config) {
			setSpeed = config.getConfigurableCommand("commands.SetSpeed", 2,
					"speed fly %.2f %s");
			stopFly = config.getConfigurableCommand("commands.StopFly", 0,
					"fly");
		}

	}
	public static class M {
		public static ConfigurableMessage requestTpTo;
		public static ConfigurableMessage requestTpHere;
		public static ConfigurableMessage denyTpTo;
		public static ConfigurableMessage denyTpHere;

		static void load(Configuration config) {
			requestTpTo = config.getConfigurableMessage(
					"messages.RequestTpTo", 1, 
					"Player %s requests to teleport to you. Enter one of the commands \"/eithonbungee tpa accept\" or \"/eithonbungee tpa deny\".");
			requestTpHere = config.getConfigurableMessage(
					"messages.RequestTpHere", 2, 
					"Player %s requests to teleport you to him/her. Enter one of the commands \"/eithonbungee tpahere accept\" or \"/eithonbungee tpahere deny\".");
			denyTpTo = config.getConfigurableMessage(
					"messages.DenyTpTo", 1, 
					"Player %s denied you to teleport to him/her.");
			denyTpHere = config.getConfigurableMessage(
					"messages.DenyTpHere", 1, 
					"Player %s denied to be teleported to you.");
		}		
	}

}
