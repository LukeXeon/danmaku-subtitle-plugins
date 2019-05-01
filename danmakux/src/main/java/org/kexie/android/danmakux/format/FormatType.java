package org.kexie.android.danmakux.format;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@StringDef({Format.FORMAT_ASS,
        Format.FORMAT_SSA,
        Format.FORMAT_SCC,
        Format.FORMAT_SRT,
        Format.FORMAT_STL,
        Format.FORMAT_XML,
        Format.FORMAT_TTML,
        Format.FORMAT_DFXP})
public @interface FormatType {
}
