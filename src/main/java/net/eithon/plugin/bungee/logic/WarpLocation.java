package net.eithon.plugin.bungee.logic;

import java.util.List;
import java.util.stream.Collectors;

import net.eithon.plugin.bungee.Config;
import net.eithon.plugin.bungee.db.DbWarpLocation;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class WarpLocation {
	private DbWarpLocation warpLocation;

	private WarpLocation(DbWarpLocation warpLocation) {
		this.warpLocation = warpLocation;
	}

	public static WarpLocation getByName(String name) {
		DbWarpLocation warpLocation = DbWarpLocation.getByName(Config.V.database, name);
		if (warpLocation == null) return null;
		return new WarpLocation(warpLocation);
	}

	public static List<WarpLocation> findAll() {
		return DbWarpLocation.findAll(Config.V.database).stream().map(dbPlayer -> new WarpLocation(dbPlayer)).collect(Collectors.toList());
	}

	public static WarpLocation getOrCreateByName(String name, String bungeeServerName, Location location) {
		DbWarpLocation warpLocation = DbWarpLocation.getByName(Config.V.database, name);
		if (warpLocation == null) {
			warpLocation = DbWarpLocation.create(Config.V.database, name, bungeeServerName, location);
		}
		return new WarpLocation(warpLocation);
	}

	public static WarpLocation getByONameOrInformSender(CommandSender sender, String name) {
		WarpLocation warpLocation = getByName(name);
		if (warpLocation != null) return warpLocation;
		if (sender != null) sender.sendMessage(String.format("Could not find warp location %s on any server.", name));
		return null;
	}

	public void update(String bungeeServerName) {
		this.warpLocation.updateBungeeServerName(bungeeServerName);
	}

	public String getName() { return this.warpLocation.getName(); }
	public String getBungeeServerName() { return this.warpLocation.getBungeeServerName(); }
	public Location getLocation() { return this.warpLocation.getLocation(); }
}
