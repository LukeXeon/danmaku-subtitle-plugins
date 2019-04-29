package org.kexie.android.danmakux.converter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;

import org.kexie.android.danmakux.format.Format;
import org.kexie.android.danmakux.format.Section;
import org.kexie.android.danmakux.format.Style;
import org.kexie.android.danmakux.format.Subtitle;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.Duration;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
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

    private static Charset getCharset(InputStream input) throws IOException {
        input.mark(0);
        byte[] buffer = new byte[1024];
        UniversalDetector detector = new UniversalDetector(null);
        int length;
        while ((length = input.read(buffer)) > 0 && !detector.isDone()) {
            detector.handleData(buffer, 0, length);
        }
        detector.dataEnd();
        input.reset();
        String charset = detector.getDetectedCharset();
        return charset == null ? Charset.defaultCharset() : Charset.forName(charset);
    }

    private static boolean checkType(Class<?> checkType, Type requestType) {
        ClassLoader bootClassLoader = Context.class.getClassLoader();
        while (checkType != null && !Objects.equals(bootClassLoader, checkType.getClassLoader())) {
            for (Type type : checkType.getGenericInterfaces()) {
                if (Objects.equals(type.getTypeName(),
                        requestType.getTypeName())) {
                    return true;
                }
            }
            checkType = checkType.getSuperclass();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private InputStream getInput() {
        IDataSource<?> source;
        if ((source = mDataSource) == null) {
            return null;
        }
        if (!checkType(source.getClass(),
                new TypeToken<IDataSource<InputStream>>() {
                }.getType())) {
            return null;
        }
        IDataSource<InputStream> dataSource = (IDataSource<InputStream>) source;
        InputStream input = dataSource.data();
        return input.markSupported() ? input : new BufferedInputStream(input);
    }

    @Override
    protected IDanmakus parse() {
        InputStream input = getInput();
        if (input != null) {
            try {
                Danmakus danmakus = new Danmakus();
                Charset charset = getCharset(input);
                Subtitle subtitle = format.parse("", input, charset);
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
            }
        }
        return null;
    }

    private BaseDanmaku
    toDanmaku(Map.Entry<Integer, Section> entry, FontScale fontScale) {
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
                : FontScale.MID_FONT_SIZE);
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