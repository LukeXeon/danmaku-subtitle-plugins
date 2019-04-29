package org.kexie.android.danmakux.format;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import java.util.concurrent.atomic.AtomicInteger;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class Style {

	private final static AtomicInteger styleCounter = new AtomicInteger(0);

	/**
	 * Constructor that receives a String to use a its identifier
	 *
	 * @param styleName = identifier of this style
	 */
	protected Style(String styleName) {
		this.id = styleName;
	}

	/**
	 * Constructor that receives a String with the new styleName and a style to copy
	 *
	 * @param styleName
	 * @param style
	 */
	protected Style(String styleName, Style style) {
		this.id = styleName;
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
	public String id;
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
	 * @param value  color string
	 * @return
	 */
	static String getRGBAValue(String format, String value) {
		String color = "";
		if (format.equalsIgnoreCase("name")) {
			//standard color format from W3C
			switch (value) {
				case "transparent":
					color = "00000000";
					break;
				case "black":
					color = "000000ff";
					break;
				case "silver":
					color = "c0c0c0ff";
					break;
				case "gray":
					color = "808080ff";
					break;
				case "white":
					color = "ffffffff";
					break;
				case "maroon":
					color = "800000ff";
					break;
				case "red":
					color = "ff0000ff";
					break;
				case "purple":
					color = "800080ff";
					break;
				case "fuchsia":
					color = "ff00ffff";
					break;
				case "magenta":
					color = "ff00ffff ";
					break;
				case "green":
					color = "008000ff";
					break;
				case "lime":
					color = "00ff00ff";
					break;
				case "olive":
					color = "808000ff";
					break;
				case "yellow":
					color = "ffff00ff";
					break;
				case "navy":
					color = "000080ff";
					break;
				case "blue":
					color = "0000ffff";
					break;
				case "teal":
					color = "008080ff";
					break;
				case "aqua":
					color = "00ffffff";
					break;
				case "cyan":
					color = "00ffffff ";
					break;
			}
		} else if (format.equalsIgnoreCase("&HBBGGRR")) {
			//hex format from SSA
			color = new StringBuilder(value.substring(2))
					.reverse()
					.append("ff")
					.toString();
		} else if (format.equalsIgnoreCase("&HAABBGGRR")) {
			//hex format from ASS
			color = new StringBuilder(value.substring(2))
					.reverse()
					.toString();
		} else if (format.equalsIgnoreCase("decimalCodedBBGGRR")) {
			//normal format from SSA
			//any missing 0s are filled in
			StringBuilder colorBuilder = new StringBuilder(Integer.toHexString(Integer.parseInt(value)));
			while (colorBuilder.length() < 6) {
				colorBuilder.insert(0, "0");
			}
			//order is reversed
			color = colorBuilder
					.reverse()
					.append("ff")
					.toString();
		} else if (format.equalsIgnoreCase("decimalCodedAABBGGRR")) {
			//normal format from ASS
			//any missing 0s are filled in
			StringBuilder colorBuilder = new StringBuilder(Long.toHexString(Long.parseLong(value)));
			while (colorBuilder.length() < 8) {
				colorBuilder.insert(0, "0");
			}
			//order is reversed
			color = colorBuilder.reverse().toString();
		}
		return color;
	}

	static String defaultID() {
		return "default" + styleCounter.getAndAdd(1);
	}

	@NonNull
	@Override
	public String toString() {
		return "Style{" +
				"id='" + id + '\'' +
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