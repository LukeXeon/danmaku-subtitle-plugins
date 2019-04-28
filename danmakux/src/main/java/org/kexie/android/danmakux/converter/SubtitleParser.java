package org.kexie.android.danmakux.converter;

import android.content.res.Resources;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;

import org.kexie.android.danmakux.format.Format;
import org.kexie.android.danmakux.format.Section;
import org.kexie.android.danmakux.format.Style;
import org.kexie.android.danmakux.format.Subtitle;

import java.util.Map;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.Duration;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.android.AndroidFileSource;
import master.flame.danmaku.danmaku.util.DanmakuUtils;

public class SubtitleParser extends BaseDanmakuParser {

    private final Format format;

    private final static int MAX_FONT_SIZE = 20;

    private static final String TAG = "SubtitleParser";

    public SubtitleParser(Format format) {
        this.format = format;
    }

    //RRGGBBAA to AARRGGBB
    private static int parseColor(String value) {
        String rgb = value.substring(0, 6);
        String a = value.substring(6);
        return Integer.parseInt(a + rgb, 16);
    }

    private static int dp2px(final float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    protected IDanmakus parse() {
        if (mDataSource instanceof AndroidFileSource) {
            AndroidFileSource source = (AndroidFileSource) mDataSource;
            try {
                Danmakus danmakus = new Danmakus();
                Subtitle subtitle = format
                        .parse("", source.data());
                for (Map.Entry<Integer, Section> entry
                        : subtitle.captions.entrySet()) {
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
    toDanmaku(Map.Entry<Integer, Section> entry) {
        Section section = entry.getValue();
        if (TextUtils.isEmpty(section.content)) {
            return null;
        }
        int start = section.start.milliseconds;
        int end = section.end.milliseconds;
        int duration = end - start;
        if (duration <= 0) {
            return null;
        }
        BaseDanmaku item = mContext
                .mDanmakuFactory
                .createDanmaku(BaseDanmaku.TYPE_FIX_BOTTOM, mContext);
        int id = entry.getKey();
        Style style = section.style;
        int textColor = style != null
                && style.color != null
                ? parseColor(style.color)
                : Color.WHITE;
        int backgroundColor = style != null
                && style.backgroundColor != null
                ? parseColor(style.backgroundColor)
                : Color.BLACK;
        float fontSize = style != null
                && style.fontSize != null
                && TextUtils.isDigitsOnly(style.fontSize)
                ? Float.parseFloat(style.fontSize) : MAX_FONT_SIZE;
        int fontSizePx = dp2px(Math.min(fontSize, MAX_FONT_SIZE));
        item.setTime(start);
        item.duration = new Duration(duration);
        item.index = id;
        item.textSize = fontSizePx;
        item.textColor = textColor;
        item.textShadowColor = backgroundColor;
        item.setTimer(mTimer);
        String content = section.content.replaceAll("\\<br[ ]*/\\>", "/n");
        DanmakuUtils.fillText(item, content);
        item.flags = mContext.mGlobalFlagValues;
        return item;
    }
}