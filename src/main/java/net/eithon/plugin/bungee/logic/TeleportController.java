package net.eithon.plugin.bungee.logic;

import java.util.HashMap;
import java.util.UUID;

import net.eithon.library.extensions.EithonPlugin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportController {
	private EithonPlugin _eithonPlugin;
	private HashMap<UUID, TeleportToPlayerPojo> _waitingForTeleport;
	private String _bungeeServerName;

	public TeleportController(EithonPlugin eithonPlugin) {
		this._eithonPlugin = eithonPlugin;
		this._waitingForTeleport = new HashMap<UUID, TeleportToPlayerPojo>();
		this._bungeeServerName = null;
	}

	public void tpToPlayer(CommandSender sender, Player sourcePlayer, OfflinePlayer targetPlayer, boolean force) {
		this._eithonPlugin.getEithonLogger().info("Source player: %s, target player: %s", 
				sourcePlayer.getName(), targetPlayer.getName());
		if (targetPlayer.isOnline()) {
			forcedTpToOnlinePlayer(sourcePlayer, targetPlayer.getPlayer());
		} else {
			tpToBungeePlayer(sender, sourcePlayer, targetPlayer, force);
		}
	}

	public void handleTeleportEvent(TeleportToPlayerPojo info) {
		Player localPlayer = getLocalPlayer(info);

		switch (info.getMessageType()) {
		case TeleportToPlayerPojo.FORCE:
			if (info.getMessageDirectionIsFromSourceToTarget()) {
				// Prepare to teleport the source player when he/she arrives
				this._waitingForTeleport.put(info.getSourcePlayerId(), info);
			} else {
				// Initiate a teleport from this server
				OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(info.getTargetPlayerId());
				if (targetPlayer == null) return;
				tpToPlayer(null, localPlayer, targetPlayer, true);
			}
			break;
		case TeleportToPlayerPojo.REQUEST:
			if (info.getMessageDirectionIsFromSourceToTarget()) {
				requestTpTo(localPlayer);
			} else {
				requestTpHere(localPlayer);
			}
			break;
		case TeleportToPlayerPojo.DENY_RESPONSE:
			if (info.getMessageDirectionIsFromSourceToTarget()) {
				denyTpHere(localPlayer, info.getSourcePlayerId());
			} else {
				denyTpTo(localPlayer, info.getTargetPlayerId());
			}
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	private Player getLocalPlayer(TeleportToPlayerPojo info) {
		if (info.getMessageDirectionIsFromSourceToTarget()) {
			return Bukkit.getPlayer(info.getTargetPlayerId());
		} else {
			return getSourcePlayerOrForwardMessage(info);
		}
	}

	private Player getSourcePlayerOrForwardMessage(TeleportToPlayerPojo info) {
		Player sourcePlayer = Bukkit.getPlayer(info.getSourcePlayerId());
		// Verify that the source player is on line on this server
		if (sourcePlayer != null) return sourcePlayer;

		// The source player was unexpectedly not found on this server.
		// Find the server that the player has moved to and forward the message there.
		BungeePlayer bungeePlayer = BungeePlayer.getByPlayerId(info.getSourcePlayerId());
		if (bungeePlayer == null) return null;
		sendTeleportMessageToBungeeServer(bungeePlayer.getBungeeServerName(), info);
		return null;			
	}

	public void playerJoined(Player player) {
		BungeePlayer bungeePlayer = BungeePlayer.getOrCreateByOfflinePlayer(player, getBungeeServerName());
		if (bungeePlayer == null) return;
		bungeePlayer.update(getBungeeServerName());
		TeleportToPlayerPojo info = this._waitingForTeleport.get(player.getUniqueId());
		if (info == null) return;
		this._waitingForTeleport.remove(player.getUniqueId());
		if (info.isTooOld()) return;
		bungeePlayer = BungeePlayer.getByPlayerId(info.getTargetPlayerId());
		if (bungeePlayer == null) return;
		String bungeeServerName = bungeePlayer.getBungeeServerName();
		if (bungeeServerName == null) return;
		if (!bungeeServerName.equalsIgnoreCase(getBungeeServerName())) {
			// The player has moved to another server, make another server switch
			OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(info.getTargetPlayerId());
			if (targetPlayer == null) return;
			tpToBungeePlayer(null, player, targetPlayer, true);
			return;
		}
		Player targetPlayer = Bukkit.getPlayer(info.getTargetPlayerId());
		if (targetPlayer == null) return;
		forcedTpToOnlinePlayer(player, targetPlayer);
	}

	public void playerQuitted(Player player) {
		BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayer(player);
		if (bungeePlayer == null) return;
		bungeePlayer.maybeDelete(getBungeeServerName());
	}

	private void tpToBungeePlayer(CommandSender sender, Player sourcePlayer, OfflinePlayer targetPlayer, boolean force) {
		BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayer(targetPlayer);
		if (bungeePlayer == null) {
			if (sender != null) sender.sendMessage(String.format("Could not find player %s on any server.", targetPlayer.getName()));
			return;
		}
		String bungeeServerName = bungeePlayer.getBungeeServerName();
		if (bungeeServerName.equalsIgnoreCase(getBungeeServerName())) {
			bungeePlayer.maybeDelete(bungeeServerName);
			if (sender != null) sender.sendMessage(String.format("Could not find player %s on any server.", targetPlayer.getName()));
			return;
		}
		TeleportToPlayerPojo teleportToServer = new TeleportToPlayerPojo(sourcePlayer, targetPlayer);
		teleportToServer.setAsRequestFromSourcePlayer(force);
		sendTeleportMessageToBungeeServer(bungeeServerName, teleportToServer);
		if (force) this._eithonPlugin.getApi().teleportPlayerToServer(sourcePlayer, bungeeServerName);
	}

	private void sendTeleportMessageToBungeeServer(String bungeeServerName, TeleportToPlayerPojo teleportToServer) {
		this._eithonPlugin.getApi().bungeeSendDataToServer(bungeeServerName, "TeleportToPlayer", teleportToServer, true);
	}

	private String getBungeeServerName() {
		if (this._bungeeServerName != null) return this._bungeeServerName;
		this._bungeeServerName = this._eithonPlugin.getApi().getBungeeServerName();
		return this._bungeeServerName;
	}

	private void forcedTpToOnlinePlayer(Player sourcePlayer, Player targetPlayer) {
		sourcePlayer.teleport(targetPlayer);
	}
}
