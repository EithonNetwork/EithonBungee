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

	public static final String JOIN_EVENT = "JoinEvent";
	public static final String LEAVE_EVENT = "LeaveEvent";
	private String _serverName;
	private BungeeController _bungeeController;

	public JoinLeaveController(EithonPlugin eithonPlugin, BungeeController bungeeController, String serverName) {
		this._bungeeController = bungeeController;
		this._serverName = serverName;
	}
	
	public void publishJoinEventOnThisServer(JSONObject data) {
		JoinLeaveInfo info = JoinLeaveInfo.getFromJson(data);
		EithonBungeeJoinEvent e = new EithonBungeeJoinEvent(_serverName, info.getServerName(), 
				info.getPlayerId(), info.getPlayerName(), info.getMainGroup(), info.getIsNewOnServer());
		Bukkit.getServer().getPluginManager().callEvent(e);			
	}

	public void publishLeaveEventOnThisServer(JSONObject data) {
		JoinLeaveInfo info = JoinLeaveInfo.getFromJson(data);
		publishLeaveEventOnThisServer(
				info.getServerName(), 
				info.getPlayerId(), 
				info.getPlayerName(), 
				info.getMainGroup());			
	}

	public void playerLeftOnAnotherServer(String serverName, UUID playerId,
			String playerName) {
		publishLeaveEventOnThisServer(serverName, playerId, playerName, null);
	}

	private void publishLeaveEventOnThisServer(String serverName, UUID playerId,
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

	public void sendJoinEventToOtherServers(Player player) {
		sendEventToOtherServers(player, JOIN_EVENT);
	}

	public void sendLeaveEventToOtherServers(Player player) {
		sendEventToOtherServers(player, LEAVE_EVENT);
	}

	private void sendEventToOtherServers(Player player, String eventName) {
		if (player == null) return;
		String mainGroup = getHighestGroup(player.getUniqueId());
		JoinLeaveInfo info = new JoinLeaveInfo(_serverName, player.getUniqueId(), player.getName(), mainGroup);
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
