package net.eithon.plugin.bungee.logic.teleport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.bungee.Config;
import net.eithon.plugin.bungee.logic.bungeecord.BungeeController;
import net.eithon.plugin.bungee.logic.players.BungeePlayerController;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;

public class TeleportController {
	public static final String TELEPORT_TO_PLAYER = "TeleportToPlayer";
	public static final String WARP_LOCATION_REFRESH = "WarpLocationRefresh";
	private HashMap<UUID, TeleportPojo> _waitingForTeleport;
	private HashMap<UUID, List<TeleportPojo>> _requestsForTeleport;
	final private BungeePlayerController _bungeePlayers;
	private BungeeController _bungeeController;
	private EithonPlugin _eithonPlugin;

	public TeleportController(
			final EithonPlugin eithonPlugin,
			final BungeePlayerController bungeePlayers, 
			final BungeeController bungeeController) {
		this._eithonPlugin = eithonPlugin;
		this._bungeePlayers = bungeePlayers;
		this._bungeeController = bungeeController;
		this._waitingForTeleport = new HashMap<UUID, TeleportPojo>();
		this._requestsForTeleport = new HashMap<UUID, List<TeleportPojo>>();
		this.refreshWarpLocationsAsync();
	}

	public boolean tpToPlayer(CommandSender sender, Player movingPlayer, OfflinePlayer anchorPlayer, boolean force) {
		String bungeeServerName = this._bungeePlayers.getCurrentBungeeServerNameOrInformSender(sender, anchorPlayer);
		if (bungeeServerName == null) {
			sender.sendMessage(String.format("Player %s seems to be offline.", anchorPlayer.getName()));
			return false;
		}
		if (anchorPlayer.isOnline() && force) {
			movingPlayer.teleport(anchorPlayer.getPlayer());
		} else {
			TeleportPojo info = new TeleportPojo(movingPlayer, anchorPlayer);
			info.setAsRequestFromMovingPlayer(force);
			if (!this._bungeeController.playerHasPermissionToAccessServerOrInformSender(sender, movingPlayer, bungeeServerName)) return false;
			sendTeleportMessageToBungeeServer(bungeeServerName, info);
			if (force) this._bungeeController.connectToServer(movingPlayer, bungeeServerName);
		}
		return true;
	}

	public boolean tpPlayerHere(CommandSender sender, Player anchorPlayer, OfflinePlayer movingPlayer, boolean force) {
		String bungeeServerName = this._bungeePlayers.getCurrentBungeeServerNameOrInformSender(sender, movingPlayer);
		if (bungeeServerName == null) return false;

		if (movingPlayer.isOnline() && force) {
			movingPlayer.getPlayer().teleport(anchorPlayer);
		} else {
			TeleportPojo info = new TeleportPojo(movingPlayer, anchorPlayer);
			info.setAsRequestFromAnchorPlayer(force);
			sendTeleportMessageToBungeeServer(bungeeServerName, info);
		}
		return true;
	}

	public boolean warpTo(CommandSender sender, Player player, String name) {
		WarpLocation warpLocation = WarpLocation.getByName(name);
		if (warpLocation.getBungeeServerName().equalsIgnoreCase(Config.V.thisBungeeServerName)) {
			player.teleport(warpLocation.getLocation());
		} else {
			String bungeeServerName = warpLocation.getBungeeServerName();
			if (!this._bungeeController.playerHasPermissionToAccessServerOrInformSender(sender, player, bungeeServerName)) return false;
			TeleportPojo info = new TeleportPojo(player, name);
			sendTeleportMessageToBungeeServer(bungeeServerName, info);
			return this._bungeeController.connectToServer(player, bungeeServerName);
		}
		return true;
	}

	public boolean changeServer(CommandSender sender, Player player, String serverName) {
		if (!this._bungeeController.playerHasPermissionToAccessServerOrInformSender(sender, player, serverName)) return false;

		TeleportPojo info = new TeleportPojo(player);
		sendTeleportMessageToBungeeServer(serverName, info);

		boolean success = this._bungeeController.connectToServer(player, serverName);

		if (!success) {
			if (sender != null) Config.M.couldNotConnectToServer.sendMessage(sender, serverName, "Unspecified fail reason");
			return false;
		}
		return true;
	}


