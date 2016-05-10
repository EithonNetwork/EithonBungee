package net.eithon.plugin.bungee.logic.bungeecord;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.title.Title;
import net.eithon.plugin.eithonlibrary.Config;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

class BungeeListener implements PluginMessageListener {

	private EithonPlugin _eithonPlugin;
	private BungeeController _controller;

	public BungeeListener(EithonPlugin eithonPlugin, BungeeController controller) {
		this._eithonPlugin = eithonPlugin;
		this._controller = controller;
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		verbose("onPluginMessageReceived", "Enter: channel=%s, player=%s, message=%s",
				channel, player == null ? "NULL" : player.getName(), message.toString());

		if (!channel.equals("BungeeCord")) {
			verbose("onPluginMessageReceived", String.format("Unknown channel: %s", channel));			
			return;
		}
		
		MessageIn msgIn = new MessageIn(message);
		String subchannel = msgIn.readString();
		verbose("onPluginMessageReceived", String.format("subchannel=%s", subchannel));
		if (subchannel.equals("GetServer")) {
			String serverName = msgIn.readString();
			getServer(serverName);
		} else if (subchannel.equals("EithonLibraryForward")) {
			MessageIn body = new MessageIn(msgIn.readByteArray()); 
			eithonLibraryForward(body);
		} else {
			verbose("onPluginMessageReceived", "Unknown subchannel: %s", subchannel);			
		}
		
		verbose("onPluginMessageReceived", "Leave");
	}

	private void getServer(String serverName) {
		verbose("getServer", "Enter, serverName=%s", serverName);
		this._controller.setServerName(serverName);
		verbose("getServer", "Leave");
	}

	private void eithonLibraryForward(MessageIn message) {
		verbose("eithonLibraryForward", "Enter");
		String header = message.readString();
		verbose("eithonLibraryForward", String.format("forwardHeader=%s", header));
		ForwardHeader forwardHeader = ForwardHeader.getFromJsonString(header);
		if (forwardHeader.isTooOld()) {
			verbose("eithonLibraryForward", "Message was too old, Leave");
			return;
		}
		verbose("eithonLibraryForward", "%s", forwardHeader.investigateTime());
		String body = message.readString();
		verbose("eithonLibraryForward", String.format("jsonObject=%s", body));

		String commandName = forwardHeader.getCommandName();
		verbose("eithonLibraryForward", String.format("commandName=%s", commandName));
		if (commandName.equals("JoinEvent")) {
			JoinQuitInfo info = JoinQuitInfo.getFromJsonString(body);
			joinEvent(forwardHeader, info);
		} else if (commandName.equals("QuitEvent")) {
			JoinQuitInfo info = JoinQuitInfo.getFromJsonString(body);
			quitEvent(forwardHeader, info);
		} else if (commandName.equals("CallEvent")) {
			EithonBungeeEvent info = EithonBungeeEvent.getFromJsonString(body);
			verbose("eithonLibraryForward", "Calling EithonBungeeEvent %s (%s)", info.getName(), info.getData().toJSONString());
			this._eithonPlugin.getServer().getPluginManager().callEvent(info);
		} else if (commandName.equals("BroadcastMessage")) {
			MessageInfo info = MessageInfo.getFromJsonString(body);
			broadcastMessage(forwardHeader, info);
		} else {
			this._eithonPlugin.getEithonLogger().error("Unknown commandName: %s", commandName);			
		}
		verbose("onPluginMessageReceived", "Leave");
	}

	private void joinEvent(ForwardHeader forwardHeader, JoinQuitInfo info) {
		verbose("joinEvent", "Enter");
		String thatServerName = info.getServerName();
		EithonPlayer player = new EithonPlayer(info.getPlayerId());
		if (player.getOfflinePlayer() == null) {
			verbose("joinEvent", "No user found, Leave");
			return;				
		}
		String playerName = info.getPlayerName();
		player.setNameIfHasNone(playerName);
		String mainGroup = info.getMainGroup();
		String thisServerName = this._controller.getBungeeServerName();
		EithonBungeeJoinEvent e = new EithonBungeeJoinEvent(thisServerName, thatServerName, player, mainGroup);
		Bukkit.getServer().getPluginManager().callEvent(e);
		verbose("joinEvent", "Leave");
	}

	private void quitEvent(ForwardHeader forwardHeader, JoinQuitInfo info) {
		verbose("quitEvent", "Enter");
		String thatServerName = info.getServerName();
		EithonPlayer player = new EithonPlayer(info.getPlayerId());
		if (player.getOfflinePlayer() == null) {
			verbose("quitEvent", "No user found, Leave");
			return;				
		}
		String playerName = info.getPlayerName();
		player.setNameIfHasNone(playerName);
		String mainGroup = info.getMainGroup();
		String thisServerName = this._controller.getBungeeServerName();
		EithonBungeeQuitEvent e = new EithonBungeeQuitEvent(thisServerName, thatServerName, player, mainGroup);
		Bukkit.getServer().getPluginManager().callEvent(e);
		verbose("quitEvent", "Leave");
	}

	private void broadcastMessage(ForwardHeader forwardHeader, MessageInfo info) {
		String message = info.getMessage();
		verbose("broadcastMessage", String.format("Enter, message=%s", message));
		if (info.getUseTitle()) {
			sendTitle(message);
		} else {
			this._eithonPlugin.getServer().broadcastMessage(message);
		}
	}

	private void sendTitle(String message) {
		String[] lines = message.split("\\n");
		String title = lines[0];
		String subTitle = lines.length > 1 ? lines[1] : "";
		String actionBar = lines.length > 2 ? lines[2] : "";
		if (!title.equals("") || !subTitle.equals("")) Title.get().sendFloatingText(null, title, subTitle, Config.V.titleFadeInTicks, Config.V.titleStayTicks, Config.V.titleFadeOutTicks);
		if (!actionBar.equals("")) Title.get().sendActionbarMessage(null, actionBar);
	}

	private void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "BungeeListener.%s: %s", method, message);
	}
}
