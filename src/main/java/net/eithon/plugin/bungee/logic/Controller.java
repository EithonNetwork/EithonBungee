package net.eithon.plugin.bungee.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.eithon.library.command.EithonCommand;
import net.eithon.library.core.CoreMisc;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.bungee.Config;
import net.eithon.plugin.bungee.EithonBungeePlugin;
import net.eithon.plugin.bungee.logic.bungeecord.BungeeController;
import net.eithon.plugin.bungee.logic.individualmessage.IndividualMessageController;
import net.eithon.plugin.bungee.logic.joinleave.JoinLeaveController;
import net.eithon.plugin.bungee.logic.players.BungeePlayer;
import net.eithon.plugin.bungee.logic.players.BungeePlayerController;
import net.eithon.plugin.bungee.logic.teleport.TeleportController;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;

public class Controller {	
	public static final String MESSAGE_TO_PLAYER = "MessageToPlayer";
	private EithonBungeePlugin _plugin;
	private HashMap<UUID, OfflinePlayer> _lastMessageFrom;
	private String _bungeeServerName;
	private IndividualMessageController _individualMessageController;
	private JoinLeaveController _joinLeaveController;
	private TeleportController _teleportController;
	private BungeePlayerController _bungeePlayerController;
	private BungeeController _bungeeController;

	public Controller(EithonBungeePlugin plugin, BungeeController bungeeController) {
		this._plugin = plugin;
		this._bungeeController = bungeeController;
		this._individualMessageController = new IndividualMessageController(this._plugin);
		waitForServerName();
	}

	private boolean controllersAreReady() { 
		boolean controllersAreReady = this._bungeeServerName != null;
		verbose("controllersAreReady", controllersAreReady ? "TRUE" : "FALSE");
		return controllersAreReady; 
	}

