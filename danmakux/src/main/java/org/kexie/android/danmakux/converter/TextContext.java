package org.kexie.android.danmakux.converter;

import android.graphics.Color;
import android.text.TextUtils;

import org.kexie.android.danmakux.format.Section;
import org.kexie.android.danmakux.format.Style;

import java.util.Collection;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;


/**
 * 文字样式的实现
 */
final class TextContext {

    private final static float MAX_FONT_SIZE = 20;
    private final static float MIN_FONT_SIZE = 15;
    private final static float MID_FONT_SIZE = (MAX_FONT_SIZE + MIN_FONT_SIZE) / 2f;
    private final float max;
    private final float min;
    private final IDisplayer display;
    private final DanmakuContext context;

    private static String convertColor(String value) {
        String rgb = value.substring(0, 6);
        String a = value.substring(6);
        return a + rgb;
    }

    /**
     * RRGGBBAA to AARRGGBB
     *
     * @param value RRGGBBAA Color String
     * @return AARRGGBB Color int
     */
    private static int parseColor(String value) {
        return Integer.parseInt(convertColor(value), 16);
    }

    private static float mid(float f1, float f2) {
        return (f1 + f2) / 2f;
    }

    private TextContext(
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
        BaseDanmaku item = newItem(style);
        adapt(item, style, section.content);
        return item;
    }

    private BaseDanmaku newItem(Style style) {
        int type = BaseDanmaku.TYPE_FIX_BOTTOM;
        if (style != null && !TextUtils.isEmpty(style.alignment)
                && style.alignment.contains("top")) {
            type = BaseDanmaku.TYPE_FIX_TOP;
        }
        return context.mDanmakuFactory.createDanmaku(type, context);
    }

    private void adapt(BaseDanmaku item, Style style, String text) {
        //分割字符串
        String[] lines = text.split("\\<br[ ]*/\\>", -1);
        int maxLength;
        if (lines.length > 1) {
            int[] lengths = new int[lines.length];
            maxLength = maxLength(lines, lengths);
            //调整对齐
            adaptAlignment(style, lines, lengths, maxLength);
            item.lines = lines;
        } else {
            maxLength = realLength(lines[0]);
        }
        //unit px
        int textSize = adaptSize(style != null
                && style.fontSize != null
                ? Float.parseFloat(style.fontSize)
                : MID_FONT_SIZE);
        adaptText(item, text);
        item.textSize = adaptScreen(maxLength, textSize);
        adaptColor(item, style);
    }

    private static void adaptText(BaseDanmaku item, String text) {
        item.text = text.replaceAll("\\<br[ ]*/\\>", BaseDanmaku.DANMAKU_BR_CHAR);
    }

    //适配颜色
    private static void adaptColor(BaseDanmaku item, Style style) {
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
    }

    // 适配对齐策略,因为弹幕没有自带的对齐策略,
    // 所以这里我们选择在两边填充空白字符实现
    private static void adaptAlignment(Style style,
                                       String[] lines,
                                       int[] lengths,
                                       int maxLength) {
        for (int i = 0; i < lines.length; ++i) {
            int length = lengths[i];
            String line = lines[i];
            int padding = maxLength - length;
            if (padding > 0) {
                boolean match = false;
                StringBuilder builder = new StringBuilder(maxLength);
                if (style != null && !TextUtils.isEmpty(style.alignment)) {
                    if (style.alignment.contains("right")) {
                        PaddingSequence paddingSequence = new PaddingSequence(padding);
                        line = builder.append(line)
                                .append(paddingSequence)
                                .toString();
                        match = true;
                    } else if (style.alignment.contains("left")) {
                        PaddingSequence paddingSequence = new PaddingSequence(padding);
                        line = builder.append(paddingSequence)
                                .append(line)
                                .toString();
                        match = true;
                    }
                }
                if (!match) {
                    PaddingSequence paddingSequence = new PaddingSequence(padding / 2);
                    line = builder.append(paddingSequence)
                            .append(line)
                            .append(paddingSequence)
                            .toString();
                }
                lines[i] = line;
            }
        }
    }

    /**
     * 计算实际显示的最大值
     * 如中文长度为2，英文和数字长度为1
     * @param line   所有的行
     * @return 最长行的长度
     */
    private static int realLength(String line) {
        int trueLength = 0;
        int length = line.length();
        for (int j = 0; j < length; ++j) {
            char ch = line.charAt(j);
            if (TextUtils.isGraphic(ch)) {
                trueLength += isPrintableAscii(ch) ? 1 : 2;
            }
        }
        return trueLength;
    }

    private static int maxLength(String[] lines, int[] lengths) {
        int maxLength = Integer.MIN_VALUE;
        for (int i = 0; i < lines.length; ++i) {
            String line = lines[i] = lines[i].trim();
            int trueLength = realLength(line);
            lengths[i] = trueLength;
            maxLength = Math.max(maxLength, trueLength);
        }
        return maxLength;
    }

    /**
     * 如果超出屏幕大小要重新调整
     *
     * @param maxLength 最长行
     * @param textSize  原本的字体大小px
     * @return 调整过的字体大小
     */
    private int adaptScreen(int maxLength, int textSize) {
        int displayMaxSizePx = Math.max(display.getWidth(), display.getHeight());
        int textSizePx = textSize * maxLength;
        return textSizePx > displayMaxSizePx ? displayMaxSizePx / maxLength : textSize;
    }

    /**
     * @param c 字符
     * @return 是否为可显示的ascii码
     */
    private static boolean isPrintableAscii(final char c) {
        final int asciiFirst = 0x20;
        final int asciiLast = 0x7E;  // included
        return (asciiFirst <= c && c <= asciiLast) || c == '\r' || c == '\n';
    }

    /**
     * 将字体大小映射到
     * {@link TextContext#MAX_FONT_SIZE}和{@link TextContext#MIN_FONT_SIZE}
     * 之间
     *
     * @param value dp
     * @return 所占用的像素
     */
    private int adaptSize(float value) {
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

    /**
     * dpz转px
     *
     * @param value dp
     * @return px
     */
    private int dp2px(float value) {
        return (int) (value * display.getDensity() + 0.5f);
    }

    /**
     * 创建文本样式上下文对象
     *
     * @param styles  样式集合
     * @param display 所使用的屏幕信息
     * @param context 弹幕上下文
     * @return 新的样式上下文对象
     */
    static TextContext
    create(Collection<Style> styles,
           IDisplayer display,
           DanmakuContext context) {
        if (styles.isEmpty()) {
            return new TextContext(MAX_FONT_SIZE, MIN_FONT_SIZE, display, context);
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
        return new TextContext(max, min, display, context);
    }
}
