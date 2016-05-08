package net.eithon.plugin.bungee.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.bungee.Config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

public class TeleportController {
	private EithonPlugin _eithonPlugin;
	private HashMap<UUID, TeleportPojo> _waitingForTeleport;
	private HashMap<UUID, List<TeleportPojo>> _requestsForTeleport;
	private String _bungeeServerName;

	public TeleportController(EithonPlugin eithonPlugin) {
		this._eithonPlugin = eithonPlugin;
		this._waitingForTeleport = new HashMap<UUID, TeleportPojo>();
		this._requestsForTeleport = new HashMap<UUID, List<TeleportPojo>>();
		this._bungeeServerName = null;
		WarpLocation.initialize(eithonPlugin);
	}

	public boolean tpToPlayer(CommandSender sender, Player movingPlayer, OfflinePlayer anchorPlayer, boolean force) {
		BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayerOrInformSender(sender, anchorPlayer);
		if (bungeePlayer == null) {
			sender.sendMessage(String.format("Player %s seems to be offline.", anchorPlayer.getName()));
			return false;
		}
		if (anchorPlayer.isOnline() && force) {
			movingPlayer.teleport(anchorPlayer.getPlayer());
		} else {
			TeleportPojo info = new TeleportPojo(movingPlayer, anchorPlayer);
			info.setAsRequestFromMovingPlayer(force);
			String bungeeServerName = bungeePlayer.getBungeeServerName();
			if (!this._eithonPlugin.getApi().playerHasPermissionToAccessServerOrInformSender(sender, movingPlayer, bungeeServerName)) return false;
			sendTeleportMessageToBungeeServer(bungeeServerName, info);
			if (force) this._eithonPlugin.getApi().teleportPlayerToServer(movingPlayer, bungeeServerName);
		}
		return true;
	}

	public boolean warpTo(CommandSender sender, Player player, String name) {
		WarpLocation warpLocation = WarpLocation.getByName(name);
		if (warpLocation.getBungeeServerName().equalsIgnoreCase(getBungeeServerName())) {
			player.teleport(warpLocation.getLocation());
		} else {
			TeleportPojo info = new TeleportPojo(player, name);
			String bungeeServerName = warpLocation.getBungeeServerName();
			if (!this._eithonPlugin.getApi().playerHasPermissionToAccessServerOrInformSender(sender, player, bungeeServerName)) return false;
			sendTeleportMessageToBungeeServer(bungeeServerName, info);
			this._eithonPlugin.getApi().teleportPlayerToServer(player, bungeeServerName);
		}
		return true;
	}

	public void tpPlayerHere(CommandSender sender, Player anchorPlayer, OfflinePlayer movingPlayer, boolean force) {
		BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayerOrInformSender(sender, movingPlayer);
		if (bungeePlayer == null) return;
		
		if (movingPlayer.isOnline() && force) {
			movingPlayer.getPlayer().teleport(anchorPlayer);
		} else {
			TeleportPojo info = new TeleportPojo(movingPlayer, anchorPlayer);
			info.setAsRequestFromAnchorPlayer(force);
			sendTeleportMessageToBungeeServer(bungeePlayer.getBungeeServerName(), info);
		}
	}

