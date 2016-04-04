package net.eithon.plugin.bungee.logic;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.eithon.library.command.EithonCommand;
import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.bungee.Config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class Controller {
	private TeleportController _teleportController;
	private BungeePlayers _bungeePlayers;
	private EithonPlugin _eithonPlugin;
	private HashMap<UUID, OfflinePlayer> _lastMessageFrom;
	private String _bungeeServerName;
	private HashMap<String, WarpLocation> _warpLocations;

	public Controller(EithonPlugin eithonPlugin) {
		this._eithonPlugin = eithonPlugin;
		this._bungeePlayers = new BungeePlayers(eithonPlugin);
		this._teleportController = new TeleportController(eithonPlugin);
		this._lastMessageFrom = new HashMap<UUID, OfflinePlayer>();
		this._warpLocations = new HashMap<String, WarpLocation>();
	}

	public boolean requestTpToPlayer(Player movingPlayer, OfflinePlayer anchorPlayer) {
		return this._teleportController.tpToPlayer(movingPlayer, movingPlayer, anchorPlayer, false);
	}

	public void forcedTpToPlayer(Player movingPlayer, OfflinePlayer anchorPlayer) {
		this._teleportController.tpToPlayer(movingPlayer, movingPlayer, anchorPlayer, true);
	}

	public void requestTpPlayerHere(Player movingPlayer, OfflinePlayer anchorPlayer) {
		this._teleportController.tpPlayerHere(movingPlayer, movingPlayer, anchorPlayer, false);
	}

	public void forcedTpPlayerHere(Player movingPlayer, OfflinePlayer anchorPlayer) {
		this._teleportController.tpPlayerHere(movingPlayer, movingPlayer, anchorPlayer, true);
	}

	public void tpDeny(Player localPlayer) {
		this._teleportController.deny(localPlayer, localPlayer);
	}

	public void tpAccept(Player localPlayer) {
		this._teleportController.accept(localPlayer, localPlayer);
	}
	
	public void handleTeleportEvent(JSONObject jsonObject) {
		TeleportPojo info = TeleportPojo.createFromJsonObject(jsonObject);
		this._teleportController.handleTeleportEvent(info);
	}
	
	public void handleMessageEvent(JSONObject jsonObject) {
		MessageToPlayerPojo info = MessageToPlayerPojo.createFromJsonObject(jsonObject);
		handleMessageEvent(info);
	}

	private void handleMessageEvent(MessageToPlayerPojo info) {
		verbose("handleMessageEvent", "Message = %s", info.getMessage());
		Player receiver = Bukkit.getPlayer(info.getReceiverPlayerId());
		if (receiver == null) {
			verbose("handleMessageEvent", "No receiver found");
			return;
		}
		verbose("handleMessageEvent", "Receiver = %s", receiver.getName());
		OfflinePlayer sender = Bukkit.getOfflinePlayer(info.getSendingPlayerId());
		if (sender == null) {
			verbose("handleMessageEvent", "No sender found");
			return;
		}
		verbose("handleMessageEvent", "Sender = %s", sender.getName());
		this._lastMessageFrom.put(info.getReceiverPlayerId(), sender);
		Config.M.messageFrom.sendMessage(receiver, sender.getName(), info.getMessage());
		verbose("handleMessageEvent", "Message was sent");
	}

	public void playerJoined(Player player) {
		this._teleportController.playerJoined(player);
		bungeePlayerJoined(new EithonPlayer(player), getBungeeServerName());
	}

	public void playerQuitted(Player player) {
		this._teleportController.playerQuitted(player);
		bungeePlayerQuitted(new EithonPlayer(player), getBungeeServerName());
	}

	public void bungeePlayerJoined(EithonPlayer player, String thatServerName) {
		this._bungeePlayers.put(player, thatServerName);
	}

	public void bungeePlayerQuitted(EithonPlayer player, String thatServerName) {
		this._bungeePlayers.remove(player, thatServerName);
	}
	
	public List<String> getBungeePlayerNames(EithonCommand ec) {
		List<String> names = this._bungeePlayers.getNames();
		Player currentPlayer = ec.getPlayer();
		if (currentPlayer == null) return names;
		String name = currentPlayer.getName();
		return names
				.stream()
				.filter(n -> !n.equalsIgnoreCase(name))
				.collect(Collectors.toList());
	}

	public boolean sendMessageToPlayer(Player sender, OfflinePlayer receiver,
			String message) {
		BungeePlayer bungeePlayer = this._bungeePlayers.getBungeePlayer(receiver);
		if (bungeePlayer == null) {
			sender.sendMessage(String.format("Player %s seems to be offline.", receiver.getName()));
			return false;
		}
		MessageToPlayerPojo info = new MessageToPlayerPojo(sender, receiver, message);
		String bungeeServerName = bungeePlayer.getBungeeServerName();
		this._eithonPlugin.getApi().bungeeSendDataToServer(bungeeServerName, "MessageToPlayer", info, true);
		return true;
	}

	public String replyMessageToPlayer(Player sender,
			String message) {
		OfflinePlayer receiver = this._lastMessageFrom.get(sender.getUniqueId());
		if (receiver == null) {
			sender.sendMessage("No player to reply to.");
			return null;
		}
		boolean success = sendMessageToPlayer(sender, receiver, message);
		return success ? receiver.getName() : null;
	}

	public List<String> getWarpNames() {
		return this._teleportController.getWarpNames();
	}

	public boolean warpAdd(CommandSender sender, String name, Location location) {
		return this._teleportController.warpAdd(sender, name, location);
	}

	public void warpTo(Player player, String name) {
		this._teleportController.warpTo(player, name);
	}

	private String getBungeeServerName() {
		if (this._bungeeServerName != null) return this._bungeeServerName;
		this._bungeeServerName = this._eithonPlugin.getApi().getBungeeServerName();
		return this._bungeeServerName;
	}

	void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "Controller.%s: %s", method, message);
	}
}
