package org.kexie.android.danmakux.converter;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.ArrayMap;

import org.kexie.android.danmakux.format.ASSFormat;
import org.kexie.android.danmakux.format.Caption;
import org.kexie.android.danmakux.format.SCCFormat;
import org.kexie.android.danmakux.format.SRTFormat;
import org.kexie.android.danmakux.format.STLFormat;
import org.kexie.android.danmakux.format.Style;
import org.kexie.android.danmakux.format.TTMLFormat;
import org.kexie.android.danmakux.format.TimedText;
import org.kexie.android.danmakux.format.TimedTextFormat;
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
        sFormats.put("scc", SCCFormat.class);
        sFormats.put("srt", SRTFormat.class);
        sFormats.put("stl", STLFormat.class);
        sFormats.put("ttml", TTMLFormat.class);
    }

    public static Set<String> getSupportFormats() {
        return Collections.unmodifiableSet(sFormats.keySet());
    }

    public static BaseDanmakuParser create(File file) {
        String fileName = file.getName();
        String ext = FileUtils.getFileExtension(file).toLowerCase();
        Class<? extends TimedTextFormat> type = sFormats.get(ext);
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
                                = format.parseFile(fileName, source.data());
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
                int textColor = Color.parseColor('#'
                        + style.color.toUpperCase());
                int backgroundColor = Color.parseColor('#'
                        + style.backgroundColor.toUpperCase());
                int fontSize = TextUtils.isDigitsOnly(style.fontSize)
                        ? Integer.parseInt(style.fontSize) : 20;
                int start = caption.start.getMseconds();
                int end = caption.end.getMseconds();
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
}