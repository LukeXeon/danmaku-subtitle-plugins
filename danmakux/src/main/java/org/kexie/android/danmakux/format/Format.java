package org.kexie.android.danmakux.format;

import android.support.annotation.RestrictTo;
import android.util.ArrayMap;

import java.io.IOException;
import java.io.InputStream;
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

@SuppressWarnings("WeakerAccess")
public abstract class Format {

    public static final String FORMAT_ASS = "ass";
    public static final String FORMAT_SSA = "ssa";
    public static final String FORMAT_SCC = "scc";
    public static final String FORMAT_SRT = "srt";
    public static final String FORMAT_STL = "stl";
    public static final String FORMAT_XML = "xml";
    public static final String FORMAT_TTML = "ttml";
    public static final String FORMAT_DFXP = "dfxp";

    private static final Map<String, Class<? extends Format>> sFormatsTable;
    public static final Set<String> SUPPORT_FORMATS;

    static {
        sFormatsTable = new ArrayMap<>();
        sFormatsTable.put(FORMAT_ASS, ASSFormat.class);
        sFormatsTable.put(FORMAT_SSA, ASSFormat.class);
        sFormatsTable.put(FORMAT_SCC, SCCFormat.class);
        sFormatsTable.put(FORMAT_SRT, SRTFormat.class);
        sFormatsTable.put(FORMAT_STL, STLFormat.class);
        sFormatsTable.put(FORMAT_XML, XMLFormat.class);
        sFormatsTable.put(FORMAT_TTML, XMLFormat.class);
        sFormatsTable.put(FORMAT_DFXP, XMLFormat.class);
        SUPPORT_FORMATS = Collections.unmodifiableSet(sFormatsTable.keySet());
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static Format forName(String name) {
        Class<? extends Format> type = sFormatsTable.get(name.toLowerCase());
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

    /**
     * This methods receives the path to a file, parses it, and returns a TimedTextObject
     *
     * @param fileName String that contains the path to the file
     * @return TimedTextObject representing the parsed file
     * @throws IOException when having trouble reading the file from the given path
     */

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public final Subtitle parse(String fileName, InputStream input)
            throws IOException, FormatException {
        return parse(fileName, input, Charset.defaultCharset());
    }

    /**
     * This methods receives the path to a file, parses it, and returns a TimedTextObject
     *
     * @param fileName String that contains the path to the file
     * @param charset  the Charset to use when reading the InputStream
     * @return TimedTextObject representing the parsed file
     * @throws IOException when having trouble reading the file from the given path
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public abstract Subtitle parse(String fileName, InputStream input, Charset charset)
            throws IOException, FormatException;

    /**
     * This method transforms a given TimedTextObject into a formated subtitle file
     *
     * @param tto the object to transform into a file
     * @return NULL if the given TimedTextObject has not been built first,
     * or String[] where each String is at least a line, if size is 2, then the file has at least two lines.
     * or byte[] in case the file is a binary (as is the case of STL format)
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public abstract Object transformation(Subtitle tto);

}
