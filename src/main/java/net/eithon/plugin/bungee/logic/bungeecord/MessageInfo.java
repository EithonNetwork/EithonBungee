package net.eithon.plugin.bungee.logic.bungeecord;

import net.eithon.library.json.JsonObject;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class MessageInfo  extends JsonObject<MessageInfo>{
	private String _message;
	private boolean _useTitle;
	
	public MessageInfo() {
		this._message = null;
		this._useTitle = false;
	}
	
	public MessageInfo(String message, boolean useTitle) {
		this._message = message;
		this._useTitle = useTitle;
	}
	
	public String getMessage() { return this._message; }
	public boolean getUseTitle() { return this._useTitle; }
	
	public String toJsonString() {
		return ((JSONObject) toJson()).toJSONString();
	}
	
	public static MessageInfo getFromJsonString(String jsonString) {
		JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonString);
		return getFromJson(jsonObject);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object toJson() {
		JSONObject json = new JSONObject();
		json.put("message", this._message);
		json.put("useTitle", new Boolean(this._useTitle));
		return json;
	}

	@Override
	public MessageInfo fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		if (jsonObject == null) return null;
		this._message = (String) jsonObject.get("message");
		this._useTitle = false;
		Boolean useTitle = (Boolean) jsonObject.get("useTitle");
		if (useTitle != null) this._useTitle = useTitle.booleanValue();
		return this;
	}

	@Override
	public MessageInfo factory() {
		return new MessageInfo();
	}

	public static MessageInfo getFromJson(Object json) {
		MessageInfo info = new MessageInfo();
		return info.fromJson(json);
	}
}
