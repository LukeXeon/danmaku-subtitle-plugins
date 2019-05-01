package org.kexie.android.danmakux.io;

import org.kexie.android.danmakux.format.Format;
import org.kexie.android.danmakux.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.danmaku.util.IOUtils;

public final class FormattedDataSource implements IDataSource<InputStream> {
    private final String format;
    private final FileInputStream input;

    private FormattedDataSource(String format, FileInputStream input) {
        this.format = format;
        this.input = input;
    }

    public static FormattedDataSource loadFile(File file)
            throws IllegalDataException {
        FormattedDataSource result = null;
        IllegalDataException exception = null;
        if (file != null && file.isFile()) {
            String format = FileUtils.getFileExtension(file);
            if (Format.SUPPORT_FORMATS.contains(format)) {
                try {
                    FileInputStream inputStream = new FileInputStream(file);
                    result = new FormattedDataSource(format, inputStream);
                } catch (FileNotFoundException e) {
                    exception = new IllegalDataException(e);
                }
            }
        }
        if (result == null) {
            throw (exception == null ? new IllegalDataException() : exception);
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
