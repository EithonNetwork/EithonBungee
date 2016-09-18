package net.eithon.plugin.bungee;

import java.util.List;

import net.eithon.library.extensions.EithonPlugin;
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
		public static long maxAllowedTeleportDelayInSeconds;
		public static long maxAllowedMessageDelayInSeconds;
		public static List<String> groupPriorities;
		public static double reloadWarpLocationsAfterSeconds;
		public static String primaryBungeeServer;
		public static List<String> bungeeServerNames;
		public static String thisBungeeServerName;
		public static long secondsBetweenHeartBeats;
		public static boolean mandatoryPlayerNames;
		public static String databaseUrl;
		public static String databaseUsername;
		public static String databasePassword;

		static void load(Configuration config) {
			secondsBetweenHeartBeats = config.getSeconds("TimeSpanBetweenHeartBeats", 10);
			maxAllowedTeleportDelayInSeconds = config.getSeconds("MaxAllowedTeleportDelayTimeSpan", 30);
			maxAllowedMessageDelayInSeconds = config.getSeconds("MaxAllowedMessageDelayInSeconds", 10);
			groupPriorities = config.getStringList("GroupPriorities");
			reloadWarpLocationsAfterSeconds = config.getSeconds("ReloadWarpLocationsAfterTimeSpan", "5m");
			primaryBungeeServer = config.getString("PrimaryBungeeServer", "Hub");
			bungeeServerNames = config.getStringList("BungeeServers");
			thisBungeeServerName = config.getString("ThisBungeeServer", "Hub");
			mandatoryPlayerNames = config.getBoolean("MandatoryPlayerNames", true);
			getDatabase(config);
		}

		private static void getDatabase(Configuration config) {
			final String databaseHostname = config.getString("database.Hostname", null);
			final String databasePort = config.getString("database.Port", null);
			final String databaseName = config.getString("database.Name", null);
			databaseUrl = "jdbc:mysql://" + databaseHostname + ":" + databasePort + "/" + databaseName;
			databaseUsername = config.getString("database.Username", null);
			databasePassword = config.getString("database.Password", null);
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
		public static ConfigurableMessage tryAgain;
		public static ConfigurableMessage bannedPlayer;
		public static ConfigurableMessage unbannedPlayer;
		public static ConfigurableMessage playerNotBanned;
		public static IndividualConfigurableMessage joinMessage;
		public static IndividualConfigurableMessage quitMessage;
		public static IndividualConfigurableMessage switchMessage;

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
					"Warp location %s has been added.");
			tryAgain = config.getConfigurableMessage(
					"messages.TryAgain", 0, 
					"Could not execute the command properly now. Please try again.");
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
			bannedPlayer = config.getConfigurableMessage("messages.BannedPlayer", 3,
					"Player %s is now banned on server %s until %s.");
			unbannedPlayer = config.getConfigurableMessage("messages.UnbannedPlayer", 2,
					"Player %s has been unbanned on server %s.");
			playerNotBanned = config.getConfigurableMessage("messages.PlayerNotBanned", 2,
					"Player %s is not banned on server %s?");
			joinMessage = new IndividualConfigurableMessage(config, "messages.join");
			quitMessage = new IndividualConfigurableMessage(config, "messages.quit");
			switchMessage = new IndividualConfigurableMessage(config, "messages.switch");
		}		
	}

}
