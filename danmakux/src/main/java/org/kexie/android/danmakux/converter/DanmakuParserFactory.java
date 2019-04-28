package org.kexie.android.danmakux.converter;

import android.graphics.Color;
import android.support.annotation.StringDef;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import org.kexie.android.danmakux.format.ASSFormat;
import org.kexie.android.danmakux.format.Caption;
import org.kexie.android.danmakux.format.SCCFormat;
import org.kexie.android.danmakux.format.SRTFormat;
import org.kexie.android.danmakux.format.STLFormat;
import org.kexie.android.danmakux.format.Style;
import org.kexie.android.danmakux.format.TimedText;
import org.kexie.android.danmakux.format.TimedTextFormat;
import org.kexie.android.danmakux.format.XMLFormat;
import org.kexie.android.danmakux.utils.FileUtils;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.Duration;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.android.AndroidFileSource;
import master.flame.danmaku.danmaku.util.DanmakuUtils;

public final class DanmakuParserFactory {

    private DanmakuParserFactory() {
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

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({FORMAT_ASS, FORMAT_SSA, FORMAT_SCC, FORMAT_SRT, FORMAT_STL, FORMAT_XML, FORMAT_TTML, FORMAT_DFXP})
    @interface Format {
    }

    private static final String TAG = "DanmakuParserFactory";

    private static final Map<String, Class<? extends TimedTextFormat>> sFormats;

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

    public static Set<String> getSupportFormats() {
        return Collections.unmodifiableSet(sFormats.keySet());
    }


    public static BaseDanmakuParser create(File file) {
        String ext = FileUtils.getFileExtension(file);
        return create(ext);
    }

    public static BaseDanmakuParser create(@Format String ext) {
        Class<? extends TimedTextFormat> type = sFormats.get(ext.toLowerCase());
        TimedTextFormat format0 = null;
        if (type != null) {
            try {
                format0 = type.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        TimedTextFormat format;
        return (format = format0) == null ? null : new BaseDanmakuParser() {
            @Override
            protected IDanmakus parse() {
                if (mDataSource instanceof AndroidFileSource) {
                    AndroidFileSource source = (AndroidFileSource) mDataSource;
                    try {
                        Danmakus danmakus = new Danmakus();
                        TimedText timedTextObject
                                = format.parseFile("", source.data());
                        for (Map.Entry<Integer, Caption> entry
                                : timedTextObject.captions.entrySet()) {
                            BaseDanmaku danmaku = toDanmaku(entry);
                            if (danmaku != null) {
                                danmakus.addItem(danmaku);
                            }
                        }
                        Log.d(TAG, "parse: size=" + danmakus.size());
                        return danmakus;
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        source.release();
                    }
                }
                return null;
            }

            private BaseDanmaku
            toDanmaku(Map.Entry<Integer, Caption> entry) {
                Caption caption = entry.getValue();
                if (TextUtils.isEmpty(caption.content)) {
                    return null;
                }
                BaseDanmaku item = mContext
                        .mDanmakuFactory
                        .createDanmaku(BaseDanmaku.TYPE_FIX_BOTTOM, mContext);
                int id = entry.getKey();
                Style style = caption.style;
                int textColor = style != null
                        && style.color != null
                        ? parseColor(style.color)
                        : Color.WHITE;
                int backgroundColor = style != null
                        && style.backgroundColor != null
                        ? parseColor(style.backgroundColor)
                        : Color.BLACK;
                int fontSize = style != null
                        && style.fontSize != null
                        && TextUtils.isDigitsOnly(style.fontSize)
                        ? Integer.parseInt(style.fontSize) : 20;

                int start = caption.start.milliseconds;
                int end = caption.end.milliseconds;
                String text = caption.content;
                item.setTime(start);
                item.duration = new Duration(Math.max(0, end - start));
                item.index = id;
                item.textSize = fontSize;
                item.textColor = textColor;
                item.textShadowColor = backgroundColor;
                item.setTimer(mTimer);
                DanmakuUtils.fillText(item, text);
                return item;
            }
        };
    }

    //RRGGBBAA to AARRGGBB
    private static int parseColor(String value) {
        String rgb = value.substring(0, 6);
        String a = value.substring(6);
        return Integer.parseInt(a + rgb, 16);
    }
}