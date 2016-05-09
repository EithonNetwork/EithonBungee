package net.eithon.plugin.bungee.logic.teleport;

import java.time.LocalDateTime;
import java.util.UUID;

import net.eithon.library.json.IJsonObject;
import net.eithon.plugin.bungee.Config;

import org.bukkit.OfflinePlayer;
import org.json.simple.JSONObject;

class TeleportPojo implements IJsonObject<TeleportPojo>{
	public static final short WARP = 1;
	public static final short PLAYER_FORCE = 2;
	public static final short PLAYER_REQUEST = 3;
	public static final short PLAYER_DENY_RESPONSE = 4;
	
	private UUID movingPlayerId;	// The player that should be teleported
	private UUID anchorPlayerId;	// The player that we should teleport to
	private String warpLocationName;
	private LocalDateTime createdAt;
	private short messageType;
	private boolean messageDirectionIsFromPlayerMoving;
	
	public TeleportPojo(OfflinePlayer movingPlayer, OfflinePlayer anchorPlayer) {
		this.movingPlayerId = movingPlayer.getUniqueId();
		this.anchorPlayerId = anchorPlayer.getUniqueId();
		this.createdAt = LocalDateTime.now();
	}
	
	public TeleportPojo(OfflinePlayer movingPlayer, String warpLocationName) {
		this.movingPlayerId = movingPlayer.getUniqueId();
		this.warpLocationName = warpLocationName;
		this.createdAt = LocalDateTime.now();
		setAsWarp();
	}
	
	public void setAsWarp() {
		this.messageType = WARP;
		this.messageDirectionIsFromPlayerMoving = true;
	}
	
	public void setAsRequestFromMovingPlayer(boolean force) {
		this.messageType = force ? PLAYER_FORCE : PLAYER_REQUEST;
		this.messageDirectionIsFromPlayerMoving = true;
	}
	
	public void setAsRequestFromAnchorPlayer(boolean force) {
		this.messageType = force ? PLAYER_FORCE : PLAYER_REQUEST;
		this.messageDirectionIsFromPlayerMoving = false;
	}
	
	public void setAsDenyResponse() {
		this.messageType = PLAYER_DENY_RESPONSE;
		this.messageDirectionIsFromPlayerMoving = ! this.messageDirectionIsFromPlayerMoving;
	}

	public UUID getMovingPlayerId() { return this.movingPlayerId; }
	public UUID getAnchorPlayerId() { return this.anchorPlayerId; }
	public String getWarpLocationName() { return this.warpLocationName; }
	public short getMessageType() { return this.messageType; }
	public boolean getMessageDirectionIsFromMovingToAnchor() { return this.messageDirectionIsFromPlayerMoving; }
	
	private TeleportPojo() {}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJsonObject() {
		JSONObject json = new JSONObject();
		json.put("movingPlayerId", this.movingPlayerId.toString());
		json.put("anchorPlayerId", this.anchorPlayerId == null ? null : this.anchorPlayerId.toString());
		json.put("warpLocationName", this.warpLocationName);
		json.put("createdAt", this.createdAt.toString());
		json.put("messageType", (long) this.messageType);
		json.put("messageDirectionIsFromMovingToAnchor", this.messageDirectionIsFromPlayerMoving);
		return json;
	}

	@Override
	public String toJsonString() {
		return toJsonObject().toJSONString();
	}

	@Override
	public TeleportPojo fromJsonObject(JSONObject json) {
		this.movingPlayerId = UUID.fromString((String) json.get("movingPlayerId"));
		String UuidAsString = (String) json.get("anchorPlayerId");
		this.anchorPlayerId = UuidAsString == null ? null : UUID.fromString(UuidAsString);
		this.warpLocationName = ((String) json.get("warpLocationName"));
		this.createdAt = LocalDateTime.parse((String) json.get("createdAt"));
		Long type = (Long) json.get("messageType");
		this.messageType = PLAYER_REQUEST;
		if (type != null) this.messageType = type.shortValue();
		Boolean direction = (Boolean) json.get("messageDirectionIsFromMovingToAnchor");
		this.messageDirectionIsFromPlayerMoving = true;
		if (direction != null) this.messageDirectionIsFromPlayerMoving = direction.booleanValue();
		return this;
	}

	public static TeleportPojo createFromJsonObject(JSONObject json) {
		TeleportPojo info = new TeleportPojo();
		return info.fromJsonObject(json);
	}

	public boolean isTooOld() {
		return this.createdAt.plusSeconds(Config.V.maxAllowedTeleportDelayInSeconds).isBefore(LocalDateTime.now());
	}
}
