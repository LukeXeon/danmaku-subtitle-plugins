package org.kexie.android.danmakux.converter;

import android.text.TextUtils;
import android.util.Log;

import org.kexie.android.danmakux.format.Format;
import org.kexie.android.danmakux.model.Section;
import org.kexie.android.danmakux.model.Subtitle;
import org.kexie.android.danmakux.io.Chardet;
import org.kexie.android.danmakux.io.Jdk18BufferedInputStream;
import org.kexie.android.danmakux.utils.TypeToken;

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
import master.flame.danmaku.danmaku.util.IOUtils;

/**
 * 解析字幕文件的弹幕解析器,只支持{@link InputStream}
 */
final class SubtitleDanmakuParser extends BaseDanmakuParser {

    private final Format format;

    private static final String TAG = "SubtitleParser";

    SubtitleDanmakuParser(Format format) {
        this.format = format;
    }

    //检查输入源
    private static boolean checkSourceType(Class<?> checkType, Type requestType) {
        ClassLoader bootClassLoader = String.class.getClassLoader();
        while (checkType != null
                && IDataSource.class.isAssignableFrom(checkType)
                && !Objects.equals(bootClassLoader, checkType.getClassLoader())) {
            for (Type type : checkType.getGenericInterfaces()) {
                if (TypeToken.deepEqualsType(type, requestType)) {
                    return true;
                }
            }
            checkType = checkType.getSuperclass();
        }
        return false;
    }

    /**
     * 获取输出,使用{@link java.io.BufferedInputStream}包装
     */
    @SuppressWarnings("unchecked")
    private InputStream getInput() {
        IDataSource<?> source;
        if ((source = mDataSource) == null) {
            return null;
        }
        if (!checkSourceType(source.getClass(),
                new TypeToken<IDataSource<InputStream>>() {
                }.getType())) {
            return null;
        }
        IDataSource<InputStream> dataSource = (IDataSource<InputStream>) source;
        return Jdk18BufferedInputStream.newInstance(dataSource.data());
    }

    @Override
    protected IDanmakus parse() {
        InputStream input = getInput();
        if (input != null) {
            Danmakus danmakus = new Danmakus();
            try {
                Charset charset = Chardet.bestGuess(input);
                Log.d(TAG, "parse: begin parse");
                Subtitle subtitle = format.parse("", input, charset);
                Log.w(TAG, "parse: " + subtitle.warnings);
                DanmakuStyleContext context = DanmakuStyleContext
                        .create(subtitle.styles.values(), getDisplayer(), mContext);
                for (Map.Entry<Integer, Section> entry
                        : subtitle.captions.entrySet()) {
                    BaseDanmaku danmaku = toDanmaku(entry, context);
                    if (danmaku != null) {
                        danmakus.addItem(danmaku);
                    }
                }
                Log.d(TAG, "parse: size=" + danmakus.size());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(input);
            }
            return danmakus;
        }
        return null;
    }

    private BaseDanmaku
    toDanmaku(Map.Entry<Integer, Section> entry, DanmakuStyleContext context) {
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
        BaseDanmaku item = context.newDanmaku(section);
        int id = entry.getKey();
        item.setTime(start);
        item.duration = new Duration(duration);
        item.index = id;
        item.setTimer(mTimer);
        item.flags = mContext.mGlobalFlagValues;
        return item;
    }
}