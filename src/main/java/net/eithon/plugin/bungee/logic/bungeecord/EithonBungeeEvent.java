package net.eithon.plugin.bungee.logic.bungeecord;

import net.eithon.library.json.IJson;
import net.eithon.library.json.IJsonObject;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class EithonBungeeEvent extends Event implements IJson<EithonBungeeEvent>, IJsonObject<EithonBungeeEvent>  {
	private static final HandlerList handlers = new HandlerList();
	private String _sourceServerName;
	private String _name;
	private JSONObject _data;
	
	public EithonBungeeEvent() {
		this._name = null;
		this._data = null;
	}
	
	public EithonBungeeEvent(String sourceServerName, String name, JSONObject data) {
		this._sourceServerName = sourceServerName;
		this._name = name;
		this._data = data;
	}
	
	public String getSourceServerName() { return this._sourceServerName; }
	public String getName() { return this._name; }
	public JSONObject getData() { return this._data; }
	
	public static EithonBungeeEvent getFromJsonString(String jsonString) {
		JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonString);
		return getFromJson(jsonObject);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object toJson() {
		JSONObject json = new JSONObject();
		json.put("sourceServerName", this._sourceServerName);
		json.put("name", this._name);
		json.put("data", this._data);
		return json;
	}

	@Override
	public EithonBungeeEvent fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		if (jsonObject == null) return null;
		this._sourceServerName = (String) jsonObject.get("sourceServerName");
		this._name = (String) jsonObject.get("name");
		this._data = (JSONObject) jsonObject.get("data");
		return this;
	}

	@Override
	public EithonBungeeEvent factory() {
		return new EithonBungeeEvent();
	}

	public static EithonBungeeEvent getFromJson(Object json) {
		EithonBungeeEvent info = new EithonBungeeEvent();
		return info.fromJson(json);
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public JSONObject toJsonObject() {
		return (JSONObject) toJson();
	}

	@Override
	public EithonBungeeEvent fromJsonObject(JSONObject json) {
		return fromJson(json);
	}

	@Override
	public String toJsonString() {
		return toJsonObject().toJSONString();
	}
}
