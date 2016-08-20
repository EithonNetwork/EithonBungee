package net.eithon.plugin.bungee.db;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbTable;

public class WarpLocationTable extends DbTable<WarpLocationRow> {
	public WarpLocationTable(Database database)
			throws FatalException {
		super(WarpLocationRow.class, database);
		// TODO Auto-generated constructor stub
	}

	public WarpLocationRow getByNameOrCreate(String name, String bungeeServerName, String location) throws FatalException, TryAgainException {
		WarpLocationRow row = getByName(name);
		if (row != null) return row;
		return create(name, bungeeServerName, location);
	}

	public WarpLocationRow create(String name, String bungeeServerName, String location) throws FatalException, TryAgainException {
		WarpLocationRow row = new WarpLocationRow();
		row.name = name;
		row.bungee_server_name = bungeeServerName;
		row.location = location;
		long id = this.jDapper.createOne(row);
		return get(id);
	}

	public WarpLocationRow getByName(String name) throws FatalException, TryAgainException {
		return this.jDapper.readTheOnlyOneWhere("name=?", name);
	}
}
