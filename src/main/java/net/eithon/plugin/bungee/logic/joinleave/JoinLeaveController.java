package net.eithon.plugin.bungee.logic.joinleave;

import java.util.UUID;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.facades.PermissionsFacade;
import net.eithon.plugin.bungee.logic.bungeecord.BungeeController;
import net.eithon.plugin.eithonlibrary.Config;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class JoinLeaveController {

	public static final String SWITCH_EVENT = "SwitchEvent";
	public static final String JOIN_EVENT = "JoinEvent";
	public static final String LEAVE_EVENT = "LeaveEvent";
	private String _serverName;
	private BungeeController _bungeeController;

	public JoinLeaveController(EithonPlugin eithonPlugin, BungeeController bungeeController, String serverName) {
		this._bungeeController = bungeeController;
		this._serverName = serverName;
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
		EithonBungeeJoinEvent e = new EithonBungeeJoinEvent(this._serverName, info.getToServerName(), 
				info.getPlayerId(), info.getPlayerName(), info.getMainGroup(), info.getIsNewOnServer());
		Bukkit.getServer().getPluginManager().callEvent(e);			
	}
	
	public void publishSwitchEventOnThisServer(JSONObject data) {
		JoinLeaveInfo info = JoinLeaveInfo.getFromJson(data);
		EithonBungeeSwitchEvent e = new EithonBungeeSwitchEvent(this._serverName, info.getFromServerName(), info.getToServerName(), 
				info.getPlayerId(), info.getPlayerName(), info.getMainGroup());
		Bukkit.getServer().getPluginManager().callEvent(e);			
	}
	
	private void publishJoinEventOnThisServer(Player player) {
		String mainGroup = getHighestGroup(player.getUniqueId());
		EithonBungeeJoinEvent e = new EithonBungeeJoinEvent(this._serverName, this._serverName, 
				player.getUniqueId(), player.getName(), mainGroup, !player.hasPlayedBefore());
		Bukkit.getServer().getPluginManager().callEvent(e);			
	}
	
	private void publishSwitchEventOnThisServer(Player player, String previousServer) {
		String mainGroup = getHighestGroup(player.getUniqueId());
		EithonBungeeSwitchEvent e = new EithonBungeeSwitchEvent(this._serverName, previousServer, this._serverName, 
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
		EithonBungeeLeaveEvent e = new EithonBungeeLeaveEvent(_serverName, serverName, 
				playerId, playerName, mainGroup);
		Bukkit.getServer().getPluginManager().callEvent(e);
	}

	public static String getHighestGroup(UUID playerId) {
		String[] currentGroups = PermissionsFacade.getPlayerPermissionGroups(playerId);
		for (String priorityGroup : Config.V.groupPriorities) {
			for (String playerGroup : currentGroups) {
				if (playerGroup.equalsIgnoreCase(priorityGroup)) {
					return priorityGroup;
				}
			}
		}
		return null;
	}

	private void sendSwitchEventToOtherServers(Player player, String fromServer) {
		sendEventToOtherServers(SWITCH_EVENT, player, fromServer, this._serverName);
	}

	private void sendJoinEventToOtherServers(Player player) {
		sendEventToOtherServers(JOIN_EVENT, player, null, this._serverName);
	}

	public void sendLeaveEventToOtherServers(Player player) {
		sendEventToOtherServers(LEAVE_EVENT, player, this._serverName, null);
	}

	private void sendEventToOtherServers(String eventName, Player player, String fromServerName, String toServerName) {
		if (player == null) return;
		String mainGroup = getHighestGroup(player.getUniqueId());
		JoinLeaveInfo info = new JoinLeaveInfo(fromServerName, toServerName, player.getUniqueId(), player.getName(), mainGroup);
		if (!player.hasPlayedBefore()) info.setIsNewOnServer();
		this._bungeeController.sendDataToAll(eventName, info, true);
	}

	/*
	private void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "JoinLeaveController.%s: %s", method, message);
	}
	*/
}
