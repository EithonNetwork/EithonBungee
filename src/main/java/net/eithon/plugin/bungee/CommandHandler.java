package net.eithon.plugin.bungee;

import net.eithon.library.command.CommandSyntaxException;
import net.eithon.library.command.EithonCommand;
import net.eithon.library.command.ICommandSyntax;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.bungee.logic.Controller;
import net.eithon.plugin.stats.logic.TryHandler;

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
			commandSyntax
			.parseCommandSyntax("refresh")
			.setCommandExecutor(p -> refreshCommand(p));
			setupTpCommand(commandSyntax);
			setupServerCommand(commandSyntax);
			setupMessageCommand(commandSyntax);
			setupWarpCommand(commandSyntax);
			setupBanCommand(commandSyntax);
			this._commandSyntax = commandSyntax;
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
	}

	ICommandSyntax getCommandSyntax() { return this._commandSyntax; }

	private void setupServerCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		ICommandSyntax cmd = commandSyntax
				.parseCommandSyntax("server <name>")
				.setCommandExecutor(p -> serverCommand(p));
		cmd
		.getParameterSyntax("name")
		.setMandatoryValues(ec -> Config.V.bungeeServerNames);
	}

	private void setupTpCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {

		// tpto request <player>
		ICommandSyntax cmd = commandSyntax.parseCommandSyntax("tpto request <player>")
				.setCommandExecutor(eithonCommand -> requestTpToPlayer(eithonCommand));
		acceptAllBungeePlayerNames(cmd);

		// tpto force <player>
		cmd = commandSyntax.parseCommandSyntax("tpto force <player>")
				.setCommandExecutor(eithonCommand -> forcedTpToPlayer(eithonCommand));
		acceptAllBungeePlayerNames(cmd);

		// tphere request <player>
		cmd = commandSyntax.parseCommandSyntax("tphere request <player>")
				.setCommandExecutor(eithonCommand -> requestTpPlayerHere(eithonCommand));
		acceptAllBungeePlayerNames(cmd);

		// tphere force <player>
		cmd = commandSyntax.parseCommandSyntax("tphere force <player>")
				.setCommandExecutor(eithonCommand -> forcedTpPlayerHere(eithonCommand));
		acceptAllBungeePlayerNames(cmd);

		// deny
		cmd = commandSyntax.parseCommandSyntax("tp deny")
				.setCommandExecutor(eithonCommand -> tpDeny(eithonCommand));

		// accept
		cmd = commandSyntax.parseCommandSyntax("tp accept")
				.setCommandExecutor(eithonCommand -> tpAccept(eithonCommand));
	}

	private void setupMessageCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {

		// message <player> <message>
		ICommandSyntax cmd = commandSyntax.parseCommandSyntax("message <player> <message:REST>")
				.setCommandExecutor(eithonCommand -> sendMessageToPlayer(eithonCommand));
		acceptAllBungeePlayerNames(cmd);

		// message reply <message>
		cmd = commandSyntax.parseCommandSyntax("reply <message:REST>")
				.setCommandExecutor(eithonCommand -> sendReply(eithonCommand));
	}

	private void setupWarpCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {

		// message <player> <message>
		ICommandSyntax cmd = commandSyntax.parseCommandSyntax("warp add <name>")
				.setCommandExecutor(eithonCommand -> warpAdd(eithonCommand));

		// message reply <message>
		cmd = commandSyntax.parseCommandSyntax("warp to <name>")
				.setCommandExecutor(eithonCommand -> warpTo(eithonCommand));
		cmd.
		getParameterSyntax("name")
		.setMandatoryValues(ec -> this._controller.getWarpNames());
	}

	private void setupBanCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {

		// message <player> <message>
		ICommandSyntax cmd = commandSyntax.parseCommandSyntax("ban add <player> <server> <time-span : TIME_SPAN  {24h, 48h, 72h, ...}>")
				.setCommandExecutor(eithonCommand -> banAdd(eithonCommand));
		acceptAllBungeePlayerNames(cmd);
		cmd
		.getParameterSyntax("server")
		.setMandatoryValues(ec -> Config.V.bungeeServerNames);

		// message reply <message>
		cmd = commandSyntax.parseCommandSyntax("ban remove <player> <server>")
				.setCommandExecutor(eithonCommand -> banRemove(eithonCommand));
		cmd
		.getParameterSyntax("server")
		.setMandatoryValues(ec -> Config.V.bungeeServerNames);

		// message reply <message>
		cmd = commandSyntax.parseCommandSyntax("ban list")
				.setCommandExecutor(eithonCommand -> banList(eithonCommand));
	}

	private void acceptAllBungeePlayerNames(ICommandSyntax cmd) {
		if (Config.V.mandatoryPlayerNames) {
			cmd
			.getParameterSyntax("player")
			.setMandatoryValues(ec -> this._controller.getBungeePlayerNames(ec));
		} else {
			cmd
			.getParameterSyntax("player")
			.setExampleValues(ec -> this._controller.getBungeePlayerNames(ec));
		}
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
		boolean success = this._controller.requestTpToPlayer(movingPlayer, anchorPlayer);
		if (!success) return;
		movingPlayer.sendMessage(String.format("Request sent to player %s", anchorPlayer.getName()));
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
		boolean success = this._controller.requestTpPlayerHere(anchorPlayer, movingPlayer);
		if (!success) return;
		anchorPlayer.sendMessage(String.format("Request sent to player %s", movingPlayer.getName()));
	}

	private void tpDeny(EithonCommand eithonCommand)
	{
		Player player = eithonCommand.getPlayerOrInformSender();
		if (player == null) return;
		this._controller.tpDeny(player);
		player.sendMessage("You have denied the teleportation request.");
	}

	private void tpAccept(EithonCommand eithonCommand)
	{
		Player player = eithonCommand.getPlayerOrInformSender();
		if (player == null) return;
		this._controller.tpAccept(player);
		player.sendMessage("You have accepted the teleportation request.");

	}

	private void sendMessageToPlayer(EithonCommand eithonCommand) {
		Player sender = eithonCommand.getPlayerOrInformSender();
		if (sender == null) return;
		OfflinePlayer receiver = eithonCommand.getArgument("player").asOfflinePlayer();
		if (receiver == null) {
			throw new NotImplementedException();
		}
		String message = eithonCommand.getArgument("message").asString();
		boolean success = TryHandler.handleExceptions(sender, () -> {
					return this._controller.sendMessageToPlayer(sender, receiver, message);
				});
		if (!success) return;
		Config.M.messageSent.sendMessage(sender, receiver.getName(), message);
	}

	private void sendReply(EithonCommand eithonCommand) {
		Player sender = eithonCommand.getPlayerOrInformSender();
		if (sender == null) return;
		String message = eithonCommand.getArgument("message").asString();
		String receiverName = TryHandler.handleExceptions(sender, () -> {
			return this._controller.replyMessageToPlayer(sender, message);
		});
		if (receiverName == null) return;
		Config.M.messageSent.sendMessage(sender, receiverName, message);
	}

	private void warpAdd(EithonCommand eithonCommand) {
		Player player = eithonCommand.getPlayerOrInformSender();
		if (player == null) return;
		String name = eithonCommand.getArgument("name").asString();
		boolean success = this._controller.warpAdd(player, name, player.getLocation());
		if (!success) return;
		Config.M.warpAdded.sendMessage(player, name);
	}

	private void warpTo(EithonCommand eithonCommand) {
		Player player = eithonCommand.getPlayerOrInformSender();
		if (player == null) return;
		String name = eithonCommand.getArgument("name").asString();
		this._controller.warpTo(player, player, name);
	}

	private void banAdd(EithonCommand eithonCommand) {
		final CommandSender sender = eithonCommand.getSender();
		final OfflinePlayer player = eithonCommand.getArgument("player").asOfflinePlayer();
		final String serverName = eithonCommand.getArgument("server").asString();
		final long seconds = eithonCommand.getArgument("time-span").asSeconds();
		this._controller.banAddAsync(sender, player, serverName, seconds);
	}

	private void banRemove(EithonCommand eithonCommand) {
		CommandSender sender = eithonCommand.getSender();
		OfflinePlayer player = eithonCommand.getArgument("player").asOfflinePlayer();
		final String serverName = eithonCommand.getArgument("server").asString();
		this._controller.banRemoveAsync(sender, player, serverName);
	}

	private void banList(EithonCommand eithonCommand) {
		CommandSender sender = eithonCommand.getSender();
		this._controller.banListAsync(sender);
	}

	private void refreshCommand(EithonCommand command)
	{
		CommandSender sender = command.getSender();
		this._controller.refreshBungeePlayer();
		this._controller.refreshWarpLocations();
		sender.sendMessage("Started refresh of bungee players and warp locations.");
	}

	private void serverCommand(EithonCommand command)
	{
		String serverName = command.getArgument("name").asString();
		Player player = command.getPlayer();
		boolean success = this._controller.connectPlayerToServerOrInformSender(player, player, serverName);
		if (!success) return;
		Config.M.connectedToServer.sendMessage(player, serverName);
	}
}
