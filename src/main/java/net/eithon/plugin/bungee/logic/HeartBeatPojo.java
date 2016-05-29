package net.eithon.plugin.bungee.logic;

import java.time.LocalDateTime;

import net.eithon.library.json.IJsonObject;
import net.eithon.plugin.bungee.Config;

import org.json.simple.JSONObject;

class HeartBeatPojo implements IJsonObject<HeartBeatPojo>{
	private String serverName;
	private LocalDateTime createdAt;
	
	public HeartBeatPojo(String serverName) {
		this.serverName = serverName;
		this.createdAt = LocalDateTime.now();
	}

	public String getServerName() { return this.serverName; }
	
	private HeartBeatPojo() {}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJsonObject() {
		JSONObject json = new JSONObject();
		json.put("serverName", this.serverName);
		json.put("createdAt", this.createdAt.toString());
		return json;
	}

	@Override
	public String toJsonString() {
		return toJsonObject().toJSONString();
	}

	@Override
	public HeartBeatPojo fromJsonObject(JSONObject json) {
		this.serverName = ((String) json.get("serverName"));
		this.createdAt = LocalDateTime.parse((String) json.get("createdAt"));
		return this;
	}

	public static HeartBeatPojo createFromJsonObject(JSONObject json) {
		HeartBeatPojo info = new HeartBeatPojo();
		return info.fromJsonObject(json);
	}

	public boolean isTooOld() {
		return this.createdAt.plusSeconds(Config.V.secondsBetweenHeartBeats).isBefore(LocalDateTime.now());
	}
}