	public void handleTeleportEvent(TeleportPojo info) {
		if (info.getMessageType() == TeleportPojo.WARP) {
			// Prepare to teleport the moving player when he/she arrives
			waitForPlayerToComeToServer(info);
			return;
		}
		Player localPlayer = getLocalPlayer(info);
		OfflinePlayer remotePlayer = getRemotePlayer(info);
		if (remotePlayer == null) return;

		switch (info.getMessageType()) {
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
		UUID playerId = info.getMovingPlayerId();
		// Maybe player is already on server?
		Player player = Bukkit.getPlayer(playerId);
		if (player != null) {
			teleportPlayerAccordingToInfo(player, info);
			return;
		}
		this._waitingForTeleport.put(playerId, info);
	}

	private void saveRequest(Player localPlayer, TeleportPojo info) {
		List<TeleportPojo> list = this._requestsForTeleport.get(localPlayer.getUniqueId());
		if (list == null) {
			list = new ArrayList<TeleportPojo>();
			this._requestsForTeleport.put(localPlayer.getUniqueId(), list);
		}
		list.add(info);
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
		BungeePlayer bungeePlayer = BungeePlayer.getByPlayerId(info.getMovingPlayerId());
		if (bungeePlayer == null) return null;
		sendTeleportMessageToBungeeServer(bungeePlayer.getBungeeServerName(), info);
		return null;			
	}

	public void playerJoined(final Player player) {
		String currentBungeeServerName = getBungeeServerName();
		if (currentBungeeServerName != null) {
			playerJoinedAndServerNameIsKnown(player, currentBungeeServerName);
			return;
		}
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				playerJoined(player);
			}
		}, TimeMisc.secondsToTicks(1));
	}

	private void playerJoinedAndServerNameIsKnown(final Player movingPlayer, final String currentBungeeServerName) {
		final BungeePlayer bungeePlayer = BungeePlayer.getOrCreateByOfflinePlayer(movingPlayer, currentBungeeServerName);
		if (bungeePlayer == null) return;
		bungeePlayer.update(getBungeeServerName());
		TeleportPojo info = this._waitingForTeleport.get(movingPlayer.getUniqueId());
		if (info == null) return;
		this._waitingForTeleport.remove(movingPlayer.getUniqueId());
		if (info.isTooOld()) return;
		teleportPlayerAccordingToInfo(movingPlayer, info);
	}

	private void teleportPlayerAccordingToInfo(final Player movingPlayer, TeleportPojo info) {
		if (info.getMessageType() == TeleportPojo.WARP) {
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
		final BungeePlayer anchorBungeePlayer = BungeePlayer.getByPlayerId(info.getAnchorPlayerId());
		if (anchorBungeePlayer == null) return;
		String anchorBungeeServerName = anchorBungeePlayer.getBungeeServerName();
		if (anchorBungeeServerName == null) return;
		if (!anchorBungeeServerName.equalsIgnoreCase(getBungeeServerName())) {
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

	public void playerQuitted(Player player) {
		BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayer(player);
		if (bungeePlayer == null) return;
		bungeePlayer.maybeDelete(getBungeeServerName());
	}

	private void sendTeleportMessageToBungeeServer(TeleportPojo info) {
		UUID remotePlayerId = null;
		if (info.getMessageDirectionIsFromMovingToAnchor()) {
			remotePlayerId = info.getAnchorPlayerId();
		} else {
			remotePlayerId = info.getMovingPlayerId();
		}
		BungeePlayer bungeePlayer = BungeePlayer.getByPlayerId(remotePlayerId);
		if (bungeePlayer == null) return;
		String bungeeServerName = bungeePlayer.getBungeeServerName();
		this._eithonPlugin.getApi().bungeeSendDataToServer(bungeeServerName, "TeleportToPlayer", info, true);
	}

	private void sendTeleportMessageToBungeeServer(String bungeeServerName, TeleportPojo info) {
		this._eithonPlugin.getApi().bungeeSendDataToServer(bungeeServerName, "TeleportToPlayer", info, true);
	}

	private String getBungeeServerName() {
		if (this._bungeeServerName != null) return this._bungeeServerName;
		this._bungeeServerName = this._eithonPlugin.getApi().getBungeeServerName();
		return this._bungeeServerName;
	}

	private void forcedTpToOnlinePlayer(Player movingPlayer, Player anchorPlayer) {
		movingPlayer.teleport(anchorPlayer);
	}

	public void deny(CommandSender sender, Player localPlayer) {
		List<TeleportPojo> list = this._requestsForTeleport.get(localPlayer.getUniqueId());
		for (TeleportPojo info : list) {
			info.setAsDenyResponse();
			sendTeleportMessageToBungeeServer(info);
		}
		this._requestsForTeleport.remove(localPlayer.getUniqueId());
	}

	public void accept(CommandSender sender, Player localPlayer) {
		List<TeleportPojo> list = this._requestsForTeleport.get(localPlayer.getUniqueId());
		if (list == null) {
			sender.sendMessage("You don't have any pending request.");
			return;
		}
		for (TeleportPojo info : list) {
			if (info.getMessageDirectionIsFromMovingToAnchor()) {
				tpPlayerHere(sender, localPlayer, info.getMovingPlayerId(), true);
			} else {
				tpToPlayer(sender, localPlayer, info.getAnchorPlayerId(), true);			
			}
		}
		this._requestsForTeleport.remove(localPlayer.getUniqueId());
	}

	public List<String> getWarpNames() {
		return WarpLocation.getAllWarpLocations()
				.stream()
				.map(w -> w.getName())
				.collect(Collectors.toList());
	}

	public boolean warpAdd(CommandSender sender, String name, Location location) {
		String bungeeServerName = this._eithonPlugin.getApi().getBungeeServerName();
		if (bungeeServerName == null) {
			sender.sendMessage("Could not find the bungee name for this server. Please try again.");
			return false;
		}
		WarpLocation.getOrCreateByName(name, bungeeServerName, location);
		return true;
	}

	private void tpToPlayer(CommandSender sender, Player movingPlayer, UUID anchorPlayerId, boolean force) {
		OfflinePlayer anchorPlayer = Bukkit.getOfflinePlayer(anchorPlayerId);
		tpToPlayer(sender, movingPlayer, anchorPlayer, force);
	}

	private void tpPlayerHere(CommandSender sender, Player anchorPlayer, UUID movingPlayerId, boolean force) {
		OfflinePlayer movingPlayer = Bukkit.getOfflinePlayer(movingPlayerId);
		tpPlayerHere(sender, anchorPlayer, movingPlayer, force);
	}

	public void bungeePlayerJoined(EithonPlayer player, String thatServerName) {
		// TODO Auto-generated method stub
		
	}
}
