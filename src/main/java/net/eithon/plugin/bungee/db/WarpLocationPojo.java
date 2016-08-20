package net.eithon.plugin.bungee.db;

import net.eithon.library.mysql.Table;

public class WarpLocationPojo extends Table {
	public WarpLocationPojo() {
		super("warp_location");
		// TODO Auto-generated constructor stub
	}
	public String name;
	public String bungee_server_name;
	public String location;

	@Override
	public String toString() {
		String result = String.format("%s@%s", this.name, this.bungee_server_name);
		return result;
	}
}