	public void handleTeleportEvent(JSONObject jsonObject) {
		TeleportPojo info = TeleportPojo.createFromJsonObject(jsonObject);
		short messageType = info.getMessageType();
		verbose("handleTeleportEvent", "info.messageType=%d", messageType);
		if ((messageType == TeleportPojo.WARP) || (messageType == TeleportPojo.CHANGE_SERVER)) {
			// Prepare to teleport the moving player when he/she arrives
			waitForPlayerToComeToServer(info);
			return;
		}
		Player localPlayer = getLocalPlayer(info);
		OfflinePlayer remotePlayer = getRemotePlayer(info);
		if (remotePlayer == null) return;

		switch (messageType) {
		case TeleportPojo.PLAYER_FORCE:
			if (info.getMessageDirectionIsFromMovingToAnchor()) {
				waitForPlayerToComeToServer(info);
			} else {
				// Initiate a teleport from this server
				tpToPlayer(null, localPlayer, remotePlayer, true);
			}
			break;
		case TeleportPojo.PLAYER_REQUEST:
			saveRequest(localPlayer, info);
			if (info.getMessageDirectionIsFromMovingToAnchor()) {
				requestTpTo(localPlayer, remotePlayer);
			} else {
				requestTpHere(localPlayer, remotePlayer);
			}
			break;
		case TeleportPojo.PLAYER_DENY_RESPONSE:
			if (info.getMessageDirectionIsFromMovingToAnchor()) {
				denyTpHere(localPlayer, remotePlayer);
			} else {
				denyTpTo(localPlayer, remotePlayer);
			}
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	private void waitForPlayerToComeToServer(TeleportPojo info) {
		verbose("waitForPlayerToComeToServer", "info.messageType=%d", info.getMessageType());
		UUID playerId = info.getMovingPlayerId();
		// Maybe player is already on server?
		Player player = Bukkit.getPlayer(playerId);
		if (player != null) {
			teleportPlayerAccordingToInfo(player, info);
			verbose("waitForPlayerToComeToServer", "Leave. Teleporting player %s according to info",player.getName());
			return;
		}
		synchronized (this._waitingForTeleport) {
			this._waitingForTeleport.put(playerId, info);
		}
		verbose("waitForPlayerToComeToServer", "Leave. Added to _waitingForTeleport");
	}

	private void saveRequest(Player player, TeleportPojo info) {
		final UUID playerId = player.getUniqueId();
		synchronized (this._requestsForTeleport) {
			List<TeleportPojo> list = this._requestsForTeleport.get(playerId);
			if (list == null) {
				list = new ArrayList<TeleportPojo>();
				this._requestsForTeleport.put(playerId, list);
			}
			list.add(info);
		}
	}

	private void requestTpTo(Player localPlayer, OfflinePlayer remotePlayer) {
		Config.M.requestTpTo.sendMessage(localPlayer, remotePlayer.getName());
	}

	private void requestTpHere(Player localPlayer, OfflinePlayer remotePlayer) {
		Config.M.requestTpHere.sendMessage(localPlayer, remotePlayer.getName());
	}

	private void denyTpHere(Player localPlayer, OfflinePlayer remotePlayer) {
		Config.M.denyTpHere.sendMessage(localPlayer, remotePlayer.getName());		
	}

	private void denyTpTo(Player localPlayer, OfflinePlayer remotePlayer) {
		Config.M.denyTpTo.sendMessage(localPlayer, remotePlayer.getName());		
	}

	private Player getLocalPlayer(TeleportPojo info) {
		if (info.getMessageDirectionIsFromMovingToAnchor()) {
			return Bukkit.getPlayer(info.getAnchorPlayerId());
		} else {
			return getSourcePlayerOrForwardMessage(info);
		}
	}

	private OfflinePlayer getRemotePlayer(TeleportPojo info) {
		if (info.getMessageDirectionIsFromMovingToAnchor()) {
			return Bukkit.getOfflinePlayer(info.getMovingPlayerId());
		} else {
			return Bukkit.getOfflinePlayer(info.getAnchorPlayerId());
		}
	}

	private Player getSourcePlayerOrForwardMessage(TeleportPojo info) {
		Player sourcePlayer = Bukkit.getPlayer(info.getMovingPlayerId());
		// Verify that the source player is on line on this server
		if (sourcePlayer != null) return sourcePlayer;

		// The source player was unexpectedly not found on this server.
		// Find the server that the player has moved to and forward the message there.
		String bungeeServerName = this._bungeePlayers.getCurrentBungeeServerName(info.getMovingPlayerId());
		if (bungeeServerName == null) return null;
		sendTeleportMessageToBungeeServer(bungeeServerName, info);
		return null;			
	}

	public void playerJoined(final Player movingPlayer) {
		verbose("playerJoined", "movingPlayer=%s", movingPlayer.getName());
		final UUID movingPlayerId = movingPlayer.getUniqueId();
		TeleportPojo info = null;
		synchronized (this._waitingForTeleport) {
			info = this._waitingForTeleport.remove(movingPlayerId);
			if (info == null) {
				verbose("playerJoined", "Not waiting for player %s", movingPlayer.getName());
				return;
			}
		}
		if (info.isTooOld()) {
			verbose("playerJoined", "Teleport info was too old for player %s", movingPlayer.getName());
			return;
		}
		teleportPlayerAccordingToInfo(movingPlayer, info);	
		verbose("playerJoined", "Leave after teleporting player %s", movingPlayer.getName());

	}

	private void teleportPlayerAccordingToInfo(final Player movingPlayer, TeleportPojo info) {
		short messageType = info.getMessageType();
		if (messageType == TeleportPojo.CHANGE_SERVER) return;
		if (messageType == TeleportPojo.WARP) {
			teleportToWarpLocation(movingPlayer, info);
			return;
		}
		teleportToAnchorPlayer(movingPlayer, info);
	}

	private void teleportToWarpLocation(final Player movingPlayer,
			TeleportPojo info) {
		final WarpLocation warpLocation = WarpLocation.getByName(info.getWarpLocationName());
		movingPlayer.teleport(warpLocation.getLocation());
		return;
	}

	private void teleportToAnchorPlayer(final Player movingPlayer,
			TeleportPojo info) {
		final String anchorBungeeServerName = this._bungeePlayers.getCurrentBungeeServerName(info.getAnchorPlayerId());
		if (anchorBungeeServerName == null) return;
		if (!anchorBungeeServerName.equalsIgnoreCase(Config.V.thisBungeeServerName)) {
			// The player has moved to another server, make another server switch
			OfflinePlayer anchorPlayer = Bukkit.getOfflinePlayer(info.getAnchorPlayerId());
			if (anchorPlayer == null) return;
			tpToPlayer(null, movingPlayer, anchorPlayer, true);
			return;
		}
		Player anchorPlayer = Bukkit.getPlayer(info.getAnchorPlayerId());
		if (anchorPlayer == null) return;
		forcedTpToOnlinePlayer(movingPlayer, anchorPlayer);
	}

	private void sendTeleportMessageToBungeeServer(TeleportPojo info) {
		UUID remotePlayerId = null;
		if (info.getMessageDirectionIsFromMovingToAnchor()) {
			remotePlayerId = info.getAnchorPlayerId();
		} else {
			remotePlayerId = info.getMovingPlayerId();
		}
		String bungeeServerName = this._bungeePlayers.getCurrentBungeeServerName(remotePlayerId);
		if (bungeeServerName == null) return;
		this._bungeeController.sendDataToServer(bungeeServerName, TELEPORT_TO_PLAYER, info, true);
	}

	private void sendTeleportMessageToBungeeServer(String bungeeServerName, TeleportPojo info) {
		this._bungeeController.sendDataToServer(bungeeServerName, TELEPORT_TO_PLAYER, info, true);
	}

	private void forcedTpToOnlinePlayer(Player movingPlayer, Player anchorPlayer) {
		movingPlayer.teleport(anchorPlayer);
	}

	public void deny(CommandSender sender, Player localPlayer) {
		List<TeleportPojo> list = null;
		synchronized (this._requestsForTeleport) {
			list = this._requestsForTeleport.remove(localPlayer.getUniqueId());
		}
		for (TeleportPojo info : list) {
			if (info.isTooOld()) continue;
			info.setAsDenyResponse();
			sendTeleportMessageToBungeeServer(info);
		}
	}

	public void accept(CommandSender sender, Player localPlayer) {
		List<TeleportPojo> list = null;
		synchronized (this._requestsForTeleport) {
			list = this._requestsForTeleport.remove(localPlayer.getUniqueId());
		}
		if (list == null) {
			sender.sendMessage("You don't have any pending request.");
			return;
		}
		for (TeleportPojo info : list) {
			if (info.isTooOld()) continue;
			if (info.getMessageDirectionIsFromMovingToAnchor()) {
				tpPlayerHere(sender, localPlayer, info.getMovingPlayerId(), true);
			} else {
				tpToPlayer(sender, localPlayer, info.getAnchorPlayerId(), true);			
			}
		}		
	}

	public List<String> getWarpNames() {
		return WarpLocation.getAllWarpLocations()
				.stream()
				.map(w -> w.getName())
				.collect(Collectors.toList());
	}

	public boolean warpAdd(CommandSender sender, String name, Location location) {
		String bungeeServerName = this._bungeeController.getBungeeServerName();
		if (bungeeServerName == null) {
			sender.sendMessage("Could not find the bungee name for this server. Please try again.");
			return false;
		}
		WarpLocation.getOrCreateByName(name, bungeeServerName, location);
		refreshWarpLocationsAsync();
		broadcastRefresh();
		return true;
	}

	public void refreshWarpLocationsAsync() {
		// End if a new initialize() has been issued.
		new BukkitRunnable() {
			@Override
			public void run() {
				WarpLocation.refresh();
			}
		}
		.runTaskAsynchronously(this._eithonPlugin);
	}

	private void tpToPlayer(CommandSender sender, Player movingPlayer, UUID anchorPlayerId, boolean force) {
		OfflinePlayer anchorPlayer = Bukkit.getOfflinePlayer(anchorPlayerId);
		tpToPlayer(sender, movingPlayer, anchorPlayer, force);
	}

	private void tpPlayerHere(CommandSender sender, Player anchorPlayer, UUID movingPlayerId, boolean force) {
		OfflinePlayer movingPlayer = Bukkit.getOfflinePlayer(movingPlayerId);
		tpPlayerHere(sender, anchorPlayer, movingPlayer, force);
	}

	private void broadcastRefresh() {
		this._bungeeController.sendDataToAll(WARP_LOCATION_REFRESH, null, true);
	}
	
	private void verbose(String method, String format, Object... args)
	{
		this._eithonPlugin.dbgVerbose("TeleportController", method, format, args);
	}
}
