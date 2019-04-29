package org.kexie.android.danmakux.converter;

import android.graphics.Color;
import android.text.TextUtils;

import org.kexie.android.danmakux.format.Section;
import org.kexie.android.danmakux.format.Style;

import java.util.Arrays;
import java.util.Collection;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;


//文字样式的实现
final class TextStyleContext {

    private final static float MAX_FONT_SIZE = 20;
    private final static float MIN_FONT_SIZE = 15;
    private final static float MID_FONT_SIZE = (MAX_FONT_SIZE + MIN_FONT_SIZE) / 2f;
    private final float max;
    private final float min;
    private final IDisplayer display;
    private final DanmakuContext context;

    //RRGGBBAA to AARRGGBB
    private static int parseColor(String value) {
        String rgb = value.substring(0, 6);
        String a = value.substring(6);
        return Integer.parseInt(a + rgb, 16);
    }

    private static float mid(float f1, float f2) {
        return (f1 + f2) / 2f;
    }

    private TextStyleContext(
            float max,
            float min,
            IDisplayer display,
            DanmakuContext context) {
        this.max = max;
        this.min = min;
        this.display = display;
        this.context = context;
    }

    BaseDanmaku newDanmaku(Section section) {
        Style style = section.style;
        BaseDanmaku item = adaptNew(style, section.content);
        if (style == null) {
            item.textColor = Color.WHITE;
            item.textShadowColor = Color.BLACK;
        } else {
            item.textColor = style.color != null
                    ? parseColor(style.color)
                    : Color.WHITE;
            item.textShadowColor = style.backgroundColor != null
                    ? parseColor(style.backgroundColor)
                    : Color.BLACK;
        }
        return item;
    }

    private BaseDanmaku newItem(boolean nonNull, Style style) {
        int type;
        if (nonNull && !TextUtils.isEmpty(style.alignment)) {
            if (style.alignment.contains("bottom")) {
                type = BaseDanmaku.TYPE_FIX_BOTTOM;
            } else if (style.alignment.contains("top")) {
                type = BaseDanmaku.TYPE_FIX_TOP;
            } else {
                type = BaseDanmaku.TYPE_FIX_BOTTOM;
            }
        } else {
            type = BaseDanmaku.TYPE_FIX_BOTTOM;
        }
        return context.mDanmakuFactory.createDanmaku(type, context);
    }

    private BaseDanmaku adaptNew(Style style, String text) {
        boolean nonNull;
        //unit px
        int textSize = mappingToPxRange((nonNull = style != null)
                && style.fontSize != null
                ? Float.parseFloat(style.fontSize)
                : MID_FONT_SIZE);
        text = text.replaceAll("\\<br[ ]*/\\>", BaseDanmaku.DANMAKU_BR_CHAR);
        BaseDanmaku item = newItem(nonNull, style);
        item.text = text;
        //分割字符串
        String[] lines = text.split(BaseDanmaku.DANMAKU_BR_CHAR, -1);
        int[] lengths = new int[lines.length];
        //计算实际显示的最大值
        //中文长度为2，英文长度为1
        int maxLength = maxLineLength(lines, lengths);
        //调整对齐
        adaptAlignment(nonNull, style, lines, lengths, maxLength);
        item.lines = lines;
        item.textSize = adaptScreen(maxLength, textSize);
        return item;
    }

    private static void adaptAlignment(
            boolean nonNull,
            Style style,
            String[] lines,
            int[] lengths,
            int maxLength) {
        for (int i = 0; i < lines.length; ++i) {
            int length = lengths[i];
            String line = lines[i];
            int padding = maxLength - length;
            if (padding > 0) {
                StringBuilder builder = new StringBuilder(maxLength);
                boolean bool = nonNull && !TextUtils.isEmpty(style.alignment);
                if (bool && style.alignment.contains("right")) {
                    char[] chars = new char[padding];
                    Arrays.fill(chars, ' ');
                    line = builder.append(line)
                            .append(chars)
                            .toString();
                } else if (bool && style.alignment.contains("left")) {
                    char[] chars = new char[padding];
                    Arrays.fill(chars, ' ');
                    line = builder.append(chars)
                            .append(line)
                            .toString();
                } else {
                    padding /= 2;
                    char[] chars = new char[padding];
                    Arrays.fill(chars, ' ');
                    line = builder.append(chars)
                            .append(line)
                            .append(chars)
                            .toString();
                }
                lines[i] = line;
            }
        }
    }

    private static int maxLineLength(String[] lines, int[] lengths) {
        int maxLength = Integer.MIN_VALUE;
        for (int i = 0; i < lines.length; ++i) {
            String line = lines[i] = lines[i].trim();
            int trueLength = 0;
            int length = line.length();
            for (int j = 0; j < length; ++j) {
                char ch = line.charAt(j);
                if (TextUtils.isGraphic(ch)) {
                    trueLength += isPrintableAscii(ch) ? 1 : 2;
                }
            }
            lengths[i] = trueLength;
            maxLength = Math.max(maxLength, trueLength);
        }
        return maxLength;
    }

    //如果超出屏幕大小要重新调整
    private int adaptScreen(int maxLength, int textSize) {
        int displayMaxSizePx = Math.max(display.getWidth(), display.getHeight());
        int textSizePx = textSize * maxLength;
        return textSizePx > displayMaxSizePx ? displayMaxSizePx / maxLength : textSize;
    }

    private static boolean isPrintableAscii(final char c) {
        final int asciiFirst = 0x20;
        final int asciiLast = 0x7E;  // included
        return (asciiFirst <= c && c <= asciiLast) || c == '\r' || c == '\n';
    }

    private int mappingToPxRange(float value) {
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
        return (int) (value * display.getDensity() + 0.5f);
    }

    static TextStyleContext
    create(Collection<Style> styles,
           IDisplayer display,
           DanmakuContext context) {
        if (styles.isEmpty()) {
            return new TextStyleContext(MAX_FONT_SIZE, MIN_FONT_SIZE, display, context);
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
        return new TextStyleContext(max, min, display, context);
    }
}
