package net.eithon.plugin.bungee.db;

import java.sql.Timestamp;

import net.eithon.library.mysql.Table;

public class PlayerPojo extends Table {
	public PlayerPojo() {
		super("player");
	}
	public String bungee_server_name;
	public String player_id;
	public String player_name;
	public Timestamp left_at;
	
	@Override
	public String toString() {
		if (this.left_at == null) return String.format("%s@%s", this.player_name, this.bungee_server_name);
		return String.format("%s has left server %s", this.player_name, this.bungee_server_name);
	}
	
}
