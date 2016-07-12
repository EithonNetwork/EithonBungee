package net.eithon.plugin.bungee.logic.bungeecord;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.IJsonObject;

import org.bukkit.entity.Player;

class BungeeSender {
	private EithonPlugin _eithonPlugin;
	private Channel _messageChannel;
	private BungeeController _bungeeController;

	public BungeeSender(EithonPlugin eithonPlugin, BungeeController bungeeController) {
		this._eithonPlugin = eithonPlugin;
		this._bungeeController = bungeeController;
		this._messageChannel = new Channel(eithonPlugin);
	}

	boolean forwardToAll(String command, IJsonObject<?> info, boolean rejectOld) {
		return forward("ALL", command, info, rejectOld);
	}

	boolean forwardToAll(String command, IJsonObject<?> info, boolean rejectOld, Player player) {
		return forward("ALL", command, info, rejectOld, player);
	}

	boolean forward(String destinationServer, String command, IJsonObject<?> info, boolean rejectOld) {
		return forward(destinationServer, command, info, rejectOld, null);
	}

	boolean forward(String destinationServer, String command, IJsonObject<?> info, boolean rejectOld, Player player) {
		verbose("forward", "Enter; destinationServer=%s, command = %s", destinationServer, command);
		String sourceServerName =  this._bungeeController.getBungeeServerName();
		ForwardHeader header = new ForwardHeader(command, sourceServerName, rejectOld);
		verbose("send", "header = %s", header.toJSONString());
		MessageOut data = new MessageOut()
		.add(header.toJSONString())
		.add(info.toJsonString());
		boolean success = this._messageChannel.send(player, "Forward", data, destinationServer, "EithonLibraryForward");
		verbose("forward", "Leave, success = %s", success ? "TRUE" : "FALSE");
		return success;
	}

	boolean getServer() {
		return this._messageChannel.send("GetServer");
	}

	boolean connect(Player player, String serverName) {
		return this._messageChannel.send(player, "Connect", serverName);
	}

	private void verbose(String method, String format, Object... args) {
		this._eithonPlugin.dbgVerbose("BungeeSender", method, format, args);	
	}
}
