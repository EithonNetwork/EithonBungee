package net.eithon.plugin.bungee.logic;

import java.util.UUID;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlugin;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

class BungeeListener implements PluginMessageListener {

	public static final String EITHON_BUNGEE_FIXES_CHANNEL = "EithonBungeeFixes";	
	private static final String PLAYER_DISCONNECTED = "PlayerDisconnected";


	private EithonPlugin _eithonPlugin;
	private Controller _controller;

	public BungeeListener(EithonPlugin eithonPlugin, Controller controller) {
		this._eithonPlugin = eithonPlugin;
		this._controller = controller;
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		verbose("onPluginMessageReceived", "Enter: channel=%s, player=%s, message=%s",
				channel, player == null ? "NULL" : player.getName(), message.toString());

		if (!channel.equals(EITHON_BUNGEE_FIXES_CHANNEL)) {
			minor("onPluginMessageReceived", String.format("Unknown channel: %s", channel));			
			return;
		}
		
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		
		String command = in.readUTF();
		if (command.equals(PLAYER_DISCONNECTED)) {
			String serverName = in.readUTF();
			String playerId = in.readUTF();
			String playerName = in.readUTF();
			this._controller.publishLeaveEventOnThisServer(serverName, UUID.fromString(playerId), playerName);
		} else {
			minor("onPluginMessageReceived", String.format("Unknown command: %s", command));			
			return;			
		}
		
		verbose("onPluginMessageReceived", "Leave");
	}

	private void minor(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.dbgMinor("BungeeListener.%s: %s", method, message);	
	}

	private void verbose(String method, String format, Object... args) {
		this._eithonPlugin.dbgVerbose("BungeeListener", method, format, args);	
	}
}
