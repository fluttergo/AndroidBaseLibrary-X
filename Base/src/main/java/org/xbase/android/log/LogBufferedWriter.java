package org.xbase.android.log;

import java.io.IOException;
import java.io.Writer;

public class LogBufferedWriter extends Writer {

    // ===========================================================
    // Constants
    // ===========================================================

    private static final int DEFAULT_BUF_SIZE = 1024 * 4;

    // ===========================================================
    // Fields
    // ===========================================================

    private final int        mBufSize;

    private Writer           mOut;
    private char[]           mBuf;
    private int              mPos;

    // ===========================================================
    // Constructors
    // ===========================================================

    public LogBufferedWriter(Writer out) {
        this(out, DEFAULT_BUF_SIZE);
    }

    public LogBufferedWriter(Writer out, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size <= 0");
        }
        this.mOut = out;
        this.mBuf = new char[size];
        this.mBufSize = size;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public void close() throws IOException {
        synchronized (lock) {
            if (mOut == null) {
                return;
            }
            try {
                flush();
            } finally {
                mOut.close();
                mOut = null;
                mBuf = null;
            }
        }
    }

    @Override
    public void flush() throws IOException {
        synchronized (lock) {
            flushBuffer();
            mOut.flush();
        }
    }

    @Override
    public void write(char[] array, int off, int len) throws IOException {
        synchronized (lock) {
            checkNotClosed();
            if (array == null || array.length == 0){
                return;
            }
            if (mBuf == null) {
                throw new NullPointerException("Buffer is null!");
            }
            int bufLen = mBufSize;
            if ((off < 0) || (off > bufLen) || (len < 0) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            }
            if (mPos + len >= mBufSize || (off + len) > bufLen) {
                flush();
                mOut.write(array, off, len);
                mOut.flush();
                return;
            }
            System.arraycopy(array, off, mBuf, mPos, len);
            mPos += len;
        }
    }

    // ===========================================================
    // Methods
    // ===========================================================

    private void checkNotClosed() throws IOException {
        if (isClosed()) {
            throw new IOException("LogBufferedWriter is closed");
        }
    }

    private boolean isClosed() {
        return mOut == null;
    }

    /**
     * Flushes the internal buffer.
     */
    private void flushBuffer() throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (mPos == 0) {
                return;
            }
            if (mPos > 0) {
                System.out.println("flushBuffer() mPos = " + mPos);
                mOut.write(mBuf, 0, mPos);
            }
            mPos = 0;
        }
    }

    private void ensureOpen() throws IOException {
        if (mOut == null)
            throw new IOException("Stream closed");
    }

    // ===========================================================
    // Native Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

}
