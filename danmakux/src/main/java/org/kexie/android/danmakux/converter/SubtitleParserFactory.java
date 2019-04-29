package org.kexie.android.danmakux.converter;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.util.ArrayMap;

import org.kexie.android.danmakux.format.ASSFormat;
import org.kexie.android.danmakux.format.Format;
import org.kexie.android.danmakux.format.SCCFormat;
import org.kexie.android.danmakux.format.SRTFormat;
import org.kexie.android.danmakux.format.STLFormat;
import org.kexie.android.danmakux.format.XMLFormat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;

@SuppressWarnings("WeakerAccess")
public final class SubtitleParserFactory {

    private SubtitleParserFactory() {
        throw new AssertionError();
    }

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

    private static final String TAG = "DanmakuParserFactory";

    public static BaseDanmakuParser forFormat(@NonNull @FormatType String ext) {
        Class<? extends Format> type = sFormats.get(ext.toLowerCase());
        Format format = null;
        if (type != null) {
            try {
                format = type.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (format == null) {
            throw new IllegalArgumentException();
        }
        return new SubtitleDanmakuParser(format);
    }

    public static BaseDanmakuParser forFrameRate(float frameRate) {
        STLFormat format = new STLFormat();
        format.setFrameRate(frameRate);
        return new SubtitleDanmakuParser(format);
    }

    public static Set<String> getSupportFormats() {
        return Collections.unmodifiableSet(sFormats.keySet());
    }
}