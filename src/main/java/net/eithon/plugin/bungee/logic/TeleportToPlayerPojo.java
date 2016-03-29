package net.eithon.plugin.bungee.logic;

import java.time.LocalDateTime;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import net.eithon.library.json.IJsonObject;
import net.eithon.plugin.bungee.Config;

public class TeleportToPlayerPojo implements IJsonObject<TeleportToPlayerPojo>{
	private static short FORCE= 1;
	private static short REQUEST = 2;
	private static short DENY_RESPONSE = 3;
	
	private UUID sourcePlayerId;	// The player that should be teleported
	private UUID targetPlayerId;	// The player that we should teleport to
	private LocalDateTime createdAt;
	private short messageType;
	private boolean messageDirectionIsFromSourceToTarget;
	
	public TeleportToPlayerPojo(Player sourcePlayer, OfflinePlayer targetPlayer) {
		this.sourcePlayerId = sourcePlayer.getUniqueId();
		this.targetPlayerId = targetPlayer.getUniqueId();
		this.createdAt = LocalDateTime.now();
	}
	
	public void setAsRequestFromSourcePlayer(boolean force) {
		this.messageType = force ? FORCE : REQUEST;
		this.messageDirectionIsFromSourceToTarget = true;
	}
	
	public void setAsRequestFromTargetPlayer(boolean force) {
		this.messageType = force ? FORCE : REQUEST;
		this.messageDirectionIsFromSourceToTarget = false;
	}
	
	public void setAsDenyResponse() {
		this.messageType = DENY_RESPONSE;
		this.messageDirectionIsFromSourceToTarget = ! this.messageDirectionIsFromSourceToTarget;
	}
	
	private TeleportToPlayerPojo() {}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJsonObject() {
		JSONObject json = new JSONObject();
		json.put("sourcePlayerId", this.sourcePlayerId.toString());
		json.put("targetPlayerId", this.targetPlayerId.toString());
		json.put("createdAt", this.createdAt.toString());
		json.put("messageType", (long) this.messageType);
		json.put("messageDirectionIsFromSourceToTarget", this.messageDirectionIsFromSourceToTarget);
		return json;
	}

	@Override
	public String toJsonString() {
		return toJsonObject().toJSONString();
	}

	@Override
	public TeleportToPlayerPojo fromJsonObject(JSONObject json) {
		this.sourcePlayerId = UUID.fromString((String) json.get("sourcePlayerId"));
		this.targetPlayerId = UUID.fromString((String) json.get("targetPlayerId"));
		this.createdAt = LocalDateTime.parse((String) json.get("createdAt"));
		Long type = (Long) json.get("messageType");
		this.messageType = REQUEST;
		if (type != null) this.messageType = type.shortValue();
		Boolean direction = (Boolean) json.get("messageDirectionIsFromSourceToTarget");
		this.messageDirectionIsFromSourceToTarget = true;
		if (direction != null) this.messageDirectionIsFromSourceToTarget = direction.booleanValue();
		return this;
	}

	public static TeleportToPlayerPojo createFromJsonObject(JSONObject json) {
		TeleportToPlayerPojo info = new TeleportToPlayerPojo();
		return info.fromJsonObject(json);
	}

	public UUID getSourcePlayerId() { return this.sourcePlayerId; }
	public UUID getTargetPlayerId() { return this.targetPlayerId; }

	public boolean isTooOld() {
		return this.createdAt.plusSeconds(Config.V.maxAllowedTeleportDelayInSeconds).isBefore(LocalDateTime.now());
	}
}
