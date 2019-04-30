package org.kexie.android.danmakux.converter;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@StringDef({SubtitleParserFactory.FORMAT_ASS,
        SubtitleParserFactory.FORMAT_SSA,
        SubtitleParserFactory.FORMAT_SCC,
        SubtitleParserFactory.FORMAT_SRT,
        SubtitleParserFactory.FORMAT_STL,
        SubtitleParserFactory.FORMAT_XML,
        SubtitleParserFactory.FORMAT_TTML,
        SubtitleParserFactory.FORMAT_DFXP})
public @interface FormatType {
}
