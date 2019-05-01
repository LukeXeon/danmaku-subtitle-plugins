package org.kexie.android.danmakux.converter;

import android.support.annotation.NonNull;

import org.kexie.android.danmakux.format.Format;
import org.kexie.android.danmakux.format.FormatType;
import org.kexie.android.danmakux.format.STLFormat;
import org.kexie.android.danmakux.io.FormattedDataSource;
import org.kexie.android.danmakux.utils.FileUtils;

import java.io.File;

import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;

/**
 * 解析器的工厂类
 */
@SuppressWarnings("WeakerAccess")
public final class SubtitleParserFactory {

    private SubtitleParserFactory() {
        throw new AssertionError();
    }

    /**
     * 从指定格式产生,你可以使用{@link FileUtils#getFileExtension(File)}
     * 来获取文件格式
     * @param ext 文件格式
     * @return 返回解析器(未加载数据)
     */
    public static BaseDanmakuParser forFormat(@NonNull @FormatType String ext) {
        Format format = Format.forName(ext);
        if (format == null) {
            throw new IllegalArgumentException();
        }
        return new SubtitleDanmakuParser(format);
    }

    /**
     * 产生一个解析{@link Format#FORMAT_STL}格式的解析器(未加载数据)
     *
     * @param frameRate 帧率
     * @return STL格式解析器
     */
    public static BaseDanmakuParser forFrameRate(float frameRate) {
        STLFormat format = new STLFormat();
        format.setFrameRate(frameRate);
        return new SubtitleDanmakuParser(format);
    }

    /**
     * 从一个格式化的数据原获得已经加载数据原的解析器
     * @param dataSource 格式化的数据源
     * @return 已经加载该数据源的解析器
     */
    public static BaseDanmakuParser forDataSource(FormattedDataSource dataSource) {
        BaseDanmakuParser parser = forFormat(dataSource.getFormat());
        return parser.load(dataSource);
    }

    /**
     * 从一个文件的数据原获得已经加载数据原的解析器
     * @param file 指定文件
     * @return 已经加载该数据源的解析器
     * @throws IllegalDataException 如果没有找到合适的文件
     */
    public static BaseDanmakuParser forFile(File file) throws IllegalDataException {
        FormattedDataSource dataSource = FormattedDataSource.loadFile(file);
        return forDataSource(dataSource);
    }
}