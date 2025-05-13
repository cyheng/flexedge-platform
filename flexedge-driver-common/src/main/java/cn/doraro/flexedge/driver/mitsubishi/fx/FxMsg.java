

package cn.doraro.flexedge.driver.mitsubishi.fx;

import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;

import java.io.IOException;
import java.io.InputStream;

public abstract class FxMsg {
    public static final byte ENQ = 5;
    public static final byte STX = 2;
    public static final byte ETX = 3;
    public static final byte ACK = 6;
    public static final byte NCK = 21;
    public static final byte CMD_BR = 48;
    public static final byte CMD_BW = 49;
    public static final byte CMD_FORCE_ON = 55;
    public static final byte CMD_FORCE_OFF = 56;
    public static ILogger log;
    public static ILogger log_w;

    static {
        FxMsg.log = LoggerManager.getLogger((Class) FxMsg.class);
        FxMsg.log_w = LoggerManager.getLogger(FxMsg.class.getCanonicalName() + "_w");
    }

    boolean bExt;

    public FxMsg() {
        this.bExt = false;
    }

    protected static final int calCRC(final byte[] bs, final int offset, final int len) {
        int r = 0;
        for (int i = 0; i < len; ++i) {
            r += (bs[i + offset] & 0xFF);
        }
        return r & 0xFF;
    }

    protected static final byte toAsciiHexByte(final int f) {
        if (f < 10) {
            return (byte) (48 + f);
        }
        return (byte) (65 + f - 10);
    }

    protected static final int fromAsciiHexByte(final byte b) {
        if (b < 65) {
            return b - 48;
        }
        return b - 65 + 10;
    }

    public static final void toAsciiHexBytes(final int v, final byte[] bs, final int offset, final int byte_n) {
        for (int i = 0; i < byte_n; ++i) {
            final int tmpv = v >> 4 * (byte_n - i - 1) & 0xF;
            bs[offset + i] = (byte) ((tmpv < 10) ? (48 + tmpv) : (65 + tmpv - 10));
        }
    }

    public static final int fromAsciiHexBytes(final byte[] bs, final int offset, final int byte_n) {
        int r = 0;
        for (int i = 0; i < byte_n; ++i) {
            final byte b = bs[offset + i];
            final int bv = (b < 65) ? (b - 48) : (b - 65 + 10);
            r += bv;
            if (byte_n - i - 1 > 0) {
                r <<= 4;
            }
        }
        return r;
    }

    protected static void checkStreamLenTimeout(final InputStream inputs, final int len, final long timeout) throws IOException {
        long lastt = System.currentTimeMillis();
        int lastlen = inputs.available();
        long curt;
        while ((curt = System.currentTimeMillis()) - lastt < timeout) {
            final int curlen = inputs.available();
            if (curlen >= len) {
                return;
            }
            if (curlen > lastlen) {
                lastlen = curlen;
                lastt = curt;
            } else {
                try {
                    Thread.sleep(1L);
                } catch (final Exception ex) {
                }
            }
        }
        throw new IOException("time out");
    }

    public static int readCharTimeout(final InputStream inputs, final long timeout) throws IOException {
        final long curt = System.currentTimeMillis();
        while (System.currentTimeMillis() - curt < timeout) {
            if (inputs.available() >= 1) {
                return inputs.read();
            }
            try {
                Thread.sleep(1L);
            } catch (final Exception ex) {
            }
        }
        throw new IOException("time out " + timeout + "ms");
    }

    public static void clearInputStream(final InputStream inputs, final long timeout) throws IOException {
        int lastav = inputs.available();
        long curt;
        int curav;
        for (long lastt = curt = System.currentTimeMillis(); (curt = System.currentTimeMillis()) - lastt < timeout; lastt = curt, lastav = curav) {
            try {
                Thread.sleep(1L);
            } catch (final Exception ex) {
            }
            curav = inputs.available();
            if (curav != lastav) {
            }
        }
        if (lastav > 0) {
            inputs.skip(lastav);
        }
    }

    public boolean isExtCmd() {
        return this.bExt;
    }

    public FxMsg asExt(final boolean b_ext) {
        this.bExt = b_ext;
        return this;
    }

    public abstract byte[] toBytes();
}
