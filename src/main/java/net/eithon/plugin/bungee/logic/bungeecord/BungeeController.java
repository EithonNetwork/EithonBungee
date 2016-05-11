package net.eithon.plugin.bungee.logic.bungeecord;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.facades.PermissionsFacade;
import net.eithon.library.json.IJsonObject;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.eithonlibrary.Config;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class BungeeController {

	private BungeeListener _bungeeListener;
	private BungeeSender _bungeeSender;
	private EithonPlugin _eithonPlugin;
	private String _serverName;

	public BungeeController(EithonPlugin eithonPlugin) {
		this._eithonPlugin = eithonPlugin;
		createBungeeSender(eithonPlugin);
		createBungeeListener(eithonPlugin);
	}

	public void createBungeeSender(EithonPlugin eithonPlugin) {
		eithonPlugin.getServer().getMessenger().registerOutgoingPluginChannel(eithonPlugin, "BungeeCord");
		this._bungeeSender = new BungeeSender(eithonPlugin, this);
	}

	private void createBungeeListener(EithonPlugin eithonPlugin) {
		this._bungeeListener = new BungeeListener(eithonPlugin, this);
		eithonPlugin.getServer().getMessenger().registerIncomingPluginChannel(eithonPlugin, "BungeeCord", this._bungeeListener);
	}

	public void initialize() {
		this._bungeeSender.getServer();
	}

	public String getBungeeServerName() { return this._serverName; }
	public String getMinecraftServerName() { return this._eithonPlugin.getServer().getServerName(); }


	void setServerName(String serverName) { this._serverName = serverName; }

	public boolean connectToServer(Player player, String serverName) { 
		if (!playerHasPermissionToAccessServer(player, serverName)) return false;
		return this._bungeeSender.connect(player, serverName);
	}

	public void joinEvent(Player player) {
		joinQuitEvent(player, "JoinEvent");
	}

	public void quitEvent(Player player) {
		joinQuitEvent(player, "QuitEvent");
	}

	public boolean broadcastMessage(String message, boolean useTitle) {
		verbose("broadcastMessage", "Enter, message = %s", message);
		MessageInfo info = new MessageInfo(message, useTitle);
		boolean success = this._bungeeSender.forwardToAll("BroadcastMessage", info, true);
		verbose("broadcastMessage", String.format("success=%s", success ? "TRUE" : "FALSE"));
		verbose("broadcastMessage", "Leave");
		return success;
	}

	public boolean sendDataToAll(String name,
			IJsonObject<?> data, boolean rejectOld) {
		return sendDataToServer("ALL", name, data, rejectOld);
	}

	public boolean sendDataToServer(String targetServerName, String name,
			IJsonObject<?> data, boolean rejectOld) {
		JSONObject jsonObject = data == null ? null : data.toJsonObject();
		String jsonString = jsonObject == null ? null : jsonObject.toJSONString();
		verbose("sendEventToServer", "Enter, targetServerName = %s, name= %s, json=%s",
				targetServerName, name, jsonString);
		if (targetServerName == null) {
			verbose("sendEventToServer", "targetServerName NULL, Leave");
			return false;
		}
		String thisServerName = this.getBungeeServerName();
		EithonBungeeEvent info = new EithonBungeeEvent(thisServerName, name, jsonObject);
		boolean success = this._bungeeSender.forward(targetServerName, "CallEvent", info, rejectOld);
		verbose("sendEventToServer", String.format("Leave, success=%s", success ? "TRUE" : "FALSE"));
		return success;
	}

	private boolean joinQuitEvent(Player player, String eventName) {
		verbose("joinQuitEvent", "Enter, player = %s", player == null ? "NULL" : player.getName());
		if (player == null) {
			verbose("joinQuitEvent", "Player NULL, Leave");
			return false;
		}
		String mainGroup = getHighestGroup(player);
		verbose("joinQuitEvent", String.format("mainGroup=%s", mainGroup));
		String serverName = getBungeeServerName();
		verbose("joinQuitEvent", String.format("serverName=%s", serverName));
		JoinQuitInfo info = new JoinQuitInfo(serverName, player.getUniqueId(), player.getName(), mainGroup);
		boolean success = this._bungeeSender.forwardToAll(eventName, info, true, player);
		verbose("joinQuitEvent", String.format("success=%s", success ? "TRUE" : "FALSE"));
		verbose("joinQuitEvent", "Leave");
		return success;
	}

	public static String getHighestGroup(OfflinePlayer offlinePlayer) {
		String[] currentGroups = PermissionsFacade.getPlayerPermissionGroups(offlinePlayer);
		for (String priorityGroup : Config.V.groupPriorities) {
			for (String playerGroup : currentGroups) {
				if (playerGroup.equalsIgnoreCase(priorityGroup)) {
					return priorityGroup;
				}
			}
		}
		return null;
	}

	public void simulateSendPluginMessage(BungeeController receiverServer, Player player, byte[] message) {
		MessageOut messageOut;
		MessageIn messageIn = new MessageIn(message);
		String subchannel = messageIn.readString();
		if (subchannel.equals("Forward")) {
			messageIn.readString(); // destinationServer
			String pluginChannel = messageIn.readString();
			byte[] body = messageIn.readByteArray();
			messageOut = new MessageOut()
			.add(pluginChannel)
			.add(body);
		} else if (subchannel.equals("GetServer")) {
			final String serverName = this.getMinecraftServerName();
			final String targetServerName = String.format("bungee_%s", serverName);
			messageOut = new MessageOut()
			.add(subchannel)
			.add(targetServerName);
		} else {
			messageOut = new MessageOut()
			.add(message);
		}
		receiverServer.simulateReceivePluginMessage(player, messageOut);
	}

	private void simulateReceivePluginMessage(Player player,
			MessageOut messageOut) {
		this._bungeeListener.onPluginMessageReceived("BungeeCord", player, messageOut.toByteArray());
	}

	private void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "BungeeController.%s: %s", method, message);
	}

	public boolean playerHasPermissionToAccessServerOrInformSender(
			CommandSender sender, Player player, String bungeeServerName) {
		if (playerHasPermissionToAccessServer(player, bungeeServerName)) return true;
		if (sender == null) return false;
		sender.sendMessage(String.format("Player %s is not permitted to access server %s.", 
				player.getName(), bungeeServerName));

		return false;
	}

	public boolean playerHasPermissionToAccessServer(Player player,
			String bungeeServerName) {
		return player.hasPermission(String.format("eithonbungee.access.server.%s", bungeeServerName));
	}
}
