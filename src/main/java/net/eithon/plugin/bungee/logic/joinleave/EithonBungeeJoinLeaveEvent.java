package net.eithon.plugin.bungee.logic.joinleave;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public abstract class EithonBungeeJoinLeaveEvent extends Event {
	private UUID _playerId;
	private String _playerName;
	private String _mainGroup;
	private String _thisServerName;
	private String _thatServerName;

	public EithonBungeeJoinLeaveEvent(String thisServerName, String thatServerName, UUID playerId, String playerName, String mainGroup) {
		this._thisServerName = thisServerName;
		this._thatServerName = thatServerName;
		this._playerId = playerId;
		this._playerName = playerName;
		this._mainGroup = mainGroup;
	}
	
	public EithonBungeeJoinLeaveEvent(String thisServerName, String thatServerName, Player player, String mainGroup) {
		this(thisServerName, thatServerName, player.getUniqueId(), player.getName(), mainGroup);
	}
	
	public UUID getPlayerId() { return this._playerId; }
	public String getPlayerName() { return this._playerName; }

	public String getMainGroup() { return this._mainGroup; }

	public String getThatServerName() { return this._thatServerName; }

	public String getThisServerName() { return this._thisServerName; }

	@Override
	public String toString() {
		return String.format("Player=%s, mainGroup=%s, thisServer=%s, thatServer=%s", 
				this._playerName, this._mainGroup, this._thisServerName, this._thatServerName);
	}
}
