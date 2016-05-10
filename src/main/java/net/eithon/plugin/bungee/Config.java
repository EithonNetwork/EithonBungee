package net.eithon.plugin.bungee;

import java.util.List;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.MySql;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;
import net.eithon.plugin.bungee.logic.individualmessage.IndividualConfigurableMessage;

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
		public static long maxAllowedMessageDelayInSeconds;
		public static List<String> groupPriorities;
		public static double reloadWarpLocationsAfterSeconds;
		public static String primaryBungeeServer;

		static void load(Configuration config) {
			database = null;
			maxAllowedTeleportDelayInSeconds = config.getSeconds("MaxAllowedTeleportDelayTimeSpan", 30);
			maxAllowedMessageDelayInSeconds = config.getSeconds("MaxAllowedMessageDelayInSeconds", 10);
			groupPriorities = config.getStringList("GroupPriorities");
			database = getDatabase(config);
			reloadWarpLocationsAfterSeconds = config.getSeconds("ReloadWarpLocationsAfterTimeSpan", "5m");
			primaryBungeeServer = config.getString("PrimaryBungeeServer", "Hub");
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

		static void load(Configuration config) {
		}

	}
	public static class M {
		public static ConfigurableMessage requestTpTo;
		public static ConfigurableMessage requestTpHere;
		public static ConfigurableMessage denyTpTo;
		public static ConfigurableMessage denyTpHere;
		public static ConfigurableMessage messageSent;
		public static ConfigurableMessage messageFrom;
		public static ConfigurableMessage warpAdded;
		public static ConfigurableMessage alreadyConnectedToServer;
		public static ConfigurableMessage couldNotConnectToServer;
		public static ConfigurableMessage connectedToServer;
		public static ConfigurableMessage joinedServerFirstTime;
		public static ConfigurableMessage pleaseWelcomeNewPlayer;
		public static IndividualConfigurableMessage joinMessage;
		public static IndividualConfigurableMessage quitMessage;

		static void load(Configuration config) {
			requestTpTo = config.getConfigurableMessage(
					"messages.RequestTpTo", 1, 
					"Player %s requests to teleport to you. Enter one of the commands \"/eithonbungee tp accept\" or \"/eithonbungee tp deny\".");
			requestTpHere = config.getConfigurableMessage(
					"messages.RequestTpHere", 2, 
					"Player %s requests to teleport you to him/her. Enter one of the commands \"/eithonbungee tp accept\" or \"/eithonbungee tp deny\".");
			denyTpTo = config.getConfigurableMessage(
					"messages.DenyTpTo", 1, 
					"Player %s denied you to teleport to him/her.");
			denyTpHere = config.getConfigurableMessage(
					"messages.DenyTpHere", 1, 
					"Player %s denied to be teleported to you.");
			messageSent = config.getConfigurableMessage(
					"messages.MessageSent", 2, 
					"To %s: %s");
			messageFrom = config.getConfigurableMessage(
					"messages.MessageFrom", 2, 
					"From %s: %s");
			warpAdded = config.getConfigurableMessage(
					"messages.WarpAdded", 1, 
					"Warp locaiton %s has been added.");
			alreadyConnectedToServer = config.getConfigurableMessage("messages.AlreadyConnectedToServer", 1,
					"You are already connected to server %s.");
			couldNotConnectToServer = config.getConfigurableMessage("messages.CouldNotConnectToServer", 2,
					"Could not connect to server %s: %s");
			connectedToServer = config.getConfigurableMessage("messages.ConnectedToServer", 1,
					"Connected to server %s.");
			joinedServerFirstTime = config.getConfigurableMessage("messages.JoinedServerFirstTime", 1,
					"%s joined for the first time!");
			pleaseWelcomeNewPlayer = config.getConfigurableMessage("messages.PleaseWelcomeNewPlayer", 1,
					"Welcome %s to the server!");
			joinMessage = new IndividualConfigurableMessage(config, "messages.join");
			quitMessage = new IndividualConfigurableMessage(config, "messages.quit");
		}		
	}

}
