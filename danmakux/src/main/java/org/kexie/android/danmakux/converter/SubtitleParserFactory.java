package org.kexie.android.danmakux.converter;

import android.support.annotation.NonNull;

import org.kexie.android.danmakux.format.Format;
import org.kexie.android.danmakux.format.FormatType;
import org.kexie.android.danmakux.format.STLFormat;
import org.kexie.android.danmakux.io.FormattedDataSource;
import org.kexie.android.danmakux.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;

import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;

@SuppressWarnings("WeakerAccess")
public final class SubtitleParserFactory {

    private SubtitleParserFactory() {
        throw new AssertionError();
    }
    
    /**
     * 从指定格式产生,你可以使用{@link FileUtils#getFileExtension(File)}
     * 来获取文件格式
     *
     * @param ext 文件格式
     * @return 返回解析器
     */
    public static BaseDanmakuParser forFormat(@NonNull @FormatType String ext) {
        Format format = Format.forName(ext);
        if (format == null) {
            throw new IllegalArgumentException();
        }
        return new SubtitleDanmakuParser(format);
    }

    /**
     * 产生一个解析{@link Format#FORMAT_STL}格式的解析器
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
        return parser.load(dataSource);
    }

    public static BaseDanmakuParser forFile(File file) throws FileNotFoundException {
        FormattedDataSource dataSource = FormattedDataSource.load(file);
        return forDataSource(dataSource);
    }
}