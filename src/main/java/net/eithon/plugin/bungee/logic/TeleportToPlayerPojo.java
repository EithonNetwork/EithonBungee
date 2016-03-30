package net.eithon.plugin.bungee.logic;

import java.time.LocalDateTime;
import java.util.UUID;

import net.eithon.library.json.IJsonObject;
import net.eithon.plugin.bungee.Config;

import org.bukkit.OfflinePlayer;
import org.json.simple.JSONObject;

public class TeleportToPlayerPojo implements IJsonObject<TeleportToPlayerPojo>{
	public static final short FORCE = 1;
	public static final short REQUEST = 2;
	public static final short DENY_RESPONSE = 3;
	
	private UUID movingPlayerId;	// The player that should be teleported
	private UUID anchorPlayerId;	// The player that we should teleport to
	private LocalDateTime createdAt;
	private short messageType;
	private boolean messageDirectionIsFromMovingToAnchor;
	
	public TeleportToPlayerPojo(OfflinePlayer movingPlayer, OfflinePlayer anchorPlayer) {
		this.movingPlayerId = movingPlayer.getUniqueId();
		this.anchorPlayerId = anchorPlayer.getUniqueId();
		this.createdAt = LocalDateTime.now();
	}
	
	public void setAsRequestFromMovingPlayer(boolean force) {
		this.messageType = force ? FORCE : REQUEST;
		this.messageDirectionIsFromMovingToAnchor = true;
	}
	
	public void setAsRequestFromAnchorPlayer(boolean force) {
		this.messageType = force ? FORCE : REQUEST;
		this.messageDirectionIsFromMovingToAnchor = false;
	}
	
	public void setAsDenyResponse() {
		this.messageType = DENY_RESPONSE;
		this.messageDirectionIsFromMovingToAnchor = ! this.messageDirectionIsFromMovingToAnchor;
	}

	public UUID getMovingPlayerId() { return this.movingPlayerId; }
	public UUID getAnchorPlayerId() { return this.anchorPlayerId; }
	public short getMessageType() { return this.messageType; }
	public boolean getMessageDirectionIsFromMovingToAnchor() { return this.messageDirectionIsFromMovingToAnchor; }
	
	private TeleportToPlayerPojo() {}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJsonObject() {
		JSONObject json = new JSONObject();
		json.put("movingPlayerId", this.movingPlayerId.toString());
		json.put("anchorPlayerId", this.anchorPlayerId.toString());
		json.put("createdAt", this.createdAt.toString());
		json.put("messageType", (long) this.messageType);
		json.put("messageDirectionIsFromMovingToAnchor", this.messageDirectionIsFromMovingToAnchor);
		return json;
	}

	@Override
	public String toJsonString() {
		return toJsonObject().toJSONString();
	}

	@Override
	public TeleportToPlayerPojo fromJsonObject(JSONObject json) {
		this.movingPlayerId = UUID.fromString((String) json.get("movingPlayerId"));
		this.anchorPlayerId = UUID.fromString((String) json.get("anchorPlayerId"));
		this.createdAt = LocalDateTime.parse((String) json.get("createdAt"));
		Long type = (Long) json.get("messageType");
		this.messageType = REQUEST;
		if (type != null) this.messageType = type.shortValue();
		Boolean direction = (Boolean) json.get("messageDirectionIsFromMovingToAnchor");
		this.messageDirectionIsFromMovingToAnchor = true;
		if (direction != null) this.messageDirectionIsFromMovingToAnchor = direction.booleanValue();
		return this;
	}

	public static TeleportToPlayerPojo createFromJsonObject(JSONObject json) {
		TeleportToPlayerPojo info = new TeleportToPlayerPojo();
		return info.fromJsonObject(json);
	}

	public boolean isTooOld() {
		return this.createdAt.plusSeconds(Config.V.maxAllowedTeleportDelayInSeconds).isBefore(LocalDateTime.now());
	}
}
