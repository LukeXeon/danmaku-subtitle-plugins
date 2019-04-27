package org.kexie.android.danmakux.format;


import android.support.annotation.NonNull;

public class Style {

	private static int styleCounter;

	/**
	 * Constructor that receives a String to use a its identifier
	 *
	 * @param styleName = identifier of this style
	 */
	Style(String styleName) {
		this.iD = styleName;
	}

	/**
	 * Constructor that receives a String with the new styleName and a style to copy
	 *
	 * @param styleName
	 * @param style
	 */
	Style(String styleName, Style style) {
		this.iD = styleName;
		if (style == null) {
			return;
		}
		this.font = style.font;
		this.fontSize = style.fontSize;
		this.color = style.color;
		this.backgroundColor = style.backgroundColor;
		this.textAlign = style.textAlign;
		this.italic = style.italic;
		this.underline = style.underline;
		this.bold = style.bold;
	}

	/* ATTRIBUTES */
	public String iD;
	public String font;
	public String fontSize;
	/**
	 * colors are stored as 8 chars long RGBA
	 */
	public String color;
	public String backgroundColor;
	public String textAlign = "";

	public boolean italic;
	public boolean bold;
	public boolean underline;

	/* METHODS */

	/**
	 * To get the string containing the hex value to put into color or background color
	 *
	 * @param format supported: "name", "&HBBGGRR", "&HAABBGGRR", "decimalCodedBBGGRR", "decimalCodedAABBGGRR"
	 * @param value  RRGGBBAA string
	 * @return
	 */
	public static String getRGBValue(String format, String value) {
		StringBuilder color = null;
		if (format.equalsIgnoreCase("name")) {
			//standard color format from W3C
			switch (value) {
				case "transparent":
					color = new StringBuilder("00000000");
					break;
				case "black":
					color = new StringBuilder("000000ff");
					break;
				case "silver":
					color = new StringBuilder("c0c0c0ff");
					break;
				case "gray":
					color = new StringBuilder("808080ff");
					break;
				case "white":
					color = new StringBuilder("ffffffff");
					break;
				case "maroon":
					color = new StringBuilder("800000ff");
					break;
				case "red":
					color = new StringBuilder("ff0000ff");
					break;
				case "purple":
					color = new StringBuilder("800080ff");
					break;
				case "fuchsia":
					color = new StringBuilder("ff00ffff");
					break;
				case "magenta":
					color = new StringBuilder("ff00ffff ");
					break;
				case "green":
					color = new StringBuilder("008000ff");
					break;
				case "lime":
					color = new StringBuilder("00ff00ff");
					break;
				case "olive":
					color = new StringBuilder("808000ff");
					break;
				case "yellow":
					color = new StringBuilder("ffff00ff");
					break;
				case "navy":
					color = new StringBuilder("000080ff");
					break;
				case "blue":
					color = new StringBuilder("0000ffff");
					break;
				case "teal":
					color = new StringBuilder("008080ff");
					break;
				case "aqua":
					color = new StringBuilder("00ffffff");
					break;
				case "cyan":
					color = new StringBuilder("00ffffff ");
					break;
			}
		} else if (format.equalsIgnoreCase("&HBBGGRR")) {
			//hex format from SSA
			String sb = value.substring(6) +
					value.substring(4, 5) +
					value.substring(2, 3) +
					"ff";
			color = new StringBuilder(sb);
		} else if (format.equalsIgnoreCase("&HAABBGGRR")) {
			//hex format from ASS
			String sb = value.substring(8) +
					value.substring(6, 7) +
					value.substring(4, 5) +
					value.substring(2, 3);
			color = new StringBuilder(sb);
		} else if (format.equalsIgnoreCase("decimalCodedBBGGRR")) {
			//normal format from SSA
			color = new StringBuilder(Integer.toHexString(Integer.parseInt(value)));
			//any missing 0s are filled in
			while (color.length() < 6) color.insert(0, "0");
			//order is reversed
			color = new StringBuilder(color.substring(4) + color.substring(2, 4) + color.substring(0, 2) + "ff");
		} else if (format.equalsIgnoreCase("decimalCodedAABBGGRR")) {
			//normal format from ASS
			color = new StringBuilder(Long.toHexString(Long.parseLong(value)));
			//any missing 0s are filled in
			while (color.length() < 8) color.insert(0, "0");
			//order is reversed
			color = new StringBuilder(color.substring(6) + color.substring(4, 6) + color.substring(2, 4) + color.substring(0, 2));
		}
		return color == null ? null : color.toString();
	}

	static String defaultID() {
		return "default" + styleCounter++;
	}

	@NonNull
	@Override
	public String toString() {
		return "Style{" +
				"id='" + iD + '\'' +
				", font='" + font + '\'' +
				", fontSize='" + fontSize + '\'' +
				", color='" + color + '\'' +
				", backgroundColor='" + backgroundColor + '\'' +
				", textAlign='" + textAlign + '\'' +
				", italic=" + italic +
				", bold=" + bold +
				", underline=" + underline +
				'}';
	}
}
