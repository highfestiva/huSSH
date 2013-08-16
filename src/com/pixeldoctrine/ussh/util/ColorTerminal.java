package com.pixeldoctrine.ussh.util;


public interface ColorTerminal {
	void rawPrint(String s);
	void rawPrint(String s, ElementStyle style);
}
