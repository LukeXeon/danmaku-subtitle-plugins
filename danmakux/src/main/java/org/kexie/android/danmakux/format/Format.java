package org.kexie.android.danmakux.format;

import android.support.annotation.StringDef;
import android.util.ArrayMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * This class specifies the interface for any format supported by the converter, these formats must
 * create a {@link Subtitle} from an {@link InputStream} (so it can process files form standard In or uploads)
 * and return a String array for text formats, or byte array for binary formats.
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

public abstract class Format {

	public static final String FORMAT_ASS = "ass";
	public static final String FORMAT_SSA = "ssa";
	public static final String FORMAT_SCC = "scc";
	public static final String FORMAT_SRT = "srt";
	public static final String FORMAT_STL = "stl";
	public static final String FORMAT_XML = "xml";
	public static final String FORMAT_TTML = "ttml";
	public static final String FORMAT_DFXP = "dfxp";

	private static final Map<String, Class<? extends Format>> sFormats;

	static {
		sFormats = new ArrayMap<>();
		sFormats.put(FORMAT_ASS, ASSFormat.class);
		sFormats.put(FORMAT_SSA, ASSFormat.class);
		sFormats.put(FORMAT_SCC, SCCFormat.class);
		sFormats.put(FORMAT_SRT, SRTFormat.class);
		sFormats.put(FORMAT_STL, STLFormat.class);
		sFormats.put(FORMAT_XML, XMLFormat.class);
		sFormats.put(FORMAT_TTML, XMLFormat.class);
		sFormats.put(FORMAT_DFXP, XMLFormat.class);
	}

	public static Format forName(@FormatType String ext) {
		Class<? extends Format> type = sFormats.get(ext.toLowerCase());
		Format format = null;
		if (type != null) {
			try {
				format = type.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return format;
	}

	public static String getFileFormat(File file) {
		if (file == null) return "";
		return getFileFormat(file.getPath());
	}

	/**
	 * Return the extension of file.
	 *
	 * @param filePath The path of file.
	 * @return the extension of file
	 */
	public static String getFileFormat(String filePath) {
		if (isSpace(filePath)) return "";
		int lastPoi = filePath.lastIndexOf('.');
		int lastSep = filePath.lastIndexOf(File.separator);
		if (lastPoi == -1 || lastSep >= lastPoi) return "";
		return filePath.substring(lastPoi + 1);
	}

	private static boolean isSpace(final String s) {
		if (s == null) return true;
		for (int i = 0, len = s.length(); i < len; ++i) {
			if (!Character.isWhitespace(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static Set<String> getSupportFormats() {
		return Collections.unmodifiableSet(sFormats.keySet());
	}

	/**
	 * This methods receives the path to a file, parses it, and returns a TimedTextObject
	 *
	 * @param fileName String that contains the path to the file
	 * @return TimedTextObject representing the parsed file
	 * @throws IOException when having trouble reading the file from the given path
	 */
	public abstract Subtitle parse(String fileName, InputStream is)
			throws IOException, FatalParsingException;

	/**
	 * This methods receives the path to a file, parses it, and returns a TimedTextObject
	 *
	 * @param fileName  String that contains the path to the file
	 * @param isCharset the Charset to use when reading the InputStream
	 * @return TimedTextObject representing the parsed file
	 * @throws IOException when having trouble reading the file from the given path
	 */
	public abstract Subtitle parse(String fileName, InputStream is, Charset isCharset)
			throws IOException, FatalParsingException;

	/**
	 * This method transforms a given TimedTextObject into a formated subtitle file
	 *
	 * @param tto the object to transform into a file
	 * @return NULL if the given TimedTextObject has not been built first,
	 * or String[] where each String is at least a line, if size is 2, then the file has at least two lines.
	 * or byte[] in case the file is a binary (as is the case of STL format)
	 */
	public abstract Object toFile(Subtitle tto);

	@Retention(RetentionPolicy.SOURCE)
	@StringDef({FORMAT_ASS,
			FORMAT_SSA,
			FORMAT_SCC,
			FORMAT_SRT,
			FORMAT_STL,
			FORMAT_XML,
			FORMAT_TTML,
			FORMAT_DFXP})


	private @interface FormatType {
	}
}
