package net.eithon.plugin.bungee.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import net.eithon.library.extensions.EithonLocation;
import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbRecord;
import net.eithon.library.mysql.IDbRecord;

import org.bukkit.Location;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DbWarpLocation extends DbRecord<DbWarpLocation> implements IDbRecord<DbWarpLocation> {
	private String name;
	private String bungeeServerName;
	private Location location;

	public static DbWarpLocation create(Database database, String name, String bungeeServerName, Location location) {
		DbWarpLocation warpLocation = getByName(database, name);
		if (warpLocation == null) {
			warpLocation = new DbWarpLocation(database, name, bungeeServerName, location);
			warpLocation.dbCreate();
		}
		return warpLocation;	
	}

	public static DbWarpLocation getByName(Database database, String name) {
		return getByWhere(database, "name=?", name);
	}

	public static List<DbWarpLocation> findAll(Database database) {
		DbWarpLocation warpLocation = new DbWarpLocation(database);
		return warpLocation.findAll();
	}

	private DbWarpLocation(Database database, String name, String bungeeServerName, Location location) {
		this(database);
		this.name = name;
		this.bungeeServerName = bungeeServerName;
		this.location = location;
	}

	private DbWarpLocation(Database database) {
		this(database, -1);
	}

	protected DbWarpLocation(Database database, long id) {
		super(database, "warp_location", id);
	}

	public String getName() { return this.name; }
	public String getBungeeServerName() { return this.bungeeServerName; }
	public Location getLocation() { return this.location; }

	@Override
	public String toString() {
		String result = String.format("%s@%s", this.name, this.bungeeServerName);
		return result;
	}

	public void updateBungeeServerName(String bungeeServerName) {
		this.bungeeServerName = bungeeServerName;
		dbUpdate();
	}

	public void updateLocation(Location location) {
		this.location = location;
		dbUpdate();
	}

	private static DbWarpLocation getByWhere(Database database, String format, Object... arguments) {
		DbWarpLocation dbPlayer = new DbWarpLocation(database);
		return dbPlayer.getByWhere(format, arguments);
	}

	@Override
	public DbWarpLocation fromDb(ResultSet resultSet) throws SQLException {
		super.fromDb(resultSet);
		this.name = resultSet.getString("name");
		this.bungeeServerName = resultSet.getString("bungee_server_name");		
		JSONParser parser = new JSONParser();
		EithonLocation eithonLocation = null;
		try {
			JSONObject jsonObject = (JSONObject) parser.parse(resultSet.getString("location")); 
			eithonLocation = EithonLocation.getFromJson(jsonObject);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.location = null;
		if (eithonLocation != null) this.location = eithonLocation.getLocation();
		return this;
	}

	@Override
	public HashMap<String, Object> getColumnValues() {
		HashMap<String, Object> columnValues = new HashMap<String, Object>();
		columnValues.put("name", this.name);
		columnValues.put("bungee_server_name", this.bungeeServerName);
		String locationAsString = null;
		if (this.location != null) locationAsString = new EithonLocation(this.location).toJsonString();
		columnValues.put("location", locationAsString);
		return columnValues;
	}

	@Override
	public DbWarpLocation factory(Database database, long id) {
		return new DbWarpLocation(database, id);
	}

	@Override
	public String getUpdatedAtColumnName() { return "updated_at"; }
}
