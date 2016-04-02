package net.eithon.plugin.bungee.logic;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;

import net.eithon.library.core.PlayerCollection;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;

public class BungeePlayers {
	private PlayerCollection<BungeePlayer> _bungeePlayers;

	public BungeePlayers(EithonPlugin eithonPlugin) {
		refresh();
	}

	private void refresh() {
		this._bungeePlayers = new PlayerCollection<BungeePlayer>();
		for (BungeePlayer bungeePlayer : BungeePlayer.findAll()) {
			this._bungeePlayers.put(bungeePlayer.getOfflinePlayer(), bungeePlayer);
		};
	}

	public void put(EithonPlayer player, String thatServerName) {
		BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayer(player.getOfflinePlayer());
		if (bungeePlayer == null) return;
		this._bungeePlayers.put(player, bungeePlayer);
	}

	public void remove(EithonPlayer player, String thatServerName) {
		this._bungeePlayers.remove(player);
	}
	
	public List<String> getNames() {
		return this._bungeePlayers.values()
				.stream()
				.map(bp -> bp.getOfflinePlayer().getName())
				.collect(Collectors.toList());
	}

	public BungeePlayer getBungeePlayer(OfflinePlayer player) {
		if (this._bungeePlayers.hasInformation(player)) return this._bungeePlayers.get(player);
		BungeePlayer bungeePlayer = BungeePlayer.getByOfflinePlayer(player);
		if (bungeePlayer == null) return null;
		refresh();
		return bungeePlayer;
	}
}
