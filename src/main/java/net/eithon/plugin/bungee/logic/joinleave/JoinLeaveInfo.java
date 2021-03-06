package net.eithon.plugin.bungee.logic.joinleave;

import java.util.UUID;

import net.eithon.library.json.JsonObject;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JoinLeaveInfo  extends JsonObject<JoinLeaveInfo> {
	private String _fromServerName;
	private String _toServerName;
	private String _mainGroup;
	private String _playerName;
	private UUID _playerId;
	private boolean _isNewOnServer;
	private boolean _isFirstJoinToday;

	public JoinLeaveInfo(String fromServerName, String toServerName, UUID playerId, String playerName, String mainGroup) {
		this._fromServerName = fromServerName;
		this._toServerName = toServerName;
		this._playerId = playerId;
		this._playerName = playerName;
		this._mainGroup = mainGroup;
		this._isNewOnServer = false;
	}

	public JoinLeaveInfo() { }

	public String getFromServerName() { return this._fromServerName; }
	public String getToServerName() { return this._toServerName; }
	public String getMainGroup() { return this._mainGroup; }
	public String getPlayerName() { return this._playerName; }
	public UUID getPlayerId() { return this._playerId; }
	public boolean getIsNewOnServer() { return this._isNewOnServer; }
	public void setIsNewOnServer() { this._isNewOnServer = true; }
	public boolean getIsFirstJoinToday() { return this._isFirstJoinToday; }
	public void setIsFirstJoinToday() { this._isFirstJoinToday = true; }

	public String toJSONString() {
		return ((JSONObject) toJson()).toJSONString();
	}

	public static JoinLeaveInfo getFromJsonString(String jsonString) {
		JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonString);
		return getFromJson(jsonObject);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object toJson() {
		JSONObject json = new JSONObject();
		json.put("fromServerName", this._fromServerName);
		json.put("toServerName", this._toServerName);
		json.put("mainGroup", this._mainGroup);
		json.put("playerId", this._playerId == null ? null : this._playerId.toString());
		json.put("playerName", this._playerName);
		json.put("isNewOnServer", this._isNewOnServer);
		json.put("isFirstJoinToday", this._isFirstJoinToday);
		return json;
	}

	@Override
	public JoinLeaveInfo fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		if (jsonObject == null) return null;
		this._fromServerName = (String) jsonObject.get("fromServerName");
		this._toServerName = (String) jsonObject.get("toServerName");
		this._mainGroup = (String) jsonObject.get("mainGroup");
		this._playerId = null;
		String uuid = (String) jsonObject.get("playerId");
		if (uuid != null) {
			this._playerId = UUID.fromString(uuid);
		}
		this._playerName = (String) jsonObject.get("playerName");
		this._isNewOnServer = false;
		Boolean isNewOnServer = (Boolean) jsonObject.get("isNewOnServer");
		if (isNewOnServer != null) {
			this._isNewOnServer = isNewOnServer.booleanValue();
		}
		this._isFirstJoinToday = false;
		Boolean isFirstJoinToday = (Boolean) jsonObject.get("isFirstJoinToday");
		if (isFirstJoinToday != null) {
			this._isFirstJoinToday = isFirstJoinToday.booleanValue();
		}
		return this;
	}

	@Override
	public JoinLeaveInfo factory() {
		return new JoinLeaveInfo();
	}

	public static JoinLeaveInfo getFromJson(Object json) {
		JoinLeaveInfo info = new JoinLeaveInfo();
		return info.fromJson(json);
	}

	@Override
	public String toString() {	
		return String.format("Player=%s, mainGroup=%s, fromServerName=%s, toServerName=%s, isNewOnServer=%s, firstJoinToday=%s", 
				this._playerName, this._mainGroup, this._fromServerName, this._toServerName,
				this._isNewOnServer ? "TRUE" : "FALSE", 
						this._isFirstJoinToday ? "TRUE" : "FALSE");
	}
}
