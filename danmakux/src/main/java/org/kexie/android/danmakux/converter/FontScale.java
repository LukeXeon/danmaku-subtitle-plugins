package org.kexie.android.danmakux.converter;

import android.content.res.Resources;
import android.text.TextUtils;

import org.kexie.android.danmakux.format.Style;

import java.util.Collection;

final class FontScale {
    final static float MAX_FONT_SIZE = 20;
    final static float MIN_FONT_SIZE = 15;
    final static float MID_FONT_SIZE = (MAX_FONT_SIZE + MIN_FONT_SIZE) / 2f;
    private final static FontScale NORMAL_FONT_SCALE
            = new FontScale(MAX_FONT_SIZE, MIN_FONT_SIZE);
    private final float max;
    private final float min;
    private final float density;

    private static float mid(float f1, float f2) {
        return (f1 + f2) / 2f;
    }

    private FontScale(float max, float min) {
        this.max = max;
        this.min = min;
        this.density = Resources.getSystem().getDisplayMetrics().density;
    }

    int adapt(float value) {
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
        return (int) (value * density + 0.5f);
    }

    static FontScale create(Collection<Style> styles) {
        if (styles.isEmpty()) {
            return FontScale.NORMAL_FONT_SCALE;
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
        return new FontScale(max, min);
    }
}