package net.eithon.plugin.bungee.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.bungee.Config;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

public class TeleportController {
	private EithonPlugin _eithonPlugin;
	private HashMap<UUID, TeleportToPlayerPojo> _waitingForTeleport;
	private HashMap<UUID, List<TeleportToPlayerPojo>> _requestsForTeleport;
	private String _bungeeServerName;

	public TeleportController(EithonPlugin eithonPlugin) {
		this._eithonPlugin = eithonPlugin;
		this._waitingForTeleport = new HashMap<UUID, TeleportToPlayerPojo>();
		this._requestsForTeleport = new HashMap<UUID, List<TeleportToPlayerPojo>>();
		this._bungeeServerName = null;
	}

	public void tpToPlayer(CommandSender sender, Player movingPlayer, OfflinePlayer anchorPlayer, boolean force) {
		BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayerOrInformSender(sender, anchorPlayer);
		if (anchorPlayer.isOnline() && force) {
			movingPlayer.teleport(anchorPlayer.getPlayer());
		} else {
			TeleportToPlayerPojo info = new TeleportToPlayerPojo(movingPlayer, anchorPlayer);
			info.setAsRequestFromMovingPlayer(force);
			String bungeeServerName = bungeePlayer.getBungeeServerName();
			sendTeleportMessageToBungeeServer(bungeeServerName, info);
			if (force) this._eithonPlugin.getApi().teleportPlayerToServer(movingPlayer, bungeeServerName);
		}
	}

	public void tpPlayerHere(CommandSender sender, Player anchorPlayer, OfflinePlayer movingPlayer, boolean force) {
		BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayerOrInformSender(sender, movingPlayer);
		if (bungeePlayer == null) return;
		
		if (movingPlayer.isOnline() && force) {
			movingPlayer.getPlayer().teleport(anchorPlayer);
		} else {
			TeleportToPlayerPojo info = new TeleportToPlayerPojo(movingPlayer, anchorPlayer);
			info.setAsRequestFromAnchorPlayer(force);
			sendTeleportMessageToBungeeServer(bungeePlayer.getBungeeServerName(), info);
		}
	}

	public void handleTeleportEvent(TeleportToPlayerPojo info) {
		Player localPlayer = getLocalPlayer(info);
		OfflinePlayer remotePlayer = getRemotePlayer(info);
		if (remotePlayer == null) return;

		switch (info.getMessageType()) {
		case TeleportToPlayerPojo.FORCE:
			if (info.getMessageDirectionIsFromMovingToAnchor()) {
				// Prepare to teleport the source player when he/she arrives
				this._waitingForTeleport.put(info.getMovingPlayerId(), info);
			} else {
				// Initiate a teleport from this server
				tpToPlayer(null, localPlayer, remotePlayer, true);
			}
			break;
		case TeleportToPlayerPojo.REQUEST:
			saveRequest(localPlayer, info);
			if (info.getMessageDirectionIsFromMovingToAnchor()) {
				requestTpTo(localPlayer, remotePlayer);
			} else {
				requestTpHere(localPlayer, remotePlayer);
			}
			break;
		case TeleportToPlayerPojo.DENY_RESPONSE:
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

	private void saveRequest(Player localPlayer, TeleportToPlayerPojo info) {
		List<TeleportToPlayerPojo> list = this._requestsForTeleport.get(localPlayer.getUniqueId());
		if (list == null) {
			list = new ArrayList<TeleportToPlayerPojo>();
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

	private Player getLocalPlayer(TeleportToPlayerPojo info) {
		if (info.getMessageDirectionIsFromMovingToAnchor()) {
			return Bukkit.getPlayer(info.getAnchorPlayerId());
		} else {
			return getSourcePlayerOrForwardMessage(info);
		}
	}

	private OfflinePlayer getRemotePlayer(TeleportToPlayerPojo info) {
		if (info.getMessageDirectionIsFromMovingToAnchor()) {
			return Bukkit.getOfflinePlayer(info.getMovingPlayerId());
		} else {
			return Bukkit.getOfflinePlayer(info.getAnchorPlayerId());
		}
	}

	private Player getSourcePlayerOrForwardMessage(TeleportToPlayerPojo info) {
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

	private void playerJoinedAndServerNameIsKnown(final Player player, final String currentBungeeServerName) {
		final BungeePlayer bungeePlayer = BungeePlayer.getOrCreateByOfflinePlayer(player, currentBungeeServerName);
		if (bungeePlayer == null) return;
		bungeePlayer.update(getBungeeServerName());
		TeleportToPlayerPojo info = this._waitingForTeleport.get(player.getUniqueId());
		if (info == null) return;
		this._waitingForTeleport.remove(player.getUniqueId());
		if (info.isTooOld()) return;
		final BungeePlayer anchorBungeePlayer = BungeePlayer.getByPlayerId(info.getAnchorPlayerId());
		if (anchorBungeePlayer == null) return;
		String anchorBungeeServerName = anchorBungeePlayer.getBungeeServerName();
		if (anchorBungeeServerName == null) return;
		if (!anchorBungeeServerName.equalsIgnoreCase(getBungeeServerName())) {
			// The player has moved to another server, make another server switch
			OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(info.getAnchorPlayerId());
			if (targetPlayer == null) return;
			tpToPlayer(null, player, targetPlayer, true);
			return;
		}
		Player anchorPlayer = Bukkit.getPlayer(info.getAnchorPlayerId());
		if (anchorPlayer == null) return;
		forcedTpToOnlinePlayer(player, anchorPlayer);
	}

	public void playerQuitted(Player player) {
		BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayer(player);
		if (bungeePlayer == null) return;
		bungeePlayer.maybeDelete(getBungeeServerName());
	}

	private void sendTeleportMessageToBungeeServer(TeleportToPlayerPojo info) {
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

	private void sendTeleportMessageToBungeeServer(String bungeeServerName, TeleportToPlayerPojo info) {
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
		List<TeleportToPlayerPojo> list = this._requestsForTeleport.get(localPlayer.getUniqueId());
		for (TeleportToPlayerPojo info : list) {
			info.setAsDenyResponse();
			sendTeleportMessageToBungeeServer(info);
		}
		this._requestsForTeleport.remove(localPlayer.getUniqueId());
	}

	public void accept(CommandSender sender, Player localPlayer) {
		List<TeleportToPlayerPojo> list = this._requestsForTeleport.get(localPlayer.getUniqueId());
		if (list == null) {
			sender.sendMessage("You don't have any pending request.");
			return;
		}
		for (TeleportToPlayerPojo info : list) {
			if (info.getMessageDirectionIsFromMovingToAnchor()) {
				tpPlayerHere(sender, localPlayer, info.getMovingPlayerId(), true);
			} else {
				tpToPlayer(sender, localPlayer, info.getAnchorPlayerId(), true);			
			}
		}
		this._requestsForTeleport.remove(localPlayer.getUniqueId());
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
