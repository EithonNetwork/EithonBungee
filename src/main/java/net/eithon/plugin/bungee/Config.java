package net.eithon.plugin.bungee;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.ConfigurableCommand;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Config {
	public static void load(EithonPlugin plugin)
	{
		Configuration config = plugin.getConfiguration();
		V.load(config);
		C.load(config);
		M.load(config);

	}
	public static class V {
		public static Location botLocation;
		public static String botName;
		public static String botUUID;

		static void load(Configuration config) {
			botName = config.getString("BotName", "EithonBungee");
			botUUID = config.getString("BotUUID", "afa12010-b168-11e5-bf7f-feff819cdc9f");
			String worldName = config.getString("BotWorld", "world");
			World world = Bukkit.getWorld(worldName);
			botLocation = world.getSpawnLocation();
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
