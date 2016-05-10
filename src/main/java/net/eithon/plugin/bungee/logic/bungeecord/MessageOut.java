package net.eithon.plugin.bungee.logic.bungeecord;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

class MessageOut {
	private ByteArrayDataOutput _out;

	MessageOut() {
		this._out = ByteStreams.newDataOutput();
	}

	MessageOut add(String... arguments) {
		if (arguments == null) return this;
		for (String argument : arguments) {
			if (argument != null) {
				this._out.writeUTF(argument);
			}
		}
		return this;
	}

	MessageOut add(byte[] byteArray) {
		short length = (short) (byteArray == null ? 0 : byteArray.length);
		this._out.writeShort(length);
		if (byteArray.length > 0) this._out.write(byteArray);
		return this;
	}
	
	byte[] toByteArray() { return this._out.toByteArray(); }
}
