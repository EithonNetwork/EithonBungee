package net.eithon.plugin.bungee.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.eithon.library.command.EithonCommand;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.bungee.Config;
import net.eithon.plugin.bungee.EithonBungeePlugin;
import net.eithon.plugin.bungee.logic.ban.BanController;
import net.eithon.plugin.bungee.logic.bungeecord.BungeeController;
import net.eithon.plugin.bungee.logic.individualmessage.IndividualMessageController;
import net.eithon.plugin.bungee.logic.joinleave.JoinLeaveController;
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
	public static final String HEARTBEAT = "Heartbeat";
	public static final String MESSAGE_TO_PLAYER = "MessageToPlayer";
	private EithonBungeePlugin _plugin;
	private HashMap<UUID, OfflinePlayer> _lastMessageFrom;
	private String _bungeeServerName;
	private IndividualMessageController _individualMessageController;
	private JoinLeaveController _joinLeaveController;
	private TeleportController _teleportController;
	private BungeePlayerController _bungeePlayerController;
	private BungeeController _bungeeController;
	private BanController _banController;
	private static int instanceCount = 0;
	private HashMap<String, HeartBeatPojo> _heartBeats;

	public Controller(EithonBungeePlugin plugin, BungeeController bungeeController) {
		this._plugin = plugin;
		this._bungeeController = bungeeController;
		this._individualMessageController = new IndividualMessageController(this._plugin);
		this._heartBeats = new HashMap<String, HeartBeatPojo>();
		waitForServerName();
		sendHeartBeats();
	}

	private void sendHeartBeats() {
		final int instanceNumber = ++instanceCount;
		new BukkitRunnable() {
			@Override
			public void run() {
				if (instanceNumber != instanceCount) this.cancel();
				sendHeartBeat(instanceNumber);
			}
		}.runTaskTimerAsynchronously(
				this._plugin, 
				0,
				TimeMisc.secondsToTicks(Config.V.secondsBetweenHeartBeats));;
	}

	private void sendHeartBeat(int instanceNumber) {
		if (!controllersAreReady()) return;
		HeartBeatPojo info = new HeartBeatPojo(this._bungeeServerName);
		if (this._bungeeController.sendDataToAll(HEARTBEAT, info, true)) handleHeartbeat(info);
	}

	public void disable() {
		if (!controllersAreReady()) return;
		this._bungeePlayerController.purgePlayers();
	}

	private boolean controllersAreReady() { 
		boolean controllersAreReady = this._bungeeServerName != null;
		verbose("controllersAreReady", controllersAreReady ? "TRUE" : "FALSE");
		return controllersAreReady; 
	}

	public boolean thisServerIsThePrimaryBungeeServer() {
		return serverIsThePrimaryBungeeServer(this._bungeeServerName);
	}

	public boolean serverIsThePrimaryBungeeServer(String serverName) {
		if (serverName == null) return false;
		return serverName.equalsIgnoreCase(Config.V.primaryBungeeServer);
	}
	
	public boolean serverHeartIsBeating(String serverName) {
		HeartBeatPojo info = null;
		synchronized (this._heartBeats) {
			info = this._heartBeats.get(serverName);
		}
		return (info != null) && !info.isTooOld();
	}

	private void waitForServerName() {
		String bungeeServerName = this._bungeeController.getBungeeServerName();
		if (bungeeServerName != null) {
			if (bungeeServerName.equalsIgnoreCase(Config.V.thisBungeeServerName)) {
				this._bungeePlayerController = new BungeePlayerController(this._plugin, this._bungeeController);
				this._joinLeaveController = new JoinLeaveController(this._plugin, this._bungeeController);
				this._teleportController = new TeleportController(this._plugin, this._bungeePlayerController, this._bungeeController);
				this._lastMessageFrom = new HashMap<UUID, OfflinePlayer>();
				this._banController = new BanController(this._plugin);
				createEithonBungeeFixesListener();
				this._bungeeServerName = bungeeServerName;
				return;
			}
			this._plugin.logError("According to the configuration this server is named %s, but according to BungeeCord it is named %s",
					Config.V.thisBungeeServerName, bungeeServerName);
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
		this._individualMessageController.broadcastPlayerJoined(serverName, playerName, groupName);
		verbose("broadcastPlayerJoined", "Leave");
	}

	public void broadcastPlayerSwitched(String fromServerName, String toServerName, UUID playerId, String playerName, String groupName) {
		verbose("broadcastPlayerSwitched", String.format("Enter: fromServerName=%s, toServerName=%s, player=%s, groupName=%s",
				fromServerName, toServerName, playerName, groupName));
		// Avoid double join messages
		this._individualMessageController.broadcastPlayerSwitched(fromServerName, toServerName, playerName, groupName);
		verbose("broadcastPlayerSwitched", "Leave");
	}

	public String getJoinMessage(Player player) {
		verbose("getQuitMessage", "Player=%s", player.getName());
		if (!controllersAreReady()) return null;
		final UUID playerId = player.getUniqueId();
		String mainGroup = this._joinLeaveController.getHighestGroup(playerId);
		String fromServerName = this._bungeePlayerController.getPreviousBungeeServerName(playerId);
		if (fromServerName == null) return null;
		return this._individualMessageController.getJoinMessage(this._bungeeServerName, fromServerName, player.getName(), mainGroup);
	}

	public String getQuitMessage(Player player) {
		verbose("getQuitMessage", "Player=%s", player.getName());
		if (!controllersAreReady()) return null;
		String mainGroup = this._joinLeaveController.getHighestGroup(player.getUniqueId());
		return this._individualMessageController.getQuitMessage(this._bungeeServerName, player.getName(), mainGroup);
	}

	public void broadcastPlayerQuitted(String serverName, UUID playerId, String playerName, String groupName) {
		verbose("broadcastPlayerQuitted", String.format("Enter: serverName=%s, player=%s, groupName=%s",
				serverName, playerName, groupName));
		this._individualMessageController.broadcastPlayerQuit(serverName, playerName, groupName);
		verbose("broadcastPlayerQuitted", "Leave");
	}

	private void createEithonBungeeFixesListener() {
		BungeeListener bungeeListener = new BungeeListener(this._plugin, this);
		this._plugin.getServer().getMessenger().
		registerIncomingPluginChannel(this._plugin, BungeeListener.EITHON_BUNGEE_FIXES_CHANNEL, bungeeListener);
	}

	public boolean requestTpToPlayer(Player movingPlayer, OfflinePlayer anchorPlayer) {
		if (!controllersAreReady()) return false;
		return this._teleportController.tpToPlayer(movingPlayer, movingPlayer, anchorPlayer, false);
	}

	public void forcedTpToPlayer(Player movingPlayer, OfflinePlayer anchorPlayer) {
		if (!controllersAreReady()) return;
		this._teleportController.tpToPlayer(movingPlayer, movingPlayer, anchorPlayer, true);
	}

	public boolean requestTpPlayerHere(Player movingPlayer, OfflinePlayer anchorPlayer) {
		if (!controllersAreReady()) return false;
		return this._teleportController.tpPlayerHere(movingPlayer, movingPlayer, anchorPlayer, false);
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
		if (!controllersAreReady()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					handleTeleportEvent(jsonObject);
				}
			}
			.runTaskLater(this._plugin, TimeMisc.secondsToTicks(1));
			return;
		}
		this._teleportController.handleTeleportEvent(jsonObject);
	}
	
	public void handleHeartbeat(JSONObject jsonObject) {
		if (!controllersAreReady()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					handleHeartbeat(jsonObject);
				}
			}
			.runTaskLater(this._plugin, TimeMisc.secondsToTicks(1));
			return;
		}
		HeartBeatPojo info = HeartBeatPojo.createFromJsonObject(jsonObject);
		handleHeartbeat(info);
	}

	private void handleHeartbeat(HeartBeatPojo info) {
		synchronized (this._heartBeats) {
			this._heartBeats.put(info.getServerName(), info);
		}
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
		String bungeeServerName = this._bungeePlayerController.getCurrentBungeeServerName(receiver);
		if (bungeeServerName == null) {
			sender.sendMessage(String.format("Player %s seems to be offline.", receiver.getName()));
			return false;
		}
		MessageToPlayerPojo info = new MessageToPlayerPojo(sender, receiver, message);
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

	public boolean connectPlayerToServerOrInformSender(CommandSender sender, Player player, String serverName) {
		if (!controllersAreReady()) {
			if (sender != null) Config.M.tryAgain.sendMessage(sender);
			return false;
		}
		return this._teleportController.changeServer(sender, player, serverName);
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

	public void publishSwitchEventOnThisServer(JSONObject data) {
		if (!controllersAreReady()) return;
		this._joinLeaveController.publishSwitchEventOnThisServer(data);
	}

	public void publishLeaveEventOnThisServer(JSONObject data) {
		this._joinLeaveController.publishLeaveEventOnThisServer(data);
	}

	public void publishLeaveEventOnThisServer(String serverName, UUID playerId,
			String playerName) {
		this._joinLeaveController.publishLeaveEventOnThisServer(serverName, playerId, playerName, null);
	}

	public void playerJoined(final Player player) {	
		if (!controllersAreReady()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					playerJoined(player);
				}
			}
			.runTaskLaterAsynchronously(this._plugin, TimeMisc.secondsToTicks(1.0));
			return;
		};
		new BukkitRunnable() {
			@Override
			public void run() {
				playerJoinedStageTwo(player);
			}
		}
		.runTaskAsynchronously(this._plugin);
	}

	private void playerJoinedStageTwo(final Player player) {
		if (this._banController.takeActionIfPlayerIsBannedOnThisServer(player)) return;
		new BukkitRunnable() {
			@Override
			public void run() {
				playerJoinedStageThree(player);

			}
		}.runTask(this._plugin);
	}

	private void playerJoinedStageThree(final Player player) {
		String previousServerName = this._bungeePlayerController.getAnyBungeeServerName(player);
		if ((previousServerName==null) || (previousServerName.equalsIgnoreCase(this._bungeeServerName))) {
			this._joinLeaveController.playerJoinedThisServer(player);
		} else {
			this._joinLeaveController.playerSwitchedToThisServer(player, previousServerName);			
		}
		this._teleportController.playerJoined(player);
		this._bungeePlayerController.addPlayerOnThisServerAsync(player);
	}

	public void playerJoinedOnOtherServer(UUID playerId, String playerName,
			String otherServerName) {
		if (!controllersAreReady()) return;
		this._bungeePlayerController.bungeePlayerAddedOnOtherServerAsync(playerId, playerName, otherServerName);
	}

	public void takeActionIfPlayerIsBannedOnThisServer(Player player) {
		if (!controllersAreReady()) return;
		this._banController.takeActionIfPlayerIsBannedOnThisServerAsync(player);
	}

	public void playerLeftThisServer(Player player) {
		this._bungeePlayerController.removePlayerAsync(player.getUniqueId(), player.getName(), this._bungeeServerName);
	}

	public void removeBungeePlayer(UUID playerId, String playerName, String otherServerName) {
		if (!controllersAreReady()) return;
		this._bungeePlayerController.removePlayerAsync(playerId, playerName, otherServerName);
	}

	public void banPlayerOnThisServer(
			final Player player,
			final long seconds) {
		this._banController.banPlayerOnThisServerAsync(null, player, seconds);
	}

	public void eithonBungeeLeaveReceived(String serverName, UUID playerId, String playerName, String mainGroup) {
		broadcastPlayerQuitted(serverName, playerId, playerName, mainGroup);
		removeBungeePlayer(playerId, playerName, serverName);
	}

	public void banAddAsync(
			final CommandSender sender, 
			final OfflinePlayer player, 
			final String serverName,
			final long seconds) {
		this._banController.banPlayerAsync(sender, player, serverName, seconds);
	}

	public void banRemoveAsync(
			final CommandSender sender, 
			final OfflinePlayer player, 
			final String serverName) {
		this._banController.unbanPlayerAsync(sender, player, serverName);
	}

	public void publicMessage(String message, boolean useTitle) {
		ConfigurableMessage.broadcastToThisServer(message, useTitle);
		this._bungeeController.broadcastMessage(message, useTitle);
	}

	public void banListAsync(final CommandSender sender) {
		this._banController.listBannedPlayersAsync(sender);
	}

	private void verbose(String method, String format, Object... args) {
		this._plugin.dbgVerbose("Controller", method, format, args);	
	}
}
