package net.eithon.plugin.bungee.logic;

import java.util.HashMap;
import java.util.UUID;

import net.eithon.library.extensions.EithonPlugin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

	public void forcedTpToPlayer(Player sourcePlayer, OfflinePlayer targetPlayer) {
		this._eithonPlugin.getEithonLogger().info("Source player: %s, target player: %s", 
				sourcePlayer.getName(), targetPlayer.getName());
		if (targetPlayer.isOnline()) {
			forcedTpToOnlinePlayer(sourcePlayer, targetPlayer.getPlayer());
		} else {
			forcedTpToBungeePlayer(sourcePlayer, targetPlayer);
		}
	}

	private void forcedTpToBungeePlayer(Player sourcePlayer,
			OfflinePlayer targetPlayer) {
		BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayer(targetPlayer);
		if (bungeePlayer == null) {
			sourcePlayer.sendMessage(String.format("Could not find player %s on any server.", targetPlayer.getName()));
			return;
		}
		String bungeeServerName = bungeePlayer.getBungeeServerName();
		if (bungeeServerName == null) {
			sourcePlayer.sendMessage(String.format("Could not find player %s on any server.", targetPlayer.getName()));
			return;
		}
		if (bungeeServerName.equalsIgnoreCase(getBungeeServerName())) {
			bungeePlayer.update(bungeeServerName, false);
			sourcePlayer.sendMessage(String.format("Could not find player %s on any server.", targetPlayer.getName()));
			return;
		}
		TeleportToPlayerPojo teleportToServer = new TeleportToPlayerPojo(sourcePlayer, targetPlayer);
		this._eithonPlugin.getApi().bungeeSendDataToServer(bungeeServerName, "TeleportToPlayer", teleportToServer, true);
		this._eithonPlugin.getApi().teleportPlayerToServer(sourcePlayer, bungeeServerName);
	}
	
	private String getBungeeServerName() {
		if (this._bungeeServerName != null) return this._bungeeServerName;
		this._bungeeServerName = this._eithonPlugin.getApi().getBungeeServerName();
		return this._bungeeServerName;
	}

	private void forcedTpToOnlinePlayer(Player sourcePlayer, Player targetPlayer) {
		sourcePlayer.teleport(targetPlayer);
	}

	public void prepareTeleport(TeleportToPlayerPojo info) {
		this._waitingForTeleport.put(info.getSourcePlayerId(), info);
	}

	public void playerJoined(Player player) {
		BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayer(player);
		if (bungeePlayer == null) return;
		bungeePlayer.update(getBungeeServerName(), true);
		TeleportToPlayerPojo info = this._waitingForTeleport.get(player.getUniqueId());
		if (info == null) return;
		this._waitingForTeleport.remove(player.getUniqueId());
		if (info.isTooOld()) return;
		bungeePlayer = BungeePlayer.getByPlayerId(info.getTargetPlayerId());
		String bungeeServerName = bungeePlayer.getBungeeServerName();
		if (bungeeServerName == null) return;
		if (!bungeeServerName.equalsIgnoreCase(getBungeeServerName())) {
			// The player has moved to another server, make another server switch
			OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(info.getTargetPlayerId());
			if (targetPlayer == null) return;
			forcedTpToBungeePlayer(player, targetPlayer);
			return;
		}
		Player targetPlayer = Bukkit.getPlayer(info.getTargetPlayerId());
		if (targetPlayer == null) return;
		forcedTpToOnlinePlayer(player, targetPlayer);
	}

	public void playerQuitted(Player player) {
		BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayer(player);
		if (bungeePlayer == null) return;
		bungeePlayer.update(getBungeeServerName(), false);
	}
}
