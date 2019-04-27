package org.kexie.android.danmakux.format;

import android.support.annotation.NonNull;

public class Caption {

    public Style style;

    public Time start;
    public Time end;

    /**
     * Raw content, before cleaning up templates and markup.
     */
    public String rawContent = "";
    /**
     * Cleaned-up subtitle content.
     */
    public String content = "";

    @NonNull
    @Override
    public String toString() {
        return "Caption{" +
                start + ".." + end +
                ", " + (style != null ? style.iD : null) + ", " + content +
                '}';
    }
}
