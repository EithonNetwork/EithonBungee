package net.eithon.plugin.bungee.logic.individualmessage;

import java.util.HashMap;

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

	public void broadcastPlayerJoined(String serverName, String fromServerName, String playerName, String groupName) {
		if (fromServerName == null) {
			broadcastMessage(Config.M.joinMessage, serverName, null, playerName, groupName);
		} else {
			broadcastMessage(Config.M.switchMessage, serverName, fromServerName, playerName, groupName);
		}
	}

	public void broadcastPlayerQuit(String serverName, String playerName, String groupName) {
		broadcastMessage(Config.M.quitMessage, serverName, serverName, playerName, groupName);
	}

	public String getJoinMessage(String serverName, String fromServerName, String playerName, String groupName) {
		if (fromServerName == null) {
			return getIndividualMessage(Config.M.joinMessage, null, serverName, playerName, groupName);
		} else {
			return getIndividualMessage(Config.M.switchMessage, serverName, fromServerName, playerName, groupName);
		}
	}

	public String getQuitMessage(String serverName, String playerName, String groupName) {
		return getIndividualMessage(Config.M.quitMessage, serverName, serverName, playerName, groupName);
	}

	private void broadcastMessage(IndividualConfigurableMessage message, String serverName, String fromServerName, String playerName, String groupName) {
		verbose("broadCastMessage", "Enter, serverName =%s, fromServerName = %s, Player = %s, group = %s", 
				serverName, fromServerName, playerName, groupName);
		ConfigurableMessage configurableMessage = getConfigurableMessage(message, serverName, fromServerName,
				playerName, groupName);
		if (configurableMessage == null) return;
		HashMap<String,String> namedArguments = getNamedArguments(serverName, fromServerName, playerName);
		configurableMessage.broadcastMessage(namedArguments);
		verbose("broadCastMessage", "Leave");
	}

	private String getIndividualMessage(IndividualConfigurableMessage message, String serverName, String fromServerName, String playerName, String groupName) {
		verbose("getIndividualMessage", "Enter, serverName =%s, Player = %s, group = %s", 
				serverName, playerName, groupName);
		ConfigurableMessage configurableMessage = getConfigurableMessage(message, serverName, fromServerName,
				playerName, groupName);
		if (configurableMessage == null) return null;
		HashMap<String,String> namedArguments = getNamedArguments(serverName, fromServerName, playerName);
		verbose("getIndividualMessage", "Leave");
		return configurableMessage.getMessageWithColorCoding(namedArguments);
	}

	private ConfigurableMessage getConfigurableMessage(
			IndividualConfigurableMessage message, String serverName,
			String fromServerName, String playerName, String groupName) {
		verbose("getConfigurableMessage", "Enter, serverName =%s, Player = %s, group = %s", 
				serverName, playerName, groupName);
		ConfigurableMessage configurableMessage = message.getMessage(playerName, groupName);
		if (configurableMessage == null) {
			verbose("getConfigurableMessage", "Leave, No configurable message", playerName);
			return null;
		}
		verbose("getConfigurableMessage", "Leave");
		return configurableMessage;
	}

	private HashMap<String, String> getNamedArguments(String serverName,
			String fromServerName, String playerName) {
		HashMap<String,String> namedArguments = new HashMap<String, String>();
		namedArguments.put("PLAYER_NAME", playerName);
		namedArguments.put("SERVER_NAME", serverName);
		namedArguments.put("PREVIOUS_SERVER_NAME", fromServerName);
		return namedArguments;
	}

	private void verbose(String method, String format, Object... args) {
		String message = String.format(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "IndividualMessageController.%s: %s", method, message);
	}
}
