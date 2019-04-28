package org.kexie.android.danmakux.converter;

import android.text.TextUtils;
import android.util.ArrayMap;

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

    private static final Map<String, Class<? extends TimedTextFormat>> sFormats;

    static {
        sFormats = new ArrayMap<>();
        sFormats.put("ass", ASSFormat.class);
        sFormats.put("ssa", ASSFormat.class);
        sFormats.put("scc", SCCFormat.class);
        sFormats.put("srt", SRTFormat.class);
        sFormats.put("stl", STLFormat.class);
        sFormats.put("xml", XMLFormat.class);
        sFormats.put("ttml", XMLFormat.class);
        sFormats.put("dfxp", XMLFormat.class);
    }

    public static Set<String> getSupportFormats() {
        return Collections.unmodifiableSet(sFormats.keySet());
    }


    public static BaseDanmakuParser create(File file) {
        String ext = FileUtils.getFileExtension(file);
        return create(ext);
    }

    public static BaseDanmakuParser create(String ext) {
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
                AndroidFileSource source = (AndroidFileSource) mDataSource;
                if (source != null) {
                    try {
                        Danmakus danmakus = new Danmakus();
                        TimedText timedTextObject
                                = format.parseFile("", source.data());
                        for (Map.Entry<Integer, Caption> entry
                                : timedTextObject.captions.entrySet()) {
                            BaseDanmaku danmaku = toDanmaku(entry);
                            danmakus.addItem(danmaku);
                        }
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

                BaseDanmaku item = mContext
                        .mDanmakuFactory
                        .createDanmaku(BaseDanmaku.TYPE_FIX_BOTTOM, mContext);
                int id = entry.getKey();
                Caption caption = entry.getValue();
                Style style = caption.style;
                int textColor = parseColor(style.color);
                int backgroundColor = parseColor(style.backgroundColor);
                int fontSize = TextUtils.isDigitsOnly(style.fontSize)
                        ? Integer.parseInt(style.fontSize) : 20;
                int start = caption.start.getMilliseconds();
                int end = caption.end.getMilliseconds();
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