package net.eithon.plugin.bungee.logic;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.eithon.library.command.EithonCommand;
import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.bungee.Config;
import net.eithon.plugin.bungee.EithonBungeePlugin;
import net.eithon.plugin.bungee.logic.bungeecord.BungeeController;
import net.eithon.plugin.bungee.logic.bungeecord.EithonBungeeQuitEvent;
import net.eithon.plugin.bungee.logic.individualmessage.IndividualMessageController;
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

	private TeleportController _teleportController;
	private BungeePlayerController _bungeePlayers;
	private EithonBungeePlugin _plugin;
	private HashMap<UUID, OfflinePlayer> _lastMessageFrom;
	private String _bungeeServerName;
	private BungeeController _bungeeController;
	private IndividualMessageController _individualMessageController;
	
	public Controller(EithonBungeePlugin plugin, BungeeController bungeeController) {
		this._plugin = plugin;
		this._bungeePlayers = new BungeePlayerController(plugin, bungeeController);
		this._bungeeController = bungeeController;
		this._teleportController = new TeleportController(plugin, this._bungeePlayers, bungeeController);
		this._individualMessageController = new IndividualMessageController(plugin);
		this._lastMessageFrom = new HashMap<UUID, OfflinePlayer>();
		createEithonBungeeFixesListener();
	}

	public void broadcastPlayerJoined(String serverName, EithonPlayer player, String groupName) {
		verbose("broadcastPlayerJoined", String.format("Enter: serverName=%s, player=%s, groupName=%s",
				serverName, player.getName(), groupName));
		this._individualMessageController.broadcastPlayerJoined(serverName, player, groupName);
		verbose("broadcastPlayerJoined", "Leave");
	}

	public String getJoinMessage(Player player) {
		String serverName = this._bungeeController.getBungeeServerName();
		EithonPlayer eithonPlayer = new EithonPlayer(player);
		String mainGroup = BungeeController.getHighestGroup(player);
		return this._individualMessageController.getJoinMessage(serverName, eithonPlayer, mainGroup);
	}

	public String getQuitMessage(Player player) {
		String serverName = this._bungeeController.getBungeeServerName();
		EithonPlayer eithonPlayer = new EithonPlayer(player);
		String mainGroup = BungeeController.getHighestGroup(player);
		return this._individualMessageController.getQuitMessage(serverName, eithonPlayer, mainGroup);
	}

	public void broadcastPlayerQuitted(String serverName, EithonPlayer player, String groupName) {
		verbose("broadcastPlayerQuitted", String.format("Enter: serverName=%s, player=%s, groupName=%s",
				serverName, player.getName(), groupName));
		this._individualMessageController.broadcastPlayerQuit(serverName, player, groupName);
		verbose("broadcastPlayerQuitted", "Leave");
	}

	private void createEithonBungeeFixesListener() {
		BungeeListener bungeeListener = new BungeeListener(this._plugin, this);
		this._plugin.getServer().getMessenger().
		registerIncomingPluginChannel(this._plugin, BungeeListener.EITHON_BUNGEE_FIXES_CHANNEL, bungeeListener);
	}

	void playerDisconnected(String serverName, UUID playerUuid) {
		String thisServerName = this._plugin.getApi().getBungeeServerName();
		EithonPlayer player = new EithonPlayer(playerUuid);
		String highestGroup = BungeeController.getHighestGroup(player.getOfflinePlayer());
		EithonBungeeQuitEvent e = new EithonBungeeQuitEvent(thisServerName, serverName, player, highestGroup);
		Bukkit.getServer().getPluginManager().callEvent(e);	
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

	public void playerJoined(Player player) {
		if (getBungeeServerName() != null) {
			this._teleportController.playerJoined(player);
			this._bungeePlayers.addPlayerOnThisServerAsync(player);
			this._bungeeController.joinEvent(player);
			return;
		}		
		final BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				playerJoined(player);
			}
		};
		runnable.runTaskLater(this._plugin, TimeMisc.secondsToTicks(1));
	}

	public void playerLeft(Player player) {
		this._bungeePlayers.removePlayerOnThisServerAsync(player);
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
		this._plugin.getApi().bungeeSendDataToServer(bungeeServerName, "MessageToPlayer", info, true);
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

	public boolean warpTo(CommandSender sender, Player player, String name) {
		return this._teleportController.warpTo(sender, player, name);
	}

	public boolean connectPlayerToServer(Player player, String serverName) {
		if (serverName.equalsIgnoreCase(getBungeeServerName())) {
			Config.M.alreadyConnectedToServer.sendMessage(player, serverName);
			return false;
		}

		if (!playerCanConnectToServer(player, serverName)) return false;

		boolean success = this._plugin.getApi().teleportPlayerToServer(player, serverName);

		if (!success) {
			Config.M.couldNotConnectToServer.sendMessage(player, serverName, "Unspecified fail reason");
			return false;
		}
		return true;
	}

	private boolean playerCanConnectToServer(Player player, String serverName) {
		return this._plugin.getApi().playerHasPermissionToAccessServerOrInformSender(player, player, serverName);
	}

	private String getBungeeServerName() {
		if (this._bungeeServerName != null) return this._bungeeServerName;
		this._bungeeServerName = this._plugin.getApi().getBungeeServerName();
		return this._bungeeServerName;
	}

	void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._plugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "Controller.%s: %s", method, message);
	}

	public void handleBungeePlayer(JSONObject data) {
		this._bungeePlayers.handleBungeePlayerAsync(data);
	}
}
