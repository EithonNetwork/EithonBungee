package net.eithon.plugin.bungee.logic;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.bungee.Config;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class Controller {
	private TeleportController _teleportController;
	private BungeePlayers _bungeePlayers;
	private EithonPlugin _eithonPlugin;
	private HashMap<UUID, OfflinePlayer> _lastMessageFrom;

	public Controller(EithonPlugin eithonPlugin) {
		this._eithonPlugin = eithonPlugin;
		this._teleportController = new TeleportController(eithonPlugin);
		this._bungeePlayers = new BungeePlayers(eithonPlugin);
		this._lastMessageFrom = new HashMap<UUID, OfflinePlayer>();
	}

	public void requestTpToPlayer(Player movingPlayer, OfflinePlayer anchorPlayer) {
		this._teleportController.tpToPlayer(movingPlayer, movingPlayer, anchorPlayer, false);
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
		TeleportToPlayerPojo info = TeleportToPlayerPojo.createFromJsonObject(jsonObject);
		this._teleportController.handleTeleportEvent(info);
	}
	
	public void handleMessageEvent(JSONObject jsonObject) {
		MessageToPlayerPojo info = MessageToPlayerPojo.createFromJsonObject(jsonObject);
		handleMessageEvent(info);
	}

	private void handleMessageEvent(MessageToPlayerPojo info) {
		Player receiver = Bukkit.getPlayer(info.getReceiverPlayerId());
		if (receiver == null) return;
		OfflinePlayer sender = Bukkit.getPlayer(info.getSendingPlayerId());
		if (sender == null) return;
		this._lastMessageFrom.put(info.getReceiverPlayerId(), sender);
		Config.M.messageFrom.sendMessage(receiver, sender.getName(), info.getMessage());
	}

	public void playerJoined(Player player) {
		this._teleportController.playerJoined(player);
	}

	public void playerQuitted(Player player) {
		this._teleportController.playerQuitted(player);
	}

	public void bungeePlayerJoined(EithonPlayer player, String thatServerName) {
		this._bungeePlayers.put(player, thatServerName);
	}

	public void bungeePlayerQuitted(EithonPlayer player, String thatServerName) {
		this._bungeePlayers.remove(player, thatServerName);
	}
	
	public List<String> getBungeePlayerNames() {
		return this._bungeePlayers.getNames();
	}

	public boolean sendMessageToPlayer(Player sender, OfflinePlayer receiver,
			String message) {
		BungeePlayer bungeePlayer = this._bungeePlayers.getBungeePlayer(receiver);
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
}
