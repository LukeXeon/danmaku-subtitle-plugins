package org.kexie.android.danmakux.converter;

import android.content.res.Resources;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import org.kexie.android.danmakux.format.Section;
import org.kexie.android.danmakux.format.Style;

import java.util.Arrays;
import java.util.Collection;

import master.flame.danmaku.danmaku.model.BaseDanmaku;

final class TextStyleAdapter {
    private final static float MAX_FONT_SIZE = 20;
    private final static float MIN_FONT_SIZE = 15;
    private final static float MID_FONT_SIZE = (MAX_FONT_SIZE + MIN_FONT_SIZE) / 2f;
    private final static TextStyleAdapter NORMAL_FONT_SCALE
            = new TextStyleAdapter(MAX_FONT_SIZE, MIN_FONT_SIZE);
    private final float max;
    private final float min;
    private final DisplayMetrics displayMetrics;

    //RRGGBBAA to AARRGGBB
    private static int parseColor(String value) {
        String rgb = value.substring(0, 6);
        String a = value.substring(6);
        return Integer.parseInt(a + rgb, 16);
    }

    private static float mid(float f1, float f2) {
        return (f1 + f2) / 2f;
    }

    private TextStyleAdapter(float max, float min) {
        this.max = max;
        this.min = min;
        this.displayMetrics = Resources.getSystem().getDisplayMetrics();
    }

    void adapt(BaseDanmaku item, Section section) {
        Style style = section.style;
        if (style == null) {
            item.textColor = Color.WHITE;
            item.textShadowColor = Color.BLACK;
            item.textSize = dp2px(MID_FONT_SIZE);
            return;
        }
        item.textColor = style.color != null
                ? parseColor(style.color)
                : Color.WHITE;
        item.textShadowColor = style.backgroundColor != null
                ? parseColor(style.backgroundColor)
                : Color.BLACK;
        adaptText(item, style, section.content);
    }

    @SuppressWarnings("unchecked")
    private void adaptText(BaseDanmaku item, Style style, String text) {
        float textSize = mappingSize(style.fontSize != null
                ? Float.parseFloat(style.fontSize)
                : MID_FONT_SIZE);
        text = text.replaceAll("\\<br[ ]*\\>", BaseDanmaku.DANMAKU_BR_CHAR);
        item.text = text;
        //分割字符串
        String[] lines = text.split(BaseDanmaku.DANMAKU_BR_CHAR, -1);
        //计算实际显示的最大值
        //中文长度为2，英文长度为1
        int maxLength = Integer.MIN_VALUE;
        int[] lengths = new int[lines.length];
        for (int i = 0; i < lines.length; ++i) {
            String line = lines[i];
            int trueLength = 0;
            int length = line.length();
            for (int j = 0; j < length; ++j) {
                char ch = line.charAt(i);
                if (TextUtils.isGraphic(ch)) {
                    trueLength += isPrintableAscii(ch) ? 1 : 2;
                }
            }
            lengths[i] = trueLength;
            maxLength = Math.max(maxLength, trueLength);
        }
        //超出屏幕大小，要重新调整
        int minWidth = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        int maxPx = dp2px(textSize) * maxLength;
        if (maxPx > minWidth) {
            textSize = (float) minWidth / maxLength;
        }
        //调整对齐
        for (int i = 0; i < lines.length; ++i) {
            int length = lengths[i];
            String line = lines[i];
            int padding = maxLength - length;
            if (padding > 0) {
                StringBuilder builder = new StringBuilder(maxLength);
                if (!TextUtils.isEmpty(style.textAlign)
                        && style.textAlign.contains("right")) {
                    char[] chars = new char[padding];
                    Arrays.fill(chars, ' ');
                    builder.append(line)
                            .append(chars);
                } else if (!TextUtils.isEmpty(style.textAlign)
                        && style.textAlign.contains("left")) {
                    char[] chars = new char[padding];
                    Arrays.fill(chars, ' ');
                    builder.append(chars)
                            .append(line);
                } else {
                    padding /= 2;
                    char[] chars = new char[padding];
                    Arrays.fill(chars, ' ');
                    builder.append(chars)
                            .append(line)
                            .append(chars);
                    line = builder.toString();
                }
            }
            lines[i] = line;
        }
        item.lines = lines;
        item.textSize = textSize;
    }

    private static boolean isPrintableAscii(final char c) {
        final int asciiFirst = 0x20;
        final int asciiLast = 0x7E;  // included
        return (asciiFirst <= c && c <= asciiLast) || c == '\r' || c == '\n';
    }

    private int mappingSize(float value) {
        float mid = mid(min, max);
        if (value > mid) {
            float delta = value - mid;
            float p = delta / (max - mid);
            value = MID_FONT_SIZE + p * (MAX_FONT_SIZE - MID_FONT_SIZE);
        } else if (value < mid) {
            float delta = mid - value;
            float p = delta / (mid - min);
            value = MID_FONT_SIZE - p * (MID_FONT_SIZE - MIN_FONT_SIZE);
        } else {
            value = MID_FONT_SIZE;
        }
        return dp2px(value);
    }

    private int dp2px(float value) {
        return (int) (value * displayMetrics.density + 0.5f);
    }

    static TextStyleAdapter create(Collection<Style> styles) {
        if (styles.isEmpty()) {
            return TextStyleAdapter.NORMAL_FONT_SCALE;
        }
        float min = Float.MIN_VALUE, max = Float.MIN_VALUE;
        for (Style style : styles) {
            if (!TextUtils.isEmpty(style.fontSize)
                    && TextUtils.isDigitsOnly(style.fontSize)) {
                float size = Float.parseFloat(style.fontSize);
                max = Math.max(max, size);
                min = Math.min(min, size);
            }
        }
        return new TextStyleAdapter(max, min);
    }
}
