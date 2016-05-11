package net.eithon.plugin.bungee.logic;

import java.time.LocalDateTime;
import java.util.UUID;

import net.eithon.library.json.IJsonObject;
import net.eithon.plugin.bungee.Config;

import org.bukkit.OfflinePlayer;
import org.json.simple.JSONObject;

class MessageToPlayerPojo implements IJsonObject<MessageToPlayerPojo>{
	private UUID sendingPlayerId;
	private UUID receiverPlayerId;	// The player that we should teleport to
	private LocalDateTime createdAt;
	private String message;
	
	public MessageToPlayerPojo(OfflinePlayer sendingPlayerId, OfflinePlayer receiverPlayerId, String message) {
		this.sendingPlayerId = sendingPlayerId.getUniqueId();
		this.receiverPlayerId = receiverPlayerId.getUniqueId();
		this.message = message;
		this.createdAt = LocalDateTime.now();
	}

	public UUID getSendingPlayerId() { return this.sendingPlayerId; }
	public UUID getReceiverPlayerId() { return this.receiverPlayerId; }
	public String getMessage() { return this.message; }
	
	private MessageToPlayerPojo() {}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJsonObject() {
		JSONObject json = new JSONObject();
		json.put("sendingPlayerId", this.sendingPlayerId.toString());
		json.put("receiverPlayerId", this.receiverPlayerId.toString());
		json.put("message", this.message);
		json.put("createdAt", this.createdAt.toString());
		return json;
	}

	@Override
	public String toJsonString() {
		return toJsonObject().toJSONString();
	}

	@Override
	public MessageToPlayerPojo fromJsonObject(JSONObject json) {
		this.sendingPlayerId = UUID.fromString((String) json.get("sendingPlayerId"));
		this.receiverPlayerId = UUID.fromString((String) json.get("receiverPlayerId"));
		this.message = (String) json.get("message");
		this.createdAt = LocalDateTime.parse((String) json.get("createdAt"));
		return this;
	}

	public static MessageToPlayerPojo createFromJsonObject(JSONObject json) {
		MessageToPlayerPojo info = new MessageToPlayerPojo();
		return info.fromJsonObject(json);
	}

	public boolean isTooOld() {
		return this.createdAt.plusSeconds(Config.V.maxAllowedMessageDelayInSeconds).isBefore(LocalDateTime.now());
	}
}
