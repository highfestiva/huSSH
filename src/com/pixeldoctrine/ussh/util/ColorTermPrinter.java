package com.pixeldoctrine.ussh.util;

import android.annotation.SuppressLint;


public class ColorTermPrinter {

	private ElementStyle elementStyle;
	private ColorTerminal term;

	private static final Color colorBlack			= new Color("#000000");
	private static final Color colorRed				= new Color("#F00000");
	private static final Color colorGreen			= new Color("#00F000");
	private static final Color colorYellow			= new Color("#F0F000");
	private static final Color colorBlue			= new Color("#0000F0");
	private static final Color colorMagenta  		= new Color("#F000F0");
	private static final Color colorCyan			= new Color("#00F0F0");
	private static final Color colorWhite			= new Color("#F0F0F0");
	private static final Color colorBrightRed		= new Color("#ff0000");
	private static final Color colorBrightGreen		= new Color("#00ff00");
	private static final Color colorBrightYellow	= new Color("#ffff00");
	private static final Color colorBrightBlue		= new Color("#0000ff");
	private static final Color colorBrightMagenta	= new Color("#ff00ff");
	private static final Color colorBrightCyan		= new Color("#00ffff");
	private static final Color colorBrightWhite		= new Color("#ffffff");

	private static final int[][] basic16 = {
		{ 0x00, 0x00, 0x00 }, // 0
		{ 0xCD, 0x00, 0x00 }, // 1
		{ 0x00, 0xCD, 0x00 }, // 2
		{ 0xCD, 0xCD, 0x00 }, // 3
		{ 0x00, 0x00, 0xEE }, // 4
		{ 0xCD, 0x00, 0xCD }, // 5
		{ 0x00, 0xCD, 0xCD }, // 6
		{ 0xE5, 0xE5, 0xE5 }, // 7
		{ 0x7F, 0x7F, 0x7F }, // 8
		{ 0xFF, 0x00, 0x00 }, // 9
		{ 0x00, 0xFF, 0x00 }, // 10
		{ 0xFF, 0xFF, 0x00 }, // 11
		{ 0x5C, 0x5C, 0xFF }, // 12
		{ 0xFF, 0x00, 0xFF }, // 13
		{ 0x00, 0xFF, 0xFF }, // 14
		{ 0xFF, 0xFF, 0xFF }  // 15
	};

	private static final int[] valuerange = { 0x00, 0x5F, 0x87, 0xAF, 0xD7, 0xFF };

	public void print(String colorTermText, ColorTerminal term) {
		this.term = term;
		elementStyle = new ElementStyle();
		String[] lines = colorTermText.split("\n");
		for (int i = 0; i < lines.length; ++i) {
			process(lines[i]);
			if (i+1 < lines.length) {
				term.rawPrint("\n");
			}
		}
	}

