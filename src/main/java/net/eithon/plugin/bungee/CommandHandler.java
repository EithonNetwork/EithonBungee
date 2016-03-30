package net.eithon.plugin.bungee;

import net.eithon.library.command.CommandSyntaxException;
import net.eithon.library.command.EithonCommand;
import net.eithon.library.command.EithonCommandUtilities;
import net.eithon.library.command.ICommandSyntax;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.bungee.logic.Controller;

import org.bukkit.OfflinePlayer;
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

	private void setupTpCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		
		// tpto request <player>
		ICommandSyntax cmd = commandSyntax.parseCommandSyntax("tpto request <player>")
				.setCommandExecutor(eithonCommand -> requestTpToPlayer(eithonCommand));
		setPlayerValues(cmd);
		
		// tpto force <player>
		cmd = commandSyntax.parseCommandSyntax("tpto force <player>")
				.setCommandExecutor(eithonCommand -> forcedTpToPlayer(eithonCommand));
		setPlayerValues(cmd);

		// tphere request <player>
		cmd = commandSyntax.parseCommandSyntax("tphere request <player>")
				.setCommandExecutor(eithonCommand -> requestTpPlayerHere(eithonCommand));
		setPlayerValues(cmd);

		// tphere force <player>
		cmd = commandSyntax.parseCommandSyntax("tphere force <player>")
				.setCommandExecutor(eithonCommand -> forcedTpPlayerHere(eithonCommand));
		setPlayerValues(cmd);

		// deny
		cmd = commandSyntax.parseCommandSyntax("tp deny")
				.setCommandExecutor(eithonCommand -> tpDeny(eithonCommand));

		// accept
		cmd = commandSyntax.parseCommandSyntax("tp accept")
				.setCommandExecutor(eithonCommand -> tpAccept(eithonCommand));
	}

	private void setPlayerValues(ICommandSyntax cmd) {
		cmd
		.getParameterSyntax("player")
		.setExampleValues(ec -> EithonCommandUtilities.getOnlinePlayerNames(ec));
	}


	private void forcedTpToPlayer(EithonCommand eithonCommand)
	{
		Player movingPlayer = eithonCommand.getPlayerOrInformSender();
		if (movingPlayer == null) return;
		OfflinePlayer anchorPlayer = eithonCommand.getArgument("player").asOfflinePlayer();
		if (anchorPlayer == null) {
			throw new NotImplementedException();
		}
		this._controller.forcedTpToPlayer(movingPlayer, anchorPlayer);
	}


	private void requestTpToPlayer(EithonCommand eithonCommand)
	{
		Player movingPlayer = eithonCommand.getPlayerOrInformSender();
		if (movingPlayer == null) return;
		OfflinePlayer anchorPlayer = eithonCommand.getArgument("player").asOfflinePlayer();
		if (anchorPlayer == null) {
			throw new NotImplementedException();
		}
		this._controller.requestTpToPlayer(movingPlayer, anchorPlayer);
	}


	private void forcedTpPlayerHere(EithonCommand eithonCommand)
	{
		Player anchorPlayer = eithonCommand.getPlayerOrInformSender();
		if (anchorPlayer == null) return;
		OfflinePlayer movingPlayer = eithonCommand.getArgument("player").asOfflinePlayer();
		if (movingPlayer == null) {
			throw new NotImplementedException();
		}
		this._controller.forcedTpPlayerHere(anchorPlayer, movingPlayer);
	}

	private void requestTpPlayerHere(EithonCommand eithonCommand)
	{
		Player anchorPlayer = eithonCommand.getPlayerOrInformSender();
		if (anchorPlayer == null) return;
		OfflinePlayer movingPlayer = eithonCommand.getArgument("player").asOfflinePlayer();
		if (movingPlayer == null) {
			throw new NotImplementedException();
		}
		this._controller.requestTpPlayerHere(anchorPlayer, movingPlayer);
	}

	private void tpDeny(EithonCommand eithonCommand)
	{
		Player player = eithonCommand.getPlayerOrInformSender();
		if (player == null) return;
		this._controller.tpDeny(player);
	}

	private void tpAccept(EithonCommand eithonCommand)
	{
		Player player = eithonCommand.getPlayerOrInformSender();
		if (player == null) return;
		this._controller.tpAccept(player);
	}
}
