package net.eithon.plugin.bungee;

import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.plugin.bungee.logic.Controller;
import net.eithon.plugin.bungee.logic.bungeecord.BungeeController;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EithonBungeeApi {
	private BungeeController _bungeeController;
	private Controller _controller;

	EithonBungeeApi(BungeeController bungeeController, Controller controller) {
		this._bungeeController = bungeeController;
		this._controller = controller;
	}
	
	public boolean serverHeartIsBeating(String serverName) {
		return this._controller.serverHeartIsBeating(serverName);
	}

	public boolean connectPlayerToServerOrInformSender(CommandSender sender, Player player, String serverName) {
		return this._controller.connectPlayerToServerOrInformSender(sender, player, serverName);
	}

	public boolean broadcastMessage(ConfigurableMessage configurableMessage, Object... args) {
		String message = configurableMessage.getMessageWithColorCoding(args);
		if (message == null) return false;
		if (!configurableMessage.broadcastToThisServer(args)) return false;
		return bungeeBroadcastMessage(message, configurableMessage.getUseTitle());
	}

	public boolean bungeeBroadcastMessage(String message, boolean useTitle) {
		return this._bungeeController.broadcastMessage(message, useTitle);
	}
	
	public void banPlayerOnThisServer(
			final Player player,
			final long seconds) {
		this._controller.banPlayerOnThisServer(player, seconds);
	}
}
