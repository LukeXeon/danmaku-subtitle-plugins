package org.kexie.android.danmakux.io;
import android.os.Build;
import android.support.annotation.NonNull;

import org.kexie.android.danmakux.utils.TypeToken;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * 这个类来自JDK1.8
 * 因为在低版本上使用系统带{@link BufferedInputStream}可能会出现问题
 * 这个类会覆盖{@link BufferedInputStream}的行为
 */

@SuppressWarnings({"JavaReflectionMemberAccess","WeakerAccess"})
public class Jdk18BufferedInputStream extends BufferedInputStream {

    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

    public static BufferedInputStream newInstance(InputStream in) {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.N
                ? new BufferedInputStream(in)
                : new Jdk18BufferedInputStream(in);
    }

    private static final AtomicReferenceFieldUpdater<BufferedInputStream, byte[]>
            bufUpdater = newUpdater();

    @SuppressWarnings("unchecked")
    private static AtomicReferenceFieldUpdater<BufferedInputStream, byte[]> newUpdater() {
        AtomicReferenceFieldUpdater<BufferedInputStream, byte[]> bufUpdater;
        try {
            Field field = BufferedInputStream.class.getDeclaredField("bufUpdater");
            field.setAccessible(true);
            if (TypeToken.deepEqualsType(field.getGenericType(),
                    new TypeToken<AtomicReferenceFieldUpdater<BufferedInputStream, byte[]>>() {
                    }.getType())) {
                bufUpdater = (AtomicReferenceFieldUpdater<BufferedInputStream, byte[]>)
                        field.get(null);
            } else {
                throw new IllegalStateException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            bufUpdater = AtomicReferenceFieldUpdater
                    .newUpdater(BufferedInputStream.class, byte[].class, "buf");
        }
        return bufUpdater;
    }

    private InputStream getInIfOpen() throws IOException {
        InputStream input = in;
        if (input == null)
            throw new IOException("Stream closed");
        return input;
    }

    private byte[] getBufIfOpen() throws IOException {
        byte[] buffer = buf;
        if (buffer == null)
            throw new IOException("Stream closed");
        return buffer;
    }

    public Jdk18BufferedInputStream(InputStream in) {
        super(in);
    }

    public Jdk18BufferedInputStream(InputStream in, int size) {
        super(in, size);
    }

    private void fill() throws IOException {
        byte[] buffer = getBufIfOpen();
        if (markpos < 0)
            pos = 0;
        else if (pos >= buffer.length)
            if (markpos > 0) {
                int sz = pos - markpos;
                System.arraycopy(buffer, markpos, buffer, 0, sz);
                pos = sz;
                markpos = 0;
            } else if (buffer.length >= marklimit) {
                markpos = -1;
                pos = 0;
            } else if (buffer.length >= MAX_BUFFER_SIZE) {
                throw new OutOfMemoryError("Required array size too large");
            } else {
                int nsz = (pos <= MAX_BUFFER_SIZE - pos) ?
                        pos * 2 : MAX_BUFFER_SIZE;
                if (nsz > marklimit)
                    nsz = marklimit;
                byte nbuf[] = new byte[nsz];
                System.arraycopy(buffer, 0, nbuf, 0, pos);
                if (!bufUpdater.compareAndSet(this, buffer, nbuf)) {
                    throw new IOException("Stream closed");
                }
                buffer = nbuf;
            }
        count = pos;
        int n = getInIfOpen().read(buffer, pos, buffer.length - pos);
        if (n > 0)
            count = n + pos;
    }

    public synchronized int read() throws IOException {
        if (pos >= count) {
            fill();
            if (pos >= count)
                return -1;
        }
        return getBufIfOpen()[pos++] & 0xff;
    }

    private int read1(byte[] b, int off, int len) throws IOException {
        int avail = count - pos;
        if (avail <= 0) {
            if (len >= getBufIfOpen().length && markpos < 0) {
                return getInIfOpen().read(b, off, len);
            }
            fill();
            avail = count - pos;
            if (avail <= 0) return -1;
        }
        int cnt = (avail < len) ? avail : len;
        System.arraycopy(getBufIfOpen(), pos, b, off, cnt);
        pos += cnt;
        return cnt;
    }

    public synchronized int read(@NonNull byte b[], int off, int len)
            throws IOException {
        getBufIfOpen();
        if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        int n = 0;
        for (; ; ) {
            int nread = read1(b, off + n, len - n);
            if (nread <= 0)
                return (n == 0) ? nread : n;
            n += nread;
            if (n >= len)
                return n;

            InputStream input = in;
            if (input != null && input.available() <= 0)
                return n;
        }
    }

    public synchronized long skip(long n) throws IOException {
        getBufIfOpen();
        if (n <= 0) {
            return 0;
        }
        long avail = count - pos;

        if (avail <= 0) {
            if (markpos < 0)
                return getInIfOpen().skip(n);
            fill();
            avail = count - pos;
            if (avail <= 0)
                return 0;
        }

        long skipped = (avail < n) ? avail : n;
        pos += skipped;
        return skipped;
    }

    public synchronized int available() throws IOException {
        int n = count - pos;
        int avail = getInIfOpen().available();
        return n > (Integer.MAX_VALUE - avail)
                ? Integer.MAX_VALUE
                : n + avail;
    }

    public synchronized void mark(int readlimit) {
        marklimit = readlimit;
        markpos = pos;
    }

    public synchronized void reset() throws IOException {
        getBufIfOpen(); // Cause exception if closed
        if (markpos < 0)
            throw new IOException("Resetting to invalid mark");
        pos = markpos;
    }

    public boolean markSupported() {
        return true;
    }

    public void close() throws IOException {
        byte[] buffer;
        while ((buffer = buf) != null) {
            if (bufUpdater.compareAndSet(this, buffer, null)) {
                InputStream input = in;
                in = null;
                if (input != null)
                    input.close();
                return;
            }
        }
    }
}