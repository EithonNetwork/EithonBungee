package net.eithon.plugin.bungee.logic.bungeecord;

import java.time.LocalDateTime;

import net.eithon.library.json.JsonObject;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class ForwardHeader extends JsonObject<ForwardHeader>{
	private String _commandName;
	private String _sourceServerName;
	private LocalDateTime _messageSendTime;
	private boolean _rejectOld;
	
	public ForwardHeader() {
		this._commandName = null;
		this._sourceServerName = null;
		this._messageSendTime = null;
		this._rejectOld = false;
	}
	
	public ForwardHeader(String commandName, String sourceServerName, boolean rejectOld) {
		this._commandName = commandName;
		this._sourceServerName = sourceServerName;
		this._rejectOld = rejectOld;
		this._messageSendTime = null;
	}
	
	public String getCommandName() { return this._commandName; }
	public String getSourceServerName() { return this._sourceServerName; }
	public LocalDateTime getMessageSendTime() { return this._messageSendTime; }
	public boolean getRejectOld() { return this._rejectOld; }

	public String investigateTime() {
		if (!this._rejectOld) return "this._rejectOld == false";
		if (this._messageSendTime == null) return "this._messageSendTime == null";
		return String.format("now-3: %s, sent: %s", LocalDateTime.now().minusSeconds(3).toString(), this._messageSendTime.toString());
	}
	
	public boolean isTooOld() {
		if (!this._rejectOld) return false;
		if (this._messageSendTime == null) return false;
		return LocalDateTime.now().minusSeconds(3).isAfter(this._messageSendTime);
	}
	
	public String toJSONString() {
		return ((JSONObject) toJson()).toJSONString();
	}
	
	public static ForwardHeader getFromJsonString(String jsonString) {
		JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonString);
		return getFromJson(jsonObject);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object toJson() {
		JSONObject json = new JSONObject();
		json.put("commandName", this._commandName);
		json.put("sourceServerName", this._sourceServerName);
		json.put("rejectOld", new Boolean(this._rejectOld));
		json.put("messageSendTime", LocalDateTime.now().toString());
		return json;
	}

	@Override
	public ForwardHeader fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		if (jsonObject == null) return null;
		this._commandName = (String) jsonObject.get("commandName");
		this._sourceServerName = (String) jsonObject.get("sourceServerName");
		Boolean rejectOld = (Boolean) jsonObject.get("rejectOld");
		this._rejectOld = false;
		if (rejectOld != null)  this._rejectOld = rejectOld.booleanValue();
		this._messageSendTime = null;
		String sendTime = (String) jsonObject.get("messageSendTime");
		if (sendTime != null) this._messageSendTime = LocalDateTime.parse(sendTime);
		return this;
	}

	@Override
	public ForwardHeader factory() {
		return new ForwardHeader();
	}

	public static ForwardHeader getFromJson(Object json) {
		ForwardHeader info = new ForwardHeader();
		return info.fromJson(json);
	}
}
