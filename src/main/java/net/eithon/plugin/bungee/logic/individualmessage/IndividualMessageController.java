package net.eithon.plugin.bungee.logic.individualmessage;

import java.util.HashMap;

import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.bungee.Config;

public class IndividualMessageController {
	private EithonPlugin _eithonPlugin;

	public IndividualMessageController(EithonPlugin eithonPlugin)
	{
		this._eithonPlugin = eithonPlugin;
	}
	
	public void broadcastPlayerJoined(String serverName, EithonPlayer player, String groupName) {
		broadcastMessage(Config.M.joinMessage, serverName, player, groupName);
	}
	
	public void broadcastPlayerQuit(String serverName, EithonPlayer player, String groupName) {
		broadcastMessage(Config.M.quitMessage, serverName, player, groupName);
	}
	
	public String getJoinMessage(String serverName, EithonPlayer player, String groupName) {
		return getIndividualMessage(Config.M.joinMessage, serverName, player, groupName);
	}
	
	public String getQuitMessage(String serverName, EithonPlayer player, String groupName) {
		return getIndividualMessage(Config.M.quitMessage, serverName, player, groupName);
	}
	
	private void broadcastMessage(IndividualConfigurableMessage message, String serverName, EithonPlayer player, String groupName) {
		verbose("broadCastMessage", "Enter, serverName =%s, Player = %s, group = %s", 
				serverName, player == null ? "Unknown" : player.getName(), groupName);
		String playerName = player == null ? "Unknown" : player.getName();
		ConfigurableMessage configurableMessage = message.getMessage(playerName, groupName);
		if (configurableMessage == null) {
			verbose("broadCastMessage", "Leave, No configurable message", player.getName());
			return;
		}
		HashMap<String,String> namedArguments = new HashMap<String, String>();
		namedArguments.put("PLAYER_NAME", playerName);
		namedArguments.put("SERVER_NAME", serverName);
		configurableMessage.broadcastMessage(namedArguments);
		verbose("broadCastMessage", "Leave");
	}
	
	private String getIndividualMessage(IndividualConfigurableMessage message, String serverName, EithonPlayer player, String groupName) {
		verbose("getIndividualMessage", "Enter, serverName =%s, Player = %s, group = %s", 
				serverName, player == null ? "Unknown" : player.getName(), groupName);
		String playerName = player == null ? "Unknown" : player.getName();
		ConfigurableMessage configurableMessage = message.getMessage(playerName, groupName);
		if (configurableMessage == null) {
			verbose("getIndividualMessage", "Leave, No configurable message", player.getName());
			return null;
		}
		HashMap<String,String> namedArguments = new HashMap<String, String>();
		namedArguments.put("PLAYER_NAME", playerName);
		namedArguments.put("SERVER_NAME", serverName);
		verbose("getIndividualMessage", "Leave");
		return configurableMessage.getMessageWithColorCoding(namedArguments);
	}

	private void verbose(String method, String format, Object... args) {
		String message = String.format(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "IndividualMessageController.%s: %s", method, message);
	}
}
