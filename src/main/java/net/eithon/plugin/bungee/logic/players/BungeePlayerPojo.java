package net.eithon.plugin.bungee.logic.players;

import java.util.UUID;

import net.eithon.library.json.JsonObject;

import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

class BungeePlayerPojo  extends JsonObject<BungeePlayerPojo> {
	private UUID _playerId;
	private String _playerName;
	private String _bungeeServerName;
	
	public BungeePlayerPojo() {
		this._playerId = null;
		this._playerName = null;
		this._bungeeServerName = null;
	}
	
	public BungeePlayerPojo(Player player, String bungeeServerName) {
		this._playerId = player.getUniqueId();
		this._playerName = player.getName();
		this._bungeeServerName = bungeeServerName;
	}
	
	public UUID getPlayerId() { return this._playerId; }
	public String getPlayerName() { return this._playerName; }
	public String getBungeeServerName() { return this._bungeeServerName; }
	
	public String toJSONString() {
		return ((JSONObject) toJson()).toJSONString();
	}
	
	public static BungeePlayerPojo getFromJsonString(String jsonString) {
		JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonString);
		return getFromJson(jsonObject);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object toJson() {
		JSONObject json = new JSONObject();
		json.put("playerId", this._playerId == null ? null : this._playerId.toString());
		json.put("playerName", this._playerName);
		json.put("bungeeServerName", this._bungeeServerName);
		return json;
	}

	@Override
	public BungeePlayerPojo fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		if (jsonObject == null) return null;
		this._playerId = null;
		String uuid = (String) jsonObject.get("playerId");
		if (uuid != null) {
			this._playerId = UUID.fromString(uuid);
		}
		this._playerName = (String) jsonObject.get("playerName");
		this._bungeeServerName = (String) jsonObject.get("bungeeServerName");
		return this;
	}

	@Override
	public BungeePlayerPojo factory() {
		return new BungeePlayerPojo();
	}

	public static BungeePlayerPojo getFromJson(Object json) {
		BungeePlayerPojo info = new BungeePlayerPojo();
		return info.fromJson(json);
	}
}
