package net.eithon.plugin.bungee.logic.bungeecord;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

class MessageIn {
	private ByteArrayDataInput _in;

	MessageIn(byte[] byteArray) {
		this._in = ByteStreams.newDataInput(byteArray);
	}

	String readString() {
		return this._in.readUTF();
	}

	short readShort() {
		return this._in.readShort();
	}

	byte[] readByteArray() {
		short length = readShort();
		byte[] msgbytes = new byte[length];
		if (length > 0) this._in.readFully(msgbytes);
		return msgbytes;
	}
}
