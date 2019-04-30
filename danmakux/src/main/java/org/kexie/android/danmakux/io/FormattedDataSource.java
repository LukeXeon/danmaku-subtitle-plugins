package org.kexie.android.danmakux.io;

import android.support.annotation.RestrictTo;

import org.kexie.android.danmakux.converter.FormatType;
import org.kexie.android.danmakux.converter.SubtitleParserFactory;
import org.kexie.android.danmakux.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.danmaku.util.IOUtils;

public final class FormattedDataSource implements IDataSource<InputStream> {
    private final String format;
    private final FileInputStream input;

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    private FormattedDataSource(@FormatType String format, FileInputStream input) {
        this.format = format;
        this.input = input;
    }

    public static FormattedDataSource load(File file)
            throws FileNotFoundException {
        FormattedDataSource result = null;
        FileNotFoundException exception = null;
        if (file != null && file.isFile()) {
            String format = FileUtils.getFileExtension(file);
            if (SubtitleParserFactory.SUPPORT_FORMATS.contains(format)) {
                try {
                    FileInputStream inputStream = new FileInputStream(file);
                    result = new FormattedDataSource(format, inputStream);
                } catch (FileNotFoundException e) {
                    exception = e;
                }
            }
        }
        if (result == null) {
            throw (exception == null ? new FileNotFoundException() : exception);
        }
        return result;
    }

    public String getFormat() {
        return format;
    }

    @Override
    public InputStream data() {
        return input;
    }

    @Override
    public void release() {
        IOUtils.closeQuietly(input);
    }
}
