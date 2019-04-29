package org.kexie.android.danmakux.converter;

import android.support.annotation.NonNull;

import java.util.Arrays;

//用于填充空白部分的字符序列
final class PaddingSequence implements CharSequence {

    PaddingSequence(int length) {
        this.length = Math.max(length, 0);
    }

    private final int length;

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        return ' ';
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new PaddingSequence(Math.max(end - start, 0));
    }

    @NonNull
    @Override
    public String toString() {
        char[] chars = new char[length];
        Arrays.fill(chars, ' ');
        return new String(chars);
    }
}
