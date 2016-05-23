package net.eithon.plugin.bungee;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.bungee.logic.Controller;
import net.eithon.plugin.bungee.logic.bungeecord.BungeeController;

import org.bukkit.event.Listener;

public final class EithonBungeePlugin extends EithonPlugin {
	private Controller _controller;
	private BungeeController _bungeeController;
	private EithonBungeeApi _api;

	@Override
	public void onEnable() {
		super.onEnable();
		Config.load(this);
		this._bungeeController = new BungeeController(this);
		this._controller = new Controller(this, this._bungeeController);
		CommandHandler commandHandler = new CommandHandler(this, this._controller);
		Listener eventListener = new EventListener(this, this._controller);
		this._bungeeController.initialize();
		this._api = new EithonBungeeApi(this._bungeeController, this._controller);
		super.activate(commandHandler.getCommandSyntax(), eventListener);
	}

	@Override
	public void onDisable() {
		super.onDisable();
		this._controller = null;
	}
	
	public EithonBungeeApi getApi() {return this._api; }
}