	private void waitForServerName() {
		String bungeeServerName = this._bungeeController.getBungeeServerName();
		if (bungeeServerName != null) {
			this._bungeePlayerController = new BungeePlayerController(this._plugin, this._bungeeController, bungeeServerName);
			this._joinLeaveController = new JoinLeaveController(this._plugin, this._bungeeController, bungeeServerName);
			this._teleportController = new TeleportController(this._plugin, this._bungeePlayerController, this._bungeeController, bungeeServerName);
			this._lastMessageFrom = new HashMap<UUID, OfflinePlayer>();
			createEithonBungeeFixesListener();
			this._bungeeServerName = bungeeServerName;
			return;
		}
		final BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				waitForServerName();
			}
		};
		runnable.runTaskLater(this._plugin, TimeMisc.secondsToTicks(1));
	}

	public void broadcastPlayerJoined(String serverName, UUID playerId, String playerName, String groupName) {
		verbose("broadcastPlayerJoined", String.format("Enter: serverName=%s, player=%s, groupName=%s",
				serverName, playerName, groupName));
		// Avoid double join messages
		if (serverName.equalsIgnoreCase(_bungeeServerName)) return;
		this._individualMessageController.broadcastPlayerJoined(serverName, playerName, groupName);
		verbose("broadcastPlayerJoined", "Leave");
	}

	public String getJoinMessage(Player player) {
		String mainGroup = JoinLeaveController.getHighestGroup(player.getUniqueId());
		return this._individualMessageController.getJoinMessage(this._bungeeServerName, player.getName(), mainGroup);
	}

	public String getQuitMessage(Player player) {
		String mainGroup = JoinLeaveController.getHighestGroup(player.getUniqueId());
		return this._individualMessageController.getQuitMessage(this._bungeeServerName, player.getName(), mainGroup);
	}

	public void broadcastPlayerQuitted(String serverName, UUID playerId, String playerName, String groupName) {
		verbose("broadcastPlayerQuitted", String.format("Enter: serverName=%s, player=%s, groupName=%s",
				serverName, playerName, groupName));
		if (serverName.equalsIgnoreCase(_bungeeServerName)) return;
		this._individualMessageController.broadcastPlayerQuit(serverName, playerName, groupName);
		verbose("broadcastPlayerQuitted", "Leave");
	}

	private void createEithonBungeeFixesListener() {
		BungeeListener bungeeListener = new BungeeListener(this._plugin, this);
		this._plugin.getServer().getMessenger().
		registerIncomingPluginChannel(this._plugin, BungeeListener.EITHON_BUNGEE_FIXES_CHANNEL, bungeeListener);
	}

	void playerLeftOnAnotherServer(String serverName, UUID playerId, String playerName) {
		this._joinLeaveController.playerLeftOnAnotherServer(serverName, playerId, playerName);
	}

	public boolean requestTpToPlayer(Player movingPlayer, OfflinePlayer anchorPlayer) {
		if (!controllersAreReady()) return false;
		return this._teleportController.tpToPlayer(movingPlayer, movingPlayer, anchorPlayer, false);
	}

	public void forcedTpToPlayer(Player movingPlayer, OfflinePlayer anchorPlayer) {
		if (!controllersAreReady()) return;
		this._teleportController.tpToPlayer(movingPlayer, movingPlayer, anchorPlayer, true);
	}

	public void requestTpPlayerHere(Player movingPlayer, OfflinePlayer anchorPlayer) {
		if (!controllersAreReady()) return;
		this._teleportController.tpPlayerHere(movingPlayer, movingPlayer, anchorPlayer, false);
	}

	public void forcedTpPlayerHere(Player movingPlayer, OfflinePlayer anchorPlayer) {
		if (!controllersAreReady()) return;
		this._teleportController.tpPlayerHere(movingPlayer, movingPlayer, anchorPlayer, true);
	}

	public void tpDeny(Player localPlayer) {
		if (!controllersAreReady()) return;
		this._teleportController.deny(localPlayer, localPlayer);
	}

	public void tpAccept(Player localPlayer) {
		if (!controllersAreReady()) return;
		this._teleportController.accept(localPlayer, localPlayer);
	}

	public void handleTeleportEvent(JSONObject jsonObject) {
		if (!controllersAreReady()) return;
		this._teleportController.handleTeleportEvent(jsonObject);
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

	public List<String> getBungeePlayerNames(EithonCommand ec) {
		if (!controllersAreReady()) return new ArrayList<String>();
		List<String> names = this._bungeePlayerController.getNames();
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
		if (!controllersAreReady()) return false;
		BungeePlayer bungeePlayer = this._bungeePlayerController.getBungeePlayer(receiver);
		if (bungeePlayer == null) {
			sender.sendMessage(String.format("Player %s seems to be offline.", receiver.getName()));
			return false;
		}
		MessageToPlayerPojo info = new MessageToPlayerPojo(sender, receiver, message);
		String bungeeServerName = bungeePlayer.getBungeeServerName();
		this._bungeeController.sendDataToServer(bungeeServerName, MESSAGE_TO_PLAYER, info, true);
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
		if (!controllersAreReady()) return new ArrayList<String>();
		return this._teleportController.getWarpNames();
	}

	public boolean warpAdd(CommandSender sender, String name, Location location) {
		if (!controllersAreReady()) return false;
		return this._teleportController.warpAdd(sender, name, location);
	}

	public boolean warpTo(CommandSender sender, Player player, String name) {
		if (!controllersAreReady()) return false;
		return this._teleportController.warpTo(sender, player, name);
	}

	public boolean connectPlayerToServer(Player player, String serverName) {
		if (!controllersAreReady()) {
			Config.M.tryAgain.sendMessage(player);
			return false;
		}
		return this._teleportController.changeServer(player, serverName);
	}

	public void bungeePlayerAddedOnOtherServer(JSONObject data) {
		if (!controllersAreReady()) return;
		this._bungeePlayerController.bungeePlayerAddedOnOtherServerAsync(data);
	}

	public void refreshBungeePlayer() {
		if (!controllersAreReady()) return;
		this._bungeePlayerController.refreshAsync();
	}

	public void refreshWarpLocations() {
		if (!controllersAreReady()) return;
		this._teleportController.refreshWarpLocationsAsync();	
	}

	public void publishJoinEventOnThisServer(JSONObject data) {
		if (!controllersAreReady()) return;
		this._joinLeaveController.publishJoinEventOnThisServer(data);
	}

	public void publishLeaveEventOnThisServer(JSONObject data) {
		this._joinLeaveController.publishLeaveEventOnThisServer(data);
	}

	public void playerJoined(final Player player) {	
		if (!controllersAreReady()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					playerJoined(player);
				}
			}
			.runTaskLater(this._plugin, TimeMisc.secondsToTicks(1.0));
			return;
		};
		this._joinLeaveController.sendJoinEventToOtherServers(player);
		this._teleportController.playerJoined(player);
		this._bungeePlayerController.addPlayerOnThisServerAsync(player);
	}

	public void playerLeftThisServer(Player player) {		
		removeBungeePlayer(player.getUniqueId(), player.getName(), this._bungeeServerName);
	}

	public void removeBungeePlayer(UUID playerId, String playerName, String otherServerName) {
		if (!controllersAreReady()) return;
		if (_bungeeServerName.equalsIgnoreCase(otherServerName)) return;
		this._bungeePlayerController.removePlayerAsync(playerId, playerName, otherServerName);

	}

	void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._plugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "Controller.%s: %s", method, message);
	}
}
