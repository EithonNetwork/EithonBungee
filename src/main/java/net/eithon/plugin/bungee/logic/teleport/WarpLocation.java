package net.eithon.plugin.bungee.logic.teleport;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.eithon.library.extensions.EithonLocation;
import net.eithon.plugin.bungee.Config;
import net.eithon.plugin.bungee.db.DbWarpLocation;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WarpLocation {
	private DbWarpLocation warpLocation;
	private Location location;
	private static Object lock = new Object();
	private static List<WarpLocation> _all = new ArrayList<WarpLocation>();

	public static void refresh() {
		synchronized(lock) {
			_all = readAllFromDb();
		}
	}

	private static List<WarpLocation> readAllFromDb() {
		return DbWarpLocation.findAll(Config.V.database).stream().map(dbPlayer -> new WarpLocation(dbPlayer)).collect(Collectors.toList());
	}

	private WarpLocation(DbWarpLocation warpLocation) {
		this.warpLocation = warpLocation;
	}

	public static WarpLocation getByName(String name) {
		DbWarpLocation warpLocation = DbWarpLocation.getByName(Config.V.database, name);
		if (warpLocation == null) return null;
		return new WarpLocation(warpLocation);
	}

	public static List<WarpLocation> getAllWarpLocations() {
		synchronized(lock) {
			return _all;
		}
	}

	public static WarpLocation getOrCreateByName(String name, String bungeeServerName, Location location) {
		DbWarpLocation dbWarpLocation = DbWarpLocation.getByName(Config.V.database, name);
		if (dbWarpLocation == null) {
			dbWarpLocation = DbWarpLocation.create(Config.V.database, name, bungeeServerName, 
					locationToString(location));
		} else {
			dbWarpLocation.update(bungeeServerName, locationToString(location));
		}
		return new WarpLocation(dbWarpLocation);
	}

	private static String locationToString(Location location) {
		return location == null ? null : new EithonLocation(location).toJsonString();
	}

	public static WarpLocation getByONameOrInformSender(CommandSender sender, String name) {
		WarpLocation warpLocation = getByName(name);
		if (warpLocation != null) return warpLocation;
		if (sender != null) sender.sendMessage(String.format("Could not find warp location %s on any server.", name));
		return null;
	}

	public String getName() { return this.warpLocation.getName(); }
	public String getBungeeServerName() { return this.warpLocation.getBungeeServerName(); }
	public Location getLocation() { 
		if (this.location != null) return this.location;

		JSONParser parser = new JSONParser();
		EithonLocation eithonLocation = null;
		try {
			JSONObject jsonObject = (JSONObject) parser.parse(this.warpLocation.getLocation()); 
			eithonLocation = EithonLocation.getFromJson(jsonObject);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (eithonLocation != null) this.location = eithonLocation.getLocation();
		return this.location;
	}
}
