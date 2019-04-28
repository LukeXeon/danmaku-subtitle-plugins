package org.kexie.android.danmakux.converter;

import android.content.res.Resources;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;

import com.orhanobut.logger.Logger;

import org.kexie.android.danmakux.format.Format;
import org.kexie.android.danmakux.format.Section;
import org.kexie.android.danmakux.format.Style;
import org.kexie.android.danmakux.format.Subtitle;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.Duration;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.android.AndroidFileSource;
import master.flame.danmaku.danmaku.util.DanmakuUtils;

final class SubtitleDanmakuParser extends BaseDanmakuParser {

    private final Format format;

    private static final String TAG = "SubtitleParser";

    SubtitleDanmakuParser(Format format) {
        this.format = format;
    }

    //RRGGBBAA to AARRGGBB
    private static int parseColor(String value) {
        String rgb = value.substring(0, 6);
        String a = value.substring(6);
        return Integer.parseInt(a + rgb, 16);
    }

    private static Charset getCharset(InputStream inputStream) throws IOException {
        inputStream.mark(0);
        inputStream.reset();
        return null;
    }


    private static final class FontScale {
        private final static float MAX_FONT_SIZE = 20;
        private final static float MIN_FONT_SIZE = 15;
        private final static FontScale NORMAL_FONT_SCALE
                = new FontScale(MAX_FONT_SIZE, MIN_FONT_SIZE);
        private final float max;
        private final float min;
        private final float density;

        private FontScale(float max, float min) {
            this.max = max;
            this.min = min;
            this.density = Resources.getSystem().getDisplayMetrics().density;
        }

        private static float mid(float f1, float f2) {
            return (f1 + f2) / 2f;
        }

        private int adapt(float value) {
            float mid = mid(min, max);
            if (value > mid) {
                float delta = value - mid;
                float p = delta / (max - mid);
                float mid2 = mid(MAX_FONT_SIZE, MIN_FONT_SIZE);
                value = mid2 + p * (MAX_FONT_SIZE - mid2);
            } else if (value < mid) {
                float delta = mid - value;
                float p = delta / (mid - min);
                float mid2 = mid(MAX_FONT_SIZE, MIN_FONT_SIZE);
                value = mid2 - p * (mid2 - MIN_FONT_SIZE);
            } else {
                value = mid(MAX_FONT_SIZE, MIN_FONT_SIZE);
            }
            return dp2px(value);
        }

        private int dp2px(float value) {
            return (int) (value * density + 0.5f);
        }

        private static FontScale create(Collection<Style> styles) {
            if (styles.isEmpty()) {
                return FontScale.NORMAL_FONT_SCALE;
            }
            float min = Float.MIN_VALUE, max = Float.MIN_VALUE;
            for (Style style : styles) {
                if (!TextUtils.isEmpty(style.fontSize)
                        && TextUtils.isDigitsOnly(style.fontSize)) {
                    float size = Float.parseFloat(style.fontSize);
                    max = Math.max(max, size);
                    min = Math.min(min, size);
                }
            }
            return new FontScale(max, min);
        }
    }

    @Override
    protected IDanmakus parse() {
        if (mDataSource instanceof AndroidFileSource) {
            AndroidFileSource source = (AndroidFileSource) mDataSource;
            try {
                Danmakus danmakus = new Danmakus();
                InputStream inputStream = source.data();
                Charset charset = getCharset(inputStream);
                Subtitle subtitle = format.parse("", inputStream, charset);
                Log.w(TAG, "parse: " + subtitle.warnings);
                FontScale fontScale = FontScale.create(subtitle.styling.values());
                for (Map.Entry<Integer, Section> entry
                        : subtitle.captions.entrySet()) {
                    BaseDanmaku danmaku = toDanmaku(entry, fontScale);
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
    toDanmaku(Map.Entry<Integer, Section> entry, FontScale fontScale) {
        Logger.d(entry.getValue());
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
        int fontSize = fontScale.adapt(style != null
                && style.fontSize != null
                ? Float.parseFloat(style.fontSize)
                : (FontScale.MAX_FONT_SIZE + FontScale.MIN_FONT_SIZE) / 2f);
        item.setTime(start);
        item.duration = new Duration(duration);
        item.index = id;
        item.textSize = fontSize;
        item.textColor = textColor;
        item.textShadowColor = backgroundColor;
        item.setTimer(mTimer);
        String content = section.content.replaceAll("\\<br[ ]*/\\>", "/n");
        DanmakuUtils.fillText(item, content);
        item.flags = mContext.mGlobalFlagValues;
        return item;
    }
}