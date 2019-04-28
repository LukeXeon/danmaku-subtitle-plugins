package org.kexie.android.danmakux.converter;

import org.kexie.android.danmakux.format.Format;

import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;

public final class SubtitleParserFactory {

    private SubtitleParserFactory() {
        throw new AssertionError();
    }

    private static final String TAG = "DanmakuParserFactory";

    public static BaseDanmakuParser create(Format format) {
        return new SubtitleParser(format);
    }
}