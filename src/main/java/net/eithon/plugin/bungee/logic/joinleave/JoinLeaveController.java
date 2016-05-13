package net.eithon.plugin.bungee.logic.joinleave;

import java.util.UUID;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.facades.PermissionsFacade;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.bungee.logic.bungeecord.BungeeController;
import net.eithon.plugin.eithonlibrary.Config;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class JoinLeaveController {

	public static final String JOIN_EVENT = "JoinEvent";
	public static final String LEAVE_EVENT = "LeaveEvent";
	private EithonPlugin _eithonPlugin;
	private String _serverName;
	private BungeeController _bungeeController;

	public JoinLeaveController(EithonPlugin eithonPlugin, BungeeController bungeeController, String serverName) {
		this._eithonPlugin = eithonPlugin;
		this._bungeeController = bungeeController;
		this._serverName = serverName;
	}
	
	public void publishJoinEventOnThisServer(JSONObject data) {
		JoinQuitInfo info = JoinQuitInfo.getFromJson(data);
		EithonBungeeJoinEvent e = new EithonBungeeJoinEvent(_serverName, info.getServerName(), 
				info.getPlayerId(), info.getPlayerName(), info.getMainGroup());
		Bukkit.getServer().getPluginManager().callEvent(e);			
	}

	public void publishLeaveEventOnThisServer(JSONObject data) {
		JoinQuitInfo info = JoinQuitInfo.getFromJson(data);
		EithonBungeeLeaveEvent e = new EithonBungeeLeaveEvent(_serverName, info.getServerName(), 
				info.getPlayerId(), info.getPlayerName(), info.getMainGroup());
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
		verbose("sendEventToOtherServers", "Enter, player = %s", player == null ? "NULL" : player.getName());
		if (player == null) return;
		String mainGroup = getHighestGroup(player.getUniqueId());
		verbose("sendEventToOtherServers", String.format("mainGroup=%s", mainGroup));
		JoinQuitInfo info = new JoinQuitInfo(_serverName, player.getUniqueId(), player.getName(), mainGroup);
		this._bungeeController.sendDataToAll(eventName, info, true);
		verbose("sendEventToOtherServers", "Leave");
	}

	private void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "BungeeController.%s: %s", method, message);
	}
}
