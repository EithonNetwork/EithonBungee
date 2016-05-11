package net.eithon.plugin.bungee.logic.individualmessage;

import java.util.HashMap;

import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;

public class IndividualConfigurableMessage {

	private Configuration _config;
	private String _configurationPath;
	private ConfigurableMessage _defaultMessage;
	private HashMap<String,ConfigurableMessage> _playerMessages;
	private HashMap<String,ConfigurableMessage> _groupMessages;

	public IndividualConfigurableMessage(Configuration config, String configurationPath) {
		this._config = config;
		this._configurationPath = configurationPath;
		this._defaultMessage = config.getConfigurableMessage(
				configurationPath + ".message", 0,
				"[color=green]+[/color] [color=yellow]%PLAYER_NAME%[/color] [color=gray]has joined the server.[/color]");
		this._playerMessages = new HashMap<String, ConfigurableMessage>();
		this._groupMessages = new HashMap<String, ConfigurableMessage>();
	}

	public ConfigurableMessage getMessage(String playerName, String groupName) {
		ConfigurableMessage message = getPlayerMessage(playerName);
		if (message != null) return message;
		message = getGroupMessage(groupName);
		if (message != null) return message;
		return this._defaultMessage;
	}

	private ConfigurableMessage getPlayerMessage(String playerName) {
		if (playerName == null) return null;
		ConfigurableMessage message = this._playerMessages.get(playerName);
		if (message != null) return message;
		String playerPath = getPlayerPath(playerName);
		message = this._config.getConfigurableMessage(playerPath, 0, null);
		if (!message.hasContent()) return null;
		this._playerMessages.put(playerName, message);
		return message;
	}

	private ConfigurableMessage getGroupMessage(String groupName) {
		if (groupName == null) return null;
		ConfigurableMessage message = this._groupMessages.get(groupName);
		if (message != null) return message;
		String groupPath = getGroupPath(groupName);
		message = this._config.getConfigurableMessage(groupPath, 0, null);
		if (!message.hasContent()) return null;
		this._groupMessages.put(groupName, message);
		return message;
	}

	private String getPlayerPath(String playerName) {
		return String.format("%s.players.%s", this._configurationPath, playerName);
	}

	private String getGroupPath(String rankName) {
		return String.format("%s.groups.%s", this._configurationPath, rankName);
	}
}