	private void process(String line) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		int seqEnd = -1;
		while (i < line.length()) {
			if (line.charAt(i) == 0x1b) {
				if (line.length() > i+2 && line.charAt(i+2) == 0x4b) {
					seqEnd = i+2;
				} else {
					seqEnd = line.indexOf('m', i+1);
					if (seqEnd == -1) {
						if (line.charAt(i+1) == ']') {
							seqEnd = line.indexOf(0x07, i+1);
						}
					}
					if (seqEnd == -1) seqEnd = line.indexOf(';', i+1);
					if (seqEnd == -1) seqEnd = line.indexOf('h', i+1);
	
					if (seqEnd != -1) {
						if (sb.length() != 0) {
							if (!elementStyle.reset) {
								term.rawPrint(sb.toString(), elementStyle);
							} else {
								term.rawPrint(sb.toString());
							}
							sb = new StringBuilder();
						}
						parseSequence(line, i, seqEnd);
					}
				}
				i = 1 + ((seqEnd!=-1)? seqEnd : i);
			} else {
				sb.append(maskCharacter(line.charAt(i)));
				++i;
			}
		}
		if (sb.length() != 0) {
			if (!elementStyle.reset) {
				term.rawPrint(sb.toString(), elementStyle);
			} else {
				term.rawPrint(sb.toString());
			}
		}
	}

	@SuppressLint("NewApi")
	private boolean parseSequence(String line, int begin, int end) {
		if (end-begin < 2 || line.charAt(begin) != 0x1b || line.charAt(begin+1) != 0x5b) {
			return false;
		}
		int colorCode;
		int[] colorValues = { 0,0,0 };
		String colorString;

		String codes = line.substring(begin+2, end);
		String[] codeVector = codes.split(";");
		if (codes.isEmpty()) {
			elementStyle.reset(true);
			return true;
		}
		for (int i = 0; i < codeVector.length; ++i) {
			String s = codeVector[i];
			int ansiCode = str2Num(s);
			elementStyle.reset(false);

			switch (ansiCode){
			case 0:
				elementStyle.reset(true);
				break;
			case 1:
				elementStyle.bold = true;
				break;
			case 2: //Faint
				break;

			case 3:
				elementStyle.italic = true;
				break;

			case 5: //Blink
			case 6: //Blink fast
				elementStyle.blink = true;
				break;

			case 8:
				elementStyle.conceal = true;
				break;

			case 4:// Underline Single
			case 21: // Underline double
				elementStyle.underline = true;
				break;

			case 7:
				elementStyle.setImageMode(true);
				break;

			case 22:
				elementStyle.bold = false;
				break;

			case 24:
				elementStyle.underline = false;
				break;

			case 25:
				elementStyle.blink = false;
				break;

			case 27:
				elementStyle.setImageMode(false);
				break;

			case 28:
				elementStyle.conceal = false;
				break;

			case 30:
				elementStyle.fgColor = colorBlack;
				break;
			case 31:
				elementStyle.fgColor = colorRed;
				break;
			case 32:
				elementStyle.fgColor = colorGreen;
				break;
			case 33:
				elementStyle.fgColor = colorYellow;
				break;
			case 34:
				elementStyle.fgColor = colorBlue;
				break;
			case 35:
				elementStyle.fgColor = colorMagenta;
				break;
			case 36:
				elementStyle.fgColor = colorCyan;
				break;
			case 37:
				elementStyle.fgColor = colorWhite;
				break;
			case 38: // xterm 256 foreground color mode \033[38;5;<color>
				if (++i >= codeVector.length) break;
				if(codeVector[i] != "5") break;
				if (++i == codeVector.length) break;
	
				colorCode = str2Num(codeVector[i]);
				xterm2rgb(colorCode, colorValues);
				colorString = String.format("#%02x%02x%02x", colorValues[0], colorValues[1], colorValues[2]);
				elementStyle.fgColor = new Color(colorString);
				break;

			case 39:
				elementStyle.reset(true);
				break;

			case 40:
				elementStyle.bgColor = colorBlack;
				break;
			case 41:
				elementStyle.bgColor = colorRed;
				break;
			case 42:
				elementStyle.bgColor = colorGreen;
				break;
			case 43:
				elementStyle.bgColor = colorYellow;
				break;
			case 44:
				elementStyle.bgColor = colorBlue;
				break;
			case 45:
				elementStyle.bgColor = colorMagenta;
				break;
			case 46:
				elementStyle.bgColor = colorCyan;
				break;
			case 47:
				elementStyle.bgColor = colorWhite;
				break;

			case 48:  // xterm 256 background color mode \033[48;5;<color>
				if (++i == codeVector.length) break;
				if (codeVector[i] != "5") break;
				if (++i == codeVector.length) break;

				colorCode = str2Num(codeVector[i]);
				xterm2rgb(colorCode, colorValues);
				colorString = String.format("#%02x%02x%02x", colorValues[0], colorValues[1], colorValues[2]);
				elementStyle.bgColor = new Color(colorString);
				break;

			case 49:
				elementStyle.reset(true);
				break;
			
			// aixterm codes
			case 90:
				elementStyle.fgColor = colorBlack;
				break;
			case 91:
				elementStyle.fgColor = colorBrightRed;
				break;
			case 92:
				elementStyle.fgColor = colorBrightGreen;
				break;
			case 93:
				elementStyle.fgColor = colorBrightYellow;
				break;
			case 94:
				elementStyle.fgColor = colorBrightBlue;
				break;
			case 95:
				elementStyle.fgColor = colorBrightMagenta;
				break;
			case 96:
				elementStyle.fgColor = colorBrightCyan;
				break;
			case 97:
				elementStyle.fgColor = colorBrightWhite;
				break;
			case 100:
				elementStyle.bgColor = colorBlack;
				break;
			case 101:
				elementStyle.bgColor = colorBrightRed;
				break;
			case 102:
				elementStyle.bgColor = colorBrightGreen;
				break;
			case 103:
				elementStyle.bgColor = colorBrightYellow;
				break;
			case 104:
				elementStyle.bgColor = colorBrightBlue;
				break;
			case 105:
				elementStyle.bgColor = colorBrightMagenta;
				break;
			case 106:
				elementStyle.bgColor = colorBrightCyan;
				break;
			case 107:
				elementStyle.bgColor = colorBrightWhite;
				break;
			}

			// Set RTF color index
			// 8 default colors followed by bright colors see rtfgenerator.cpp
			if (ansiCode>=30 && ansiCode <40)
				elementStyle.fgColID = ansiCode-30;
			else if (ansiCode>=90 && ansiCode <98)
				elementStyle.fgColID = ansiCode-90+8;
			else if (ansiCode>=40 && ansiCode <50)
				elementStyle.bgColID = ansiCode-40;
			else if (ansiCode>=100 && ansiCode <108)
				elementStyle.bgColID = ansiCode-100+8;
		}

		return true;
	}

	private static String maskCharacter(char c) {
		switch (c) {
			case '<' :
				return "&lt;";
			case '>' :
				return "&gt;";
			case '&' :
				return "&amp;";
			case '\"' :
				return "&quot;";
			case '\t' : // see deletion of non-printable chars below
				return "\t";
			default :
				if (c > 0x1f) {	// Printable?
					return String.valueOf(c);
				} else {
					return "";
				}
		}
	}

	/*
	 * From Wolfgang Frischs xterm256 converter utility:
	 * http://frexx.de/xterm-256-notes/
	 */
	private static void xterm2rgb(int color, int[] rgb) {
		// 16 basic colors
		if(color < 16) {
			rgb[0] = basic16[color][0];
			rgb[1] = basic16[color][1];
			rgb[2] = basic16[color][2];
		}
		// color cube color
		if(color>=16 && color<=232) {
			color-=16;
			rgb[0] = valuerange[(color/36)%6];
			rgb[1] = valuerange[(color/6)%6];
			rgb[2] = valuerange[color%6];
		}
		// gray tone
		if(color>232 && color<255) {
			rgb[0] = rgb[1] = rgb[2] = 8+(color-232)*0x0a;
		}
	}

	private static int str2Num(String s) {
		int i = 0;
		for (; i < s.length(); ++i) {
			if (!Character.isLetterOrDigit(s.charAt(i))) {
				break;
			}
		}
		if (i == 0) {
			return 0;
		}
		return Integer.valueOf(s.substring(0, i));
	}
}
