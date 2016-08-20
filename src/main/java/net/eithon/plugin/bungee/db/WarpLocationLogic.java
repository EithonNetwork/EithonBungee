package net.eithon.plugin.bungee.db;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbLogic;

public class WarpLocationLogic extends DbLogic<WarpLocationPojo> {
	public WarpLocationLogic(Database database)
			throws FatalException {
		super(WarpLocationPojo.class, database);
		// TODO Auto-generated constructor stub
	}

	public WarpLocationPojo getByNameOrCreate(String name, String bungeeServerName, String location) throws FatalException, TryAgainException {
		WarpLocationPojo row = getByName(name);
		if (row != null) return row;
		return create(name, bungeeServerName, location);
	}

	public WarpLocationPojo create(String name, String bungeeServerName, String location) throws FatalException, TryAgainException {
		WarpLocationPojo row = new WarpLocationPojo();
		row.name = name;
		row.bungee_server_name = bungeeServerName;
		row.location = location;
		long id = this.jDapper.createOne(row);
		return get(id);
	}

	public WarpLocationPojo getByName(String name) throws FatalException, TryAgainException {
		return this.jDapper.readTheOnlyOneWhere("name=?", name);
	}
}
