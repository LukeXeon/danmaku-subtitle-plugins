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
import org.kexie.android.danmakux.utils.FileUtils;

import java.io.File;
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

    /**
     * 从指定格式产生,你可以使用{@link FileUtils#getFileExtension(File)}
     * 来获取文件格式
     *
     * @param ext 文件格式
     * @return 返回解析器
     */
    public static BaseDanmakuParser forFormat(@NonNull @FormatType String ext) {
        Class<? extends Format> type = sFormatsTable.get(ext.toLowerCase());
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

    /**
     * 产生一个解析{@link SubtitleParserFactory#FORMAT_STL}格式的解析器
     *
     * @param frameRate 帧率
     * @return STL格式解析器
     */
    public static BaseDanmakuParser forFrameRate(float frameRate) {
        STLFormat format = new STLFormat();
        format.setFrameRate(frameRate);
        return new SubtitleDanmakuParser(format);
    }

    public static BaseDanmakuParser forDataSource(FormattedDataSource dataSource) {
        BaseDanmakuParser parser = forFormat(dataSource.getFormat());
        parser.load(dataSource);
        return parser;
    }

    public static BaseDanmakuParser forFile(File file) {
        FormattedDataSource dataSource = FileUtils.loadDataSource(file);
        return forDataSource(dataSource);
    }
}