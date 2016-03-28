package net.eithon.plugin.bungee.logic;

import java.util.HashMap;
import java.util.UUID;

import net.eithon.library.extensions.EithonPlugin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Controller {
	private EithonPlugin _eithonPlugin;
	private HashMap<UUID, TeleportToPlayer> _waitingForTeleport;
	private String _bungeeServerName;

	public Controller(EithonPlugin eithonPlugin) {
		this._eithonPlugin = eithonPlugin;
		this._waitingForTeleport = new HashMap<UUID, TeleportToPlayer>();
		this._bungeeServerName = null;
	}

	public void forcedTpToPlayer(Player sourcePlayer, OfflinePlayer targetPlayer) {
		this._eithonPlugin.getEithonLogger().info("Source player: %s, arget player: %s", 
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
		if (bungeePlayer == null) return;
		String bungeeServerName = bungeePlayer.getBungeeServerName();
		TeleportToPlayer teleportToServer = new TeleportToPlayer(sourcePlayer, targetPlayer);
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

	public void prepareTeleport(TeleportToPlayer info) {
		this._waitingForTeleport.put(info.getSourcePlayerId(), info);
	}

	public void playerJoined(Player player) {
		BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayer(player);
		if (bungeePlayer == null) return;
		bungeePlayer.update(getBungeeServerName());
		TeleportToPlayer info = this._waitingForTeleport.get(player.getUniqueId());
		if (info == null) return;
		this._waitingForTeleport.remove(player.getUniqueId());
		if (info.isTooOld()) return;
		Player targetPlayer = Bukkit.getPlayer(info.getTargetPlayerId());
		if (targetPlayer == null) return;
		forcedTpToOnlinePlayer(player, targetPlayer);
	}

	public void playerQuitted(Player player) {
		BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayer(player);
		if (bungeePlayer == null) return;
		bungeePlayer.update(null);
	}
}
