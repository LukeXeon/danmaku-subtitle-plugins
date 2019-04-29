package org.kexie.android.danmakux.converter;

import android.text.TextUtils;
import android.util.Log;

import org.kexie.android.danmakux.format.Format;
import org.kexie.android.danmakux.format.Section;
import org.kexie.android.danmakux.format.Subtitle;
import org.kexie.android.danmakux.utils.TypeToken;
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

    //检测字符集
    private static Charset bestGuessedCharset(InputStream input) throws IOException {
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

    //检查输入源
    private static boolean checkSourceType(Class<?> checkType, Type requestType) {
        ClassLoader bootClassLoader = String.class.getClassLoader();
        while (checkType != null
                && !Objects.equals(bootClassLoader, checkType.getClassLoader())) {
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

    /**
     *获取输出,使用{@link BufferedInputStream}包装
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
        InputStream input = dataSource.data();
        return new BufferedInputStream(input);
    }

    @Override
    protected IDanmakus parse() {
        InputStream input = getInput();
        if (input != null) {
            Danmakus danmakus = new Danmakus();
            try {
                Charset charset = bestGuessedCharset(input);
                Subtitle subtitle = format.parse("", input, charset);
                Log.w(TAG, "parse: " + subtitle.warnings);
                TextStyleContext textStyleContext = TextStyleContext
                        .create(subtitle.styles.values(), getDisplayer(), mContext);
                for (Map.Entry<Integer, Section> entry
                        : subtitle.captions.entrySet()) {
                    BaseDanmaku danmaku = toDanmaku(entry, textStyleContext);
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
    toDanmaku(Map.Entry<Integer, Section> entry, TextStyleContext context) {
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