package com.pixeldoctrine.hussh.util;

public class ElementStyle {

	public ElementStyle() {
		isNegativeMode = false;
		reset(true);
	}

	void reset(boolean b) {
		reset = b;
		if (reset) {
			bold = italic = underline = conceal = blink = bgColorSet = false;
			fgColor = new Color("#808080");
			fgColID = 0;
			bgColID = -1;
		}
	}

	public void setImageMode(boolean negative){
		if (negative != isNegativeMode){
			Color oldBg = bgColor;
			bgColor = fgColor;
			fgColor = oldBg;
			isNegativeMode=!isNegativeMode;
		}
	}

	public Color fgColor = new Color("#808080");
	public Color bgColor = new Color("#000000");
	public boolean bold, italic, underline, blink;
	public boolean reset;
	public boolean isNegativeMode, conceal;
	public boolean bgColorSet;
	public int fgColID;
	public int bgColID;
}
