package net.eithon.plugin.bungee.logic;

import java.time.LocalDateTime;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import net.eithon.library.json.IJsonObject;
import net.eithon.plugin.bungee.Config;

public class TeleportToPlayerPojo implements IJsonObject<TeleportToPlayerPojo>{
	private UUID sourcePlayerId;
	private UUID targetPlayerId;
	private LocalDateTime createdAt;
	
	public TeleportToPlayerPojo(Player sourcePlayer, OfflinePlayer targetPlayer) {
		this.sourcePlayerId = sourcePlayer.getUniqueId();
		this.targetPlayerId = targetPlayer.getUniqueId();
		this.createdAt = LocalDateTime.now();
	}
	
	private TeleportToPlayerPojo() {}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJsonObject() {
		JSONObject json = new JSONObject();
		json.put("sourcePlayerId", this.sourcePlayerId.toString());
		json.put("targetPlayerId", this.targetPlayerId.toString());
		json.put("createdAt", this.createdAt.toString());
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
		return this;
	}

	public static TeleportToPlayerPojo createFromJsonObject(JSONObject json) {
		TeleportToPlayerPojo info = new TeleportToPlayerPojo();
		return info.fromJsonObject(json);
	}

	public UUID getSourcePlayerId() { return this.sourcePlayerId;}
	public UUID getTargetPlayerId() { return this.targetPlayerId;}

	public boolean isTooOld() {
		return this.createdAt.plusSeconds(Config.V.maxAllowedTeleportDelayInSeconds).isBefore(LocalDateTime.now());
	}
}
