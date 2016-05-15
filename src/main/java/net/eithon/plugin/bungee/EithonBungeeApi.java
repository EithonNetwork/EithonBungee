package net.eithon.plugin.bungee;

import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.plugin.bungee.logic.bungeecord.BungeeController;

public class EithonBungeeApi {
	private BungeeController _controller;

	EithonBungeeApi(BungeeController _bungeeController) {
		this._controller = _bungeeController;
	}

	public boolean broadcastMessage(ConfigurableMessage configurableMessage, Object... args) {
		String message = configurableMessage.getMessageWithColorCoding(args);
		if (message == null) return false;
		configurableMessage.broadcastToThisServer(message);
		return bungeeBroadcastMessage(message, configurableMessage.getUseTitle());
	}

	public boolean bungeeBroadcastMessage(String message, boolean useTitle) {
		return this._controller.broadcastMessage(message, useTitle);
	}
}
