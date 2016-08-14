package net.eithon.plugin.bungee.db;

import java.sql.Timestamp;

import net.eithon.library.mysql.Table;
import net.eithon.library.time.TimeMisc;

public class ServerBanPojo extends Table {
	public ServerBanPojo() {
		super("server_ban");
		// TODO Auto-generated constructor stub
	}
	public String bungee_server_name;
	public String player_id;
	public String player_name;
	public Timestamp unban_at;

	@Override
	public String toString() {
		String result = String.format("%s@%s until %s", 
				this.player_name, this.bungee_server_name, TimeMisc.fromLocalDateTime(this.unban_at.toLocalDateTime()));
		return result;
	}
}
