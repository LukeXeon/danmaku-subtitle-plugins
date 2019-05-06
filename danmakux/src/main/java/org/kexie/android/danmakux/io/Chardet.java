package org.kexie.android.danmakux.io;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public final class Chardet {
    private Chardet() {
        throw new AssertionError();
    }

    public static Charset bestGuess(InputStream input) throws IOException {
        if (input.markSupported()) {
            throw new IOException();
        }
        input.mark(0);
        byte[] buffer = new byte[4096];
        UniversalDetector detector = new UniversalDetector(null);
        int length;
        while ((length = input.read(buffer)) > 0 && !detector.isDone()) {
            detector.handleData(buffer, 0, length);
        }
        detector.dataEnd();
        input.reset();
        String charset = detector.getDetectedCharset();
        return charset == null ? Charset.defaultCharset() : Charset.forName(charset);
    }
}
