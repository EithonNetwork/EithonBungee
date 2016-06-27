package net.eithon.plugin.bungee.logic.joinleave;

import java.util.UUID;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.facades.PermissionsFacade;
import net.eithon.library.plugin.PluginMisc;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.bungee.Config;
import net.eithon.plugin.bungee.logic.bungeecord.BungeeController;
import net.eithon.plugin.stats.EithonStatsApi;
import net.eithon.plugin.stats.EithonStatsPlugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;

public class JoinLeaveController {

	public static final String SWITCH_EVENT = "SwitchEvent";
	public static final String JOIN_EVENT = "JoinEvent";
	public static final String LEAVE_EVENT = "LeaveEvent";
	private BungeeController _bungeeController;
	private EithonPlugin _eithonPlugin;
	private EithonStatsPlugin _eithonStatsPlugin;

	public JoinLeaveController(EithonPlugin eithonPlugin, BungeeController bungeeController) {
		this._eithonPlugin = eithonPlugin;
		this._bungeeController = bungeeController;
		this._eithonStatsPlugin = connectToStats(eithonPlugin);
	}

	private EithonStatsPlugin connectToStats(EithonPlugin eithonPlugin) {
		Plugin plugin = PluginMisc.getPlugin("EithonStats");
		if (plugin != null && plugin.isEnabled() && (plugin instanceof EithonStatsPlugin)) {
			eithonPlugin.logInfo("Succesfully hooked into the EithonStats plugin!");
			return ((EithonStatsPlugin) plugin);
		} else {
			eithonPlugin.logWarn("The EithonStats plugin was not found.");
			return null;
		}
	}

	public void playerJoinedThisServer(Player player) {
		sendJoinEventToOtherServers(player);
		publishJoinEventOnThisServer(player);	
	}

	public void playerSwitchedToThisServer(Player player, String previousServername) {
		sendSwitchEventToOtherServers(player, previousServername);
		publishSwitchEventOnThisServer(player, previousServername);	
	}
	
	public void publishJoinEventOnThisServer(JSONObject data) {
		JoinLeaveInfo info = JoinLeaveInfo.getFromJson(data);
		EithonBungeeJoinEvent e = new EithonBungeeJoinEvent(Config.V.thisBungeeServerName, info.getToServerName(), 
				info.getPlayerId(), info.getPlayerName(), info.getMainGroup());
		if (info.getIsFirstJoinToday()) e.setIsFirstJoinToday();
		if (info.getIsNewOnServer()) e.setIsNewOnServer();
		Bukkit.getServer().getPluginManager().callEvent(e);			
	}
	
	public void publishSwitchEventOnThisServer(JSONObject data) {
		JoinLeaveInfo info = JoinLeaveInfo.getFromJson(data);
		EithonBungeeSwitchEvent e = new EithonBungeeSwitchEvent(Config.V.thisBungeeServerName, info.getFromServerName(), info.getToServerName(), 
				info.getPlayerId(), info.getPlayerName(), info.getMainGroup());
		Bukkit.getServer().getPluginManager().callEvent(e);			
	}
	
	private void publishJoinEventOnThisServer(Player player) {
		verbose("publishJoinEventOnThisServer", "Player=%s", player.getName());
		String mainGroup = getHighestGroup(player.getUniqueId());
		EithonBungeeJoinEvent e = new EithonBungeeJoinEvent(Config.V.thisBungeeServerName, Config.V.thisBungeeServerName, 
				player.getUniqueId(), player.getName(), mainGroup);
		if (!player.hasPlayedBefore()) e.setIsNewOnServer();
		if ((this._eithonStatsPlugin != null)
				&& (EithonStatsApi.isFirstIntervalToday(player))) {
			e.setIsFirstJoinToday();
		}	
		Bukkit.getServer().getPluginManager().callEvent(e);			
	}
	
	private void publishSwitchEventOnThisServer(Player player, String previousServer) {
		verbose("publishSwitchEventOnThisServer", "Player=%s", player.getName());
		String mainGroup = getHighestGroup(player.getUniqueId());
		EithonBungeeSwitchEvent e = new EithonBungeeSwitchEvent(Config.V.thisBungeeServerName, previousServer, Config.V.thisBungeeServerName, 
				player.getUniqueId(), player.getName(), mainGroup);
		Bukkit.getServer().getPluginManager().callEvent(e);			
	}

	public void publishLeaveEventOnThisServer(JSONObject data) {
		JoinLeaveInfo info = JoinLeaveInfo.getFromJson(data);
		publishLeaveEventOnThisServer(
				info.getFromServerName(),
				info.getPlayerId(), 
				info.getPlayerName(), 
				info.getMainGroup());			
	}

	public void publishLeaveEventOnThisServer(String serverName, UUID playerId,
			String playerName, String mainGroup) {
		EithonBungeeLeaveEvent e = new EithonBungeeLeaveEvent(Config.V.thisBungeeServerName, serverName, 
				playerId, playerName, mainGroup);
		Bukkit.getServer().getPluginManager().callEvent(e);
	}

	public String getHighestGroup(UUID playerId) {
		String[] currentGroups = PermissionsFacade.getPlayerPermissionGroups(playerId);
		for (String priorityGroup : Config.V.groupPriorities) {
			for (String playerGroup : currentGroups) {
				if (playerGroup.equalsIgnoreCase(priorityGroup)) {
					verbose("getHighestGroup", "Highest group: %s", priorityGroup);
					return priorityGroup;
				}
			}
		}
		return null;
	}

	private void sendSwitchEventToOtherServers(Player player, String fromServer) {
		sendEventToOtherServers(SWITCH_EVENT, player, fromServer, Config.V.thisBungeeServerName);
	}

	private void sendJoinEventToOtherServers(Player player) {
		sendEventToOtherServers(JOIN_EVENT, player, null, Config.V.thisBungeeServerName);
	}

	public void sendLeaveEventToOtherServers(Player player) {
		sendEventToOtherServers(LEAVE_EVENT, player, Config.V.thisBungeeServerName, null);
	}

	private void sendEventToOtherServers(String eventName, Player player, String fromServerName, String toServerName) {
		if (player == null) return;
		String mainGroup = getHighestGroup(player.getUniqueId());
		JoinLeaveInfo info = new JoinLeaveInfo(fromServerName, toServerName, player.getUniqueId(), player.getName(), mainGroup);
		if (!player.hasPlayedBefore()) info.setIsNewOnServer();
		if ((this._eithonStatsPlugin != null)
				&& (EithonStatsApi.isFirstIntervalToday(player))) {
			info.setIsFirstJoinToday();
		}
		this._bungeeController.sendDataToAll(eventName, info, true);
	}

	private void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "JoinLeaveController.%s: %s", method, message);
	}
}
