package net.eithon.plugin.bungee.logic.joinleave;

import java.util.UUID;

import net.eithon.library.json.JsonObject;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JoinQuitInfo  extends JsonObject<JoinQuitInfo> {
	private String _serverName;
	private String _mainGroup;
	private String _playerName;
	private UUID _playerId;
	
	public JoinQuitInfo() {
		this._serverName = null;
		this._mainGroup = null;
		this._playerName = null;
		this._playerId = null;
	}
	
	public JoinQuitInfo(String serverName, UUID playerId, String playerName, String mainGroup) {
		this._serverName = serverName;
		this._playerId = playerId;
		this._playerName = playerName;
		this._mainGroup = mainGroup;
	}
	
	public String getServerName() { return this._serverName; }
	public String getMainGroup() { return this._mainGroup; }
	public String getPlayerName() { return this._playerName; }
	public UUID getPlayerId() { return this._playerId; }
	
	public String toJSONString() {
		return ((JSONObject) toJson()).toJSONString();
	}
	
	public static JoinQuitInfo getFromJsonString(String jsonString) {
		JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonString);
		return getFromJson(jsonObject);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object toJson() {
		JSONObject json = new JSONObject();
		json.put("serverName", this._serverName);
		json.put("mainGroup", this._mainGroup);
		json.put("playerId", this._playerId == null ? null : this._playerId.toString());
		json.put("playerName", this._playerName);
		return json;
	}

	@Override
	public JoinQuitInfo fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		if (jsonObject == null) return null;
		this._serverName = (String) jsonObject.get("serverName");
		this._mainGroup = (String) jsonObject.get("mainGroup");
		this._playerId = null;
		String uuid = (String) jsonObject.get("playerId");
		if (uuid != null) {
			this._playerId = UUID.fromString(uuid);
		}
		this._playerName = (String) jsonObject.get("playerName");
		return this;
	}

	@Override
	public JoinQuitInfo factory() {
		return new JoinQuitInfo();
	}

	public static JoinQuitInfo getFromJson(Object json) {
		JoinQuitInfo info = new JoinQuitInfo();
		return info.fromJson(json);
	}
}
