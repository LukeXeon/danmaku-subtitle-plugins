package org.kexie.android.danmakux.converter;

import android.support.annotation.RestrictTo;

import java.io.FileInputStream;
import java.io.InputStream;

import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.danmaku.util.IOUtils;

public final class FormattedDataSource implements IDataSource<InputStream> {
    private final String format;
    private final FileInputStream input;

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public FormattedDataSource(String format, FileInputStream input) {
        this.format = format;
        this.input = input;
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
