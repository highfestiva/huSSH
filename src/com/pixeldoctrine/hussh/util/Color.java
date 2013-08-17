package com.pixeldoctrine.hussh.util;


public class Color {
	public Color(String rgb) {
		r = g = b = 0;
		if (rgb.startsWith("#")) {
			int i = Integer.parseInt(rgb.substring(1), 16);
			r = (i >> 16) & 0xFF;
			g = (i >>  8) & 0xFF;
			b = (i >>  0) & 0xFF;
		}
	}
	public int getIntColor() {
		int col = -1 & ~0xFFFFFF;
		col |= (r<<16) | (g<<8) | b;
		return col;
	}

	int r;
	int g;
	int b;
}
