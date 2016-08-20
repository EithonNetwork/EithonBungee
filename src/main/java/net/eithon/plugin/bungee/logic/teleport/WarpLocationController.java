package net.eithon.plugin.bungee.logic.teleport;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.bungee.db.WarpLocationTable;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class WarpLocationController {
	private WarpLocationTable warpLocationLogic;
	private static Object lock = new Object();
	private HashMap<String, WarpLocation> _all = new HashMap<String, WarpLocation>();

	public WarpLocationController(Database database) throws FatalException {
		this.warpLocationLogic = new WarpLocationTable(database);
	}

	public void refresh() throws FatalException, TryAgainException {
		synchronized(lock) {
			_all = readAllFromDb();
		}
	}

	private HashMap<String, WarpLocation> readAllFromDb() throws FatalException, TryAgainException {
		HashMap<String, WarpLocation> all = new HashMap<String, WarpLocation>();
		for (WarpLocation warpLocation : this.warpLocationLogic.findAll()
				.stream()
				.map(row -> WarpLocation.createFromRow(row))
				.collect(Collectors.toList())) {
			all.put(warpLocation.getName(), warpLocation);
		}
		return all;
	}

	public WarpLocation getByName(String name) throws FatalException, TryAgainException {
		synchronized(lock) {
			return this._all.get(name);
		}
	}

	public List<WarpLocation> getAllWarpLocations() {
		synchronized(lock) {
			return _all.values()
					.stream()
					.collect(Collectors.toList());
		}
	}

	public List<String> getWarpNames() {
		synchronized(lock) {
			return _all.keySet()
					.stream()
					.collect(Collectors.toList());
		}
	}

	public WarpLocation getOrCreateByName(String name, String bungeeServerName, Location location) throws FatalException, TryAgainException {
		WarpLocation warpLocation = WarpLocation.createFromRow(this.warpLocationLogic.getByName(name));
		if (warpLocation == null) {
			return WarpLocation.createFromRow(this.warpLocationLogic.getByNameOrCreate(name, bungeeServerName, 
					WarpLocation.locationToString(location)));
		} else {
			warpLocation.setBungeeServerName(bungeeServerName);
			warpLocation.setLocation(location);
			this.warpLocationLogic.update(warpLocation.toRow());
		}
		return warpLocation;
	}

	public WarpLocation getByONameOrInformSender(CommandSender sender, String name) throws FatalException, TryAgainException {
		WarpLocation warpLocation = getByName(name);
		if (warpLocation != null) return warpLocation;
		if (sender != null) sender.sendMessage(String.format("Could not find warp location %s on any server.", name));
		return null;
	}
}
