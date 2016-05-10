package net.eithon.plugin.bungee.logic.bungeecord;

import net.eithon.library.extensions.EithonPlayer;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public abstract class EithonBungeeJoinQuitEvent extends Event {
	private EithonPlayer _player;
	private String _mainGroup;
	private String _thisServerName;
	private String _thatServerName;

	public EithonBungeeJoinQuitEvent(String thisServerName, String thatServerName, EithonPlayer player, String mainGroup) {
		this._thisServerName = thisServerName;
		this._thatServerName = thatServerName;
		this._player = player;
		this._mainGroup = mainGroup;
	}

	public EithonBungeeJoinQuitEvent(String thisServerName, String thatServerName, Player player, String mainGroup) {
		this(thisServerName, thatServerName, new EithonPlayer(player), mainGroup);
	}

	public EithonPlayer getPlayer() { return this._player; }

	public String getMainGroup() { return this._mainGroup; }

	public String getThatServerName() { return this._thatServerName; }

	public String getThisServerName() { return this._thisServerName; }
}
