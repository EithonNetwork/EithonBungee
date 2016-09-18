package net.eithon.plugin.bungee.logic.teleport;

import org.bukkit.Location;

import net.eithon.library.extensions.EithonLocation;
import net.eithon.library.mysql.IRowMapper;
import net.eithon.plugin.bungee.db.WarpLocationRow;

public class WarpLocation implements IRowMapper<WarpLocation, WarpLocationRow> {
	private long id;
	private String name;
	private String bungeeServerName;
	private Location location;
	public WarpLocation factory() {
		return new WarpLocation();
	}

	public WarpLocationRow toRow() {
		WarpLocationRow row = new WarpLocationRow();
		row.id = this.id;
		row.name = this.name;
		row.bungee_server_name = this.bungeeServerName;
		row.location = locationToString(this.location);
		return row;
	}

	public WarpLocation fromRow(WarpLocationRow row) {
		this.id = row.id;
		this.name = row.name;
		this.bungeeServerName = row.bungee_server_name;
		this.location = stringToLocation(row.location);
		return this;
	}

	static String locationToString(Location location) {
		return location == null ? null : new EithonLocation(location).toJsonString();
	}

	private static Location stringToLocation(String location) {
		if (location == null) return null;
		EithonLocation eithonLocation = EithonLocation.getFromJsonString(location);
		return eithonLocation == null ? null : eithonLocation.getLocation();
	}
	
	public static WarpLocation createFromRow(WarpLocationRow row) {
		if (row == null) return null;
		return new WarpLocation().fromRow(row);
	}
	
	public long getId() { return id; }
	public String getName() { return name; }
	public String getBungeeServerName() { return bungeeServerName; }
	public Location getLocation() { return location; }

	public void setBungeeServerName(String bungeeServerName) { this.bungeeServerName = bungeeServerName; }
	public void setLocation(Location location) { this.location = location; }
}
