package net.eithon.plugin.bungee;

import net.eithon.plugin.bungee.logic.bungeecord.BungeeController;
import net.eithon.library.json.IJsonObject;
import net.eithon.library.plugin.ConfigurableMessage;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EithonBungeeApi {
	private BungeeController _controller;

	EithonBungeeApi(BungeeController _bungeeController) {
		this._controller = _bungeeController;
	}
	
	public String getBungeeServerName() {
		return this._controller.getBungeeServerName();
	}
	
	public String getPrimaryBungeeServerName() {
		return Config.V.primaryBungeeServer;
	}
	
	public boolean isPrimaryBungeeServer(String bungeeServerName) {
		if (bungeeServerName == null) return false;
		String primaryBungeeServerName = getPrimaryBungeeServerName();
		if (primaryBungeeServerName == null) return false;
		return bungeeServerName.equalsIgnoreCase(primaryBungeeServerName);
	}
	
	public boolean isPrimaryBungeeServer() {
		String bungeeServerName = getBungeeServerName();
		if (bungeeServerName == null) return true;
		return isPrimaryBungeeServer(bungeeServerName);
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

	public boolean teleportPlayerToServer(Player player, String serverName) {
		return this._controller.connectToServer(player, serverName);
	}

	public boolean bungeeSendDataToServer(String serverName, String name, IJsonObject<?> data, boolean rejectOld) {
		return this._controller.sendDataToServer(serverName, name, data, rejectOld);
	}

	public boolean playerHasPermissionToAccessServerOrInformSender(
			CommandSender sender, Player player, String bungeeServerName) {
		return this._controller.playerHasPermissionToAccessServerOrInformSender(sender, player, bungeeServerName);
	}

	public boolean playerHasPermissionToAccessServer(Player player, String bungeeServerName) {
		return this._controller.playerHasPermissionToAccessServer(player, bungeeServerName);
	}
}
