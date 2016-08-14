package net.eithon.plugin.bungee.logic.bungeecord;

// https://bukkit.org/threads/how-to-unit-test-your-plugin-with-example-project.23569/

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestInOutMessage {
	@Test
	public void twoStrings() {
		MessageOut out = new MessageOut();
		String string1 = "Hello";
		String string2 = "World";
		out.add(string1, string2);
		MessageIn in = new MessageIn(out.toByteArray());
		String part = in.readString();
		assertEquals(string1, part);
		part = in.readString();
		assertEquals(string2, part);
	}
	
	@Test
	public void nestedMessage() {
		MessageOut innerMessageOut = new MessageOut();
		String string1 = "Hello";
		String string2 = "World";
		innerMessageOut.add(string1, string2);
		MessageOut outerMessageOut = new MessageOut();
		String string3 = "Title";
		outerMessageOut.add(string3);
		outerMessageOut.add(innerMessageOut.toByteArray());
		
		MessageIn outerMessageIn = new MessageIn(outerMessageOut.toByteArray());
		String part = outerMessageIn.readString();
		assertEquals(string3, part);
		MessageIn innerMessageIn = new MessageIn(outerMessageIn.readByteArray());
		part = innerMessageIn.readString();
		assertEquals(string1, part);
		part = innerMessageIn.readString();
		assertEquals(string2, part);
	}
}
