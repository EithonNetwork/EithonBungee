package net.eithon.plugin.bungee;

import net.eithon.library.command.CommandSyntaxException;
import net.eithon.library.command.EithonCommand;
import net.eithon.library.command.EithonCommandUtilities;
import net.eithon.library.command.ICommandSyntax;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.CommandParser;
import net.eithon.library.plugin.ICommandHandler;
import net.eithon.plugin.bungee.logic.Controller;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class CommandHandler {
	private Controller _controller;
	private ICommandSyntax _commandSyntax;
	
	public CommandHandler(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;

		ICommandSyntax commandSyntax = EithonCommand.createRootCommand("eithonbungee");
		commandSyntax.setPermissionsAutomatically();

		try {
			setupTpCommand(commandSyntax);
			this._commandSyntax = commandSyntax;
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
	}
	
	ICommandSyntax getCommandSyntax() { return this._commandSyntax; }

	private ICommandSyntax setupTpCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		// tp <player>
		ICommandSyntax cmd = commandSyntax.parseCommandSyntax("tp <player>")
				.setCommandExecutor(eithonCommand -> forcedTpToPlayer(eithonCommand));
		cmd
		.getParameterSyntax("player")
		.setExampleValues(ec -> EithonCommandUtilities.getOnlinePlayerNames(ec));
		return cmd;
	}


	private void forcedTpToPlayer(EithonCommand eithonCommand)
	{
		Player sourcePlayer = eithonCommand.getPlayerOrInformSender();
		if (sourcePlayer == null) return;
		OfflinePlayer targetPlayer = eithonCommand.getArgument("player").asOfflinePlayer();
		if (targetPlayer == null) {
			throw new NotImplementedException();
		}
		this._controller.forcedTpToPlayer(sourcePlayer, targetPlayer);
	}
}
