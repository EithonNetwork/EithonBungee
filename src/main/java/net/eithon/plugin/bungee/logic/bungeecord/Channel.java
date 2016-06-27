package net.eithon.plugin.bungee.logic.bungeecord;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.time.TimeMisc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import com.google.common.collect.Iterables;

class Channel {
	private EithonPlugin _eithonPlugin;

	public Channel(EithonPlugin eithonPlugin) {
		this._eithonPlugin = eithonPlugin;
	}

	boolean send(String subChannel) {
		return send(subChannel, (MessageOut) null, (String[]) null);
	}

	boolean send(String subChannel, String... arguments) {
		return send(subChannel, (MessageOut) null, arguments);
	}

	boolean send(String subChannel, MessageOut msgOut, String... arguments) {
		Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
		if (player == null) {
			this._eithonPlugin.logWarn(
					"No player found. Postponed until a player joins the server.");
			retryInBackgroundUntilServerHasPlayer(subChannel, msgOut, arguments);
			return false;
		}
		boolean success = send(player, subChannel, msgOut, arguments);
		return success;
	}

	void retryInBackgroundUntilServerHasPlayer(String subChannel,
			MessageOut msgOut, String... arguments) {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
				if (player != null) send(player, subChannel, msgOut, arguments);
				else retryInBackgroundUntilServerHasPlayer(subChannel, msgOut, arguments);
			}
		}, TimeMisc.secondsToTicks(1));
	}

	boolean send(Player player, String subChannel, String... arguments) {
		return send(player, subChannel, (MessageOut) null, arguments);
	}

	boolean send(Player player, String subChannel, MessageOut message, String... arguments) {
		if (player == null) {
			boolean success = send(subChannel, message, arguments);
			return success;
		}
		MessageOut messageOut = new MessageOut();
		messageOut.add(subChannel);
		messageOut.add(arguments);
		if (message != null) messageOut.add(message.toByteArray());
		sendSync(this._eithonPlugin, player, messageOut);
		return true;
	}

	private void sendSync(Plugin plugin, Player player, MessageOut messageOut) {
		final BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				player.sendPluginMessage(plugin, "BungeeCord", messageOut.toByteArray());
			}
		};
		runnable.runTask(this._eithonPlugin);
	}
}
