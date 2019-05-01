package org.kexie.android.danmakux.format;

import android.support.annotation.RestrictTo;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


/**
 * This class represents W3C's TTML DFXP .XML subtitle format
 * <br><br>
 * Copyright (c) 2012 J. David Requejo <br>
 * j[dot]david[dot]requejo[at] Gmail
 * <br><br>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * <br><br>
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 * <br><br>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 * 
 * @author J. David Requejo
 *
 */

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class XMLFormat extends Format {

	static {
		System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
	}

	public Subtitle parse(String fileName, InputStream input, Charset charset) throws IOException, FormatException {

		Subtitle tto = new Subtitle();
		tto.fileName = fileName;

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new InputStreamReader(input, charset)));
			doc.getDocumentElement().normalize();

			//we recover the metadata
			Node node = doc.getElementsByTagName("ttm:title").item(0);
			if (node != null) tto.title = node.getTextContent();
			node = doc.getElementsByTagName("ttm:copyright").item(0);
			if (node != null) tto.copyright = node.getTextContent();
			node = doc.getElementsByTagName("ttm:desc").item(0);
			if (node != null) tto.description = node.getTextContent();

			//we recover the styles
			NodeList styleN = doc.getElementsByTagName("style");
			//we recover the timed text elements
			NodeList captionsN = doc.getElementsByTagName("p");
			//regions of the layout could also be recovered this way

			tto.warnings += "Styling attributes are only recognized inside a style definition, to be referenced later in the captions.\n\n";
			//we parse the styles
			for (int i = 0; i < styleN.getLength(); i++) {
				Style style = new Style(Style.defaultID());
				node = styleN.item(i);
				NamedNodeMap attr = node.getAttributes();
				//we get the id
				Node currentAtr = attr.getNamedItem("id");
				if (currentAtr != null)
					style.id = currentAtr.getNodeValue();
				currentAtr = attr.getNamedItem("xml:id");
				if (currentAtr != null)
					style.id = currentAtr.getNodeValue();

				//we get the style it may be based upon
				currentAtr = attr.getNamedItem("style");
				if (currentAtr != null) {
					Style old = tto.styles.get(currentAtr.getNodeValue());
					if (old != null) {
						style = new Style(style.id, old);
					}
				}

				//we check for background color
				currentAtr = attr.getNamedItem("tts:backgroundColor");
				if (currentAtr != null)
					style.backgroundColor = parseColor(currentAtr.getNodeValue(), tto);

				//we check for color
				currentAtr = attr.getNamedItem("tts:color");
				if (currentAtr != null)
					style.color = parseColor(currentAtr.getNodeValue(), tto);

				//we check for font family
				currentAtr = attr.getNamedItem("tts:fontFamily");
				if (currentAtr != null)
					style.font = currentAtr.getNodeValue();

				//we check for font size
				currentAtr = attr.getNamedItem("tts:fontSize");
				if (currentAtr != null)
					style.fontSize = currentAtr.getNodeValue();

				//we check for italics
				currentAtr = attr.getNamedItem("tts:fontStyle");
				if (currentAtr != null)
					if (currentAtr.getNodeValue().equalsIgnoreCase("italic") || currentAtr.getNodeValue().equalsIgnoreCase("oblique"))
						style.italic = true;
					else if (currentAtr.getNodeValue().equalsIgnoreCase("normal"))
						style.italic = false;

				//we check for bold
				currentAtr = attr.getNamedItem("tts:fontWeight");
				if (currentAtr != null)
					if (currentAtr.getNodeValue().equalsIgnoreCase("bold"))
						style.bold = true;
					else if (currentAtr.getNodeValue().equalsIgnoreCase("normal"))
						style.bold = false;

				//we check opacity (to set the alpha)
				currentAtr = attr.getNamedItem("tts:opacity");
				if (currentAtr != null) {
					try {
						//a number between 1.0 and 0
						float alpha = Float.parseFloat(currentAtr.getNodeValue());
						if (alpha > 1)
							alpha = 1;
						else if (alpha < 0)
							alpha = 0;

						String aa = Integer.toHexString((int) (alpha * 255));
						if (aa.length() < 2)
							aa = "0" + aa;

						style.color = style.color.substring(0, 6) + aa;
						style.backgroundColor = style.backgroundColor.substring(0, 6) + aa;

					} catch (NumberFormatException e) {
						//ignore the alpha
					}
				}

				//we check for text align
				currentAtr = attr.getNamedItem("tts:alignment");
				if (currentAtr != null)
					if (currentAtr.getNodeValue().equalsIgnoreCase("left") || currentAtr.getNodeValue().equalsIgnoreCase("start"))
						style.alignment = "bottom-left";
					else if (currentAtr.getNodeValue().equalsIgnoreCase("right") || currentAtr.getNodeValue().equalsIgnoreCase("end"))
						style.alignment = "bottom-right";

				//we check for underline
				currentAtr = attr.getNamedItem("tts:textDecoration");
				if (currentAtr != null)
					if (currentAtr.getNodeValue().equalsIgnoreCase("underline"))
						style.underline = true;
					else if (currentAtr.getNodeValue().equalsIgnoreCase("noUnderline"))
						style.underline = false;

				//we add the style
				tto.styles.put(style.id, style);
			}

			//we parse the captions
			for (int i = 0; i < captionsN.getLength(); i++) {
				Section section = new Section();
				section.content = "";
				boolean validCaption = true;
				node = captionsN.item(i);

				NamedNodeMap attr = node.getAttributes();
				//we get the begin time
				Node currentAtr = attr.getNamedItem("begin");
				//if no begin is present, 0 is assumed
				section.start = new Time("", "");
				section.end = new Time("", "");
				if (currentAtr != null)
					section.start.milliseconds = parseTimeExpression(currentAtr.getNodeValue(), tto, doc);

				//we get the end time, if present, duration is ignored, otherwise end is calculated from duration
				currentAtr = attr.getNamedItem("end");
				if (currentAtr != null)
					section.end.milliseconds = parseTimeExpression(currentAtr.getNodeValue(), tto, doc);
				else {
					currentAtr = attr.getNamedItem("dur");
					if (currentAtr != null)
						section.end.milliseconds = section.start.milliseconds + parseTimeExpression(currentAtr.getNodeValue(), tto, doc);
					else
						//no end or duration, invalid format, caption is discarded
						validCaption = false;
				}

				//we get the style
				currentAtr = attr.getNamedItem("style");
				if (currentAtr != null) {
					Style style = tto.styles.get(currentAtr.getNodeValue());
					if (style != null)
						section.style = style;
					else
						//unrecognized style
						tto.warnings += "unrecoginzed style referenced: " + currentAtr.getNodeValue() + "\n\n";
				}

				//we save the text
				NodeList textN = node.getChildNodes();
				for (int j = 0; j < textN.getLength(); j++) {
					if (textN.item(j).getNodeName().equals("#text"))
						section.content += textN.item(j).getTextContent().trim();
					else if (textN.item(j).getNodeName().equals("br"))
						section.content += "<br />";

				}
				//is this check worth it?
				if (section.content.replaceAll("\\<br /\\>", "").trim().isEmpty())
					validCaption = false;

				//and save the caption
				if (validCaption) {
					int key = section.start.milliseconds;
					//in case the key is already there, we increase it by a millisecond, since no duplicates are allowed
					while (tto.captions.containsKey(key)) key++;
					tto.captions.put(key, section);
				}

			}


		} catch (Exception e) {
			e.printStackTrace();
			//this could be a fatal error...
			throw new FormatException("Error during parsing: " + e.getMessage());
		}


		tto.built = true;
		return tto;
	}


	public String[] transform(Subtitle tto) {

		//first we check if the TimedTextObject had been built, otherwise...
		if (!tto.built)
			return null;

		//we will write the lines in an ArrayList 
		int index = 0;
		//the minimum size of the file is the number of captions and styles + lines for sections and formats and the metadata, so we'll take some extra space.
		ArrayList<String> file = new ArrayList<>(30 + tto.styles.size() + tto.captions.size());

		//identification line is placed
		file.add(index++, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		//root element is placed
		file.add(index++, "<tt xml:lang=\"" + tto.language + "\" xmlns=\"http://www.w3.org/ns/ttml\" xmlns:tts=\"http://www.w3.org/ns/ttml#styles\">");
		//head
		file.add(index++, "\t<head>");
		//metadata
		file.add(index++, "\t\t<metadata xmlns:ttm=\"http://www.w3.org/ns/ttml#metadata\">");
		//title
		String title;
		if (tto.title == null || tto.title.isEmpty())
			title = tto.fileName;
		else title = tto.title;
		file.add(index++, "\t\t\t<ttm:title>" + title + "</ttm:title>");
		//Copyright
		if (tto.copyright != null && !tto.copyright.isEmpty())
			file.add(index++, "\t\t\t<ttm:copyright>" + tto.copyright + "</ttm:copyright>");
		//additional info
		String desc = "Converted by the Online Subtitle Converter developed by J. David Requejo\n";
		if (tto.author != null && !tto.author.isEmpty())
			desc += "\n Original file by: " + tto.author + "\n";
		if (tto.description != null && !tto.description.isEmpty())
			desc += tto.description + "\n";
		file.add(index++, "\t\t\t<ttm:desc>" + desc + "\t\t\t</ttm:desc>");

		//metadata closes
		file.add(index++, "\t\t</metadata>");
		//styles opens
		file.add(index++, "\t\t<styles>");

		String line;
		//Next we iterate over the styles
		for (Style style : tto.styles.values()) {
			//we add the attributes
			line = "\t\t\t<style xml:id=\"" + style.id + "\"";
			if (style.color != null)
				line += " tts:color=\"#" + style.color + "\"";
			if (style.backgroundColor != null)
				line += " tts:backgroundColor=\"#" + style.backgroundColor + "\"";
			if (style.font != null)
				line += " tts:fontFamily=\"" + style.font + "\"";
			if (style.fontSize != null)
				line += " tts:fontSize=\"" + style.fontSize + "\"";
			if (style.italic)
				line += " tts:fontStyle=\"italic\"";
			if (style.bold)
				line += " tts:fontWeight=\"bold\"";
			line += " tts:alignment=\"";
			if (style.alignment.contains("left"))
				line += "left\"";
			else if (style.alignment.contains("right"))
				line += "rigth\"";
			else line += "center\"";
			if (style.underline)
				line += " tts:textDecoration=\"underline\"";
			//style is ready, we close it.
			line += " />";
			//we insert it
			file.add(index++, line);
		}

		//styles closes
		file.add(index++, "\t\t</styles>");

		//head closes
		file.add(index++, "\t</head>");
		//body opens
		file.add(index++, "\t<body>");
		//unique div opens
		file.add(index++, "\t\t<div>");

		//Next we iterate over the captions
		for (Section section : tto.captions.values()) {
			//we open the subtitle line
			line = "\t\t\t<p begin=\"" + section.start.getTime("hh:mm:ss,ms").replace(',', '.') + "\"";
			line += " end=\"" + section.end.getTime("hh:mm:ss,ms").replace(',', '.') + "\"";
			if (section.style != null)
				line += " style=\"" + section.style.id + "\"";
			//attributes are done being inserted, if region was implemented it should be added before this.
			line += " >" + section.content + "</p>\n";
			//we write the line
			file.add(index++, line);
		}

		//unique div closes
		file.add(index++, "\t\t</div>");
		//body closes
		file.add(index++, "\t</body>");
		//root closes
		file.add(index++, "</tt>");

		//an empty line is added
		file.add(index, "");

		String[] toReturn = new String[file.size()];
		for (int i = 0; i < toReturn.length; i++) {
			toReturn[i] = file.get(i);
		}
		return toReturn;
	}


	/* PRIVATE METHODS */

	/**
	 * Identifies the color expression and obtains the RGBA equivalent value.
	 *
	 * @param color
	 * @return
	 */
	private String parseColor(String color, Subtitle tto) {
		String value;
		String[] values;
		if (color.startsWith("#")) {
			if (color.length() == 7)
				value = color.substring(1) + "ff";
			else if (color.length() == 9)
				value = color.substring(1);
			else {
				//unrecognized format
				value = "ffffffff";
				tto.warnings += "Unrecoginzed format: " + color + "\n\n";
			}

		} else if (color.startsWith("rgb")) {
			boolean alpha = false;
			if (color.startsWith("rgba"))
				alpha = true;
			try {
				values = color.split("\\(")[1].split(",");
				int r, g, b, a = 255;
				r = Integer.parseInt(values[0]);
				g = Integer.parseInt(values[1]);
				b = Integer.parseInt(values[2].substring(0, 2));
				if (alpha) a = Integer.parseInt(values[3].substring(0, 2));

				values[0] = Integer.toHexString(r);
				values[1] = Integer.toHexString(g);
				values[2] = Integer.toHexString(b);
				if (alpha) values[2] = Integer.toHexString(a);

				StringBuilder valueBuilder = new StringBuilder();
				for (int i = 0; i < values.length; i++) {
					if (values[i].length() < 2)
						values[i] = "0" + values[i];
					valueBuilder.append(values[i]);
				}
				value = valueBuilder.toString();

				if (!alpha)
					value += "ff";

			} catch (Exception e) {
				value = "ffffffff";
				tto.warnings += "Unrecoginzed color: " + color + "\n\n";
			}

		} else {
			//it should be a named color so...
			value = Style.getRGBAValue("name", color);
			//if not recognized named color
			if (value == null || value.isEmpty()) {
				value = "ffffffff";
				tto.warnings += "Unrecoginzed color: " + color + "\n\n";
			}
		}

		return value;
	}


	/**
	 * returns the number of milliseconds equivalent to this time expression
	 *
	 * @param timeExpression
	 * @return
	 */
	private int parseTimeExpression(String timeExpression, Subtitle tto, Document doc) {
		int mSeconds = 0;
		if (timeExpression.contains(":")) {
			//it is a clock time
			String[] parts = timeExpression.split(":");
			if (parts.length == 3) {
				//we have h:m:s.fraction
				int h, m;
				float s;
				h = Integer.parseInt(parts[0]);
				m = Integer.parseInt(parts[1]);
				s = Float.parseFloat(parts[2]);
				mSeconds = h * 3600000 + m * 60000 + (int) (s * 1000);
			} else if (parts.length == 4) {
				//we have h:m:s:f.fraction
				int h, m, s;
				float f;
				int frameRate = 25;
				//we recover the frame rate
				Node n = doc.getElementsByTagName("ttp:frameRate").item(0);
				if (n != null) {
					//used as auxiliary string
					String aux = n.getNodeValue();
					try {
						frameRate = Integer.parseInt(aux);
					} catch (NumberFormatException e) {
						//should not happen, but if it does, use default value...
					}
				}
				h = Integer.parseInt(parts[0]);
				m = Integer.parseInt(parts[1]);
				s = Integer.parseInt(parts[2]);
				f = Float.parseFloat(parts[3]);
				mSeconds = h * 3600000 + m * 60000 + s * 1000 + (int) (f * 1000 / frameRate);
			} else {
				//unrecognized  clock time format
			}

		} else {
			//it is an offset - time, this is composed of a number and a metric
			String metric = timeExpression.substring(timeExpression.length() - 1);
			timeExpression = timeExpression.substring(0, timeExpression.length() - 1).replace(',', '.').trim();
			double time;
			try {
				time = Double.parseDouble(timeExpression);
				if (metric.equalsIgnoreCase("h"))
					mSeconds = (int) (time * 3600000);

				else if (metric.equalsIgnoreCase("m"))
					mSeconds = (int) (time * 60000);

				else if (metric.equalsIgnoreCase("s"))
					mSeconds = (int) (time * 1000);

				else if (metric.equalsIgnoreCase("ms"))
					mSeconds = (int) time;

				else if (metric.equalsIgnoreCase("f")) {
					int frameRate;
					//we recover the frame rate
					Node n = doc.getElementsByTagName("ttp:frameRate").item(0);
					if (n != null) {
						//used as auxiliary string
						String s = n.getNodeValue();
						frameRate = Integer.parseInt(s);
						mSeconds = (int) (time * 1000 / frameRate);
					}

				} else if (metric.equalsIgnoreCase("t")) {
					int tickRate;
					//we recover the tick rate
					Node n = doc.getElementsByTagName("ttp:tickRate").item(0);
					if (n != null) {
						//used as auxiliary string
						String s = n.getNodeValue();
						tickRate = Integer.parseInt(s);
						mSeconds = (int) (time * 1000 / tickRate);
					}


				} else {
					//invalid metric

				}
			} catch (NumberFormatException e) {
				//incorrect format for offset time
			}
		}
		return mSeconds;
	}
}
