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
		public static ConfigurableMessage waitForCoolDown;
		public static ConfigurableMessage mustBeInFreebuildWord;
		public static ConfigurableMessage alreadyOn;
		public static ConfigurableMessage alreadyOff;
		public static ConfigurableMessage activated;
		public static ConfigurableMessage deactivated;
		public static ConfigurableMessage notInCoolDown;
		public static ConfigurableMessage releasedFromCoolDown;

		static void load(Configuration config) {
			waitForCoolDown = config.getConfigurableMessage(
					"messages.WaitForCoolDown", 2, 
					"The remaining cool down period for switching Freebuild mode is %d minutes and %d seconds.");
			mustBeInFreebuildWord = config.getConfigurableMessage(
					"messages.MustBeInFreebuildWord", 0, 
					"You can only switch between survival and bungee in the SurvivalFreebuild world.");
			alreadyOn = config.getConfigurableMessage(
					"messages.AlreadyOn_0", 0, 
					"Freebuild mode is already active.");
			alreadyOff = config.getConfigurableMessage(
					"messages.AlreadyOff_0", 0, 
					"Survival mode is already active (bungee is OFF).");
			activated = config.getConfigurableMessage(
					"messages.Activated_0", 0, 
					"Freebuild mode is now active.");
			deactivated = config.getConfigurableMessage(
					"messages.Deactivated_0", 0, 
					"Survival mode is now active (bungee is OFF).");
			notInCoolDown = config.getConfigurableMessage(
					"messages.NotInCoolDown_1", 1, 
					"The player %s was not in cool down.");
			releasedFromCoolDown = config.getConfigurableMessage(
					"messages.ReleasedFromCoolDown_1", 1, 
					"The player %s was released from cool down.");
		}		
	}

}
