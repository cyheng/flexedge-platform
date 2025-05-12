// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

import cn.doraro.flexedge.core.ConnException;
import cn.doraro.flexedge.core.conn.ConnPtStream;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class MCMsg {
    public static final int SUBFRAME_HEAD_REQ_4E = 21504;
    public static final int SUBFRAME_HEAD_RESP_4E = 54272;
    public static final int SUBFRAME_HEAD_REQ_3E = 20480;
    public static final int SUBFRAME_HEAD_RESP_3E = 53248;
    static ILogger log;

    static {
        MCMsg.log = LoggerManager.getLogger((Class) MCMsg.class);
    }

    boolean readOk;
    String errInf;

    public MCMsg() {
        this.readOk = false;
        this.errInf = null;
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

    public static final int fromBinHexBytes(final byte[] bs, final int offset, final int byte_n) {
        int v = 0;
        for (int i = offset + byte_n - 1; i >= offset; --i) {
            v <<= 8;
            v |= (bs[i] & 0xFF);
        }
        return v;
    }

    public static final void toBinHexBytes(int v, final byte[] bs, final int offset, final int byte_n) {
        for (int i = offset; i < offset + byte_n; ++i) {
            bs[i] = (byte) (v & 0xFF);
            v >>= 8;
        }
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

    public static int readByteTimeout(final InputStream inputs, final long timeout) throws IOException {
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

    public static void readBytesTimeout(final InputStream inputs, final long timeout, final byte[] buf, final int offset, final int len) throws IOException {
        for (int i = 0; i < len; ++i) {
            final byte b = (byte) readByteTimeout(inputs, timeout);
            buf[offset + i] = b;
        }
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

    public abstract int getSubframeReq();

    public abstract int getSubframeResp();

    public void writeOutAscii(final OutputStream outputs) throws Exception {
        final byte[] bs = new byte[4];
        toAsciiHexBytes(this.getSubframeReq(), bs, 0, 4);
        outputs.write(bs);
    }

    public void writeOutBin(final OutputStream outputs) throws Exception {
        final byte[] bs = new byte[2];
        final int subf_req = this.getSubframeReq();
        bs[0] = (byte) (subf_req >> 8 & 0xFF);
        bs[1] = (byte) (subf_req & 0xFF);
        outputs.write(bs);
    }

    protected boolean readFromBin(final InputStream inputs, final long timeout, final StringBuilder failedr) throws IOException {
        final byte[] bs = new byte[2];
        readBytesTimeout(inputs, timeout, bs, 0, 2);
        final int subf = this.getSubframeResp();
        int tmpv = bs[0] << 8 & 0xFFFF;
        tmpv += (bs[1] & 0xFF);
        if (tmpv != subf) {
            failedr.append("head err");
            return false;
        }
        return true;
    }

    protected boolean readFromAscii(final InputStream inputs, final long timeout, final StringBuilder failedr) throws IOException {
        final byte[] bs = new byte[4];
        readBytesTimeout(inputs, timeout, bs, 0, 4);
        final int subf = this.getSubframeResp();
        final int tmpv = fromAsciiHexBytes(bs, 0, 4);
        if (tmpv != subf) {
            failedr.append("head err");
            return false;
        }
        return true;
    }

    public final boolean readFromStreamAscii(final ConnPtStream cpt, final InputStream inputs, final long timeout) throws ConnException {
        if (!cpt.isConnReady()) {
            throw new ConnException("conn not ready");
        }
        final StringBuilder failedr = new StringBuilder();
        try {
            if (!(this.readOk = this.readFromAscii(inputs, timeout, failedr))) {
                this.errInf = failedr.toString();
            }
            return this.readOk;
        } catch (final IOException ee) {
            this.readOk = false;
            this.errInf = ee.getMessage();
            if (MCMsg.log.isDebugEnabled()) {
                MCMsg.log.debug(this.errInf, (Throwable) ee);
            }
            return false;
        }
    }

    public final boolean readFromStreamBin(final ConnPtStream cpt, final InputStream inputs, final long timeout) throws ConnException {
        if (!cpt.isConnReady()) {
            throw new ConnException("conn not ready");
        }
        final StringBuilder failedr = new StringBuilder();
        try {
            if (!(this.readOk = this.readFromBin(inputs, timeout, failedr))) {
                this.errInf = failedr.toString();
            }
            return this.readOk;
        } catch (final IOException ee) {
            this.readOk = false;
            this.errInf = ee.getMessage();
            if (MCMsg.log.isDebugEnabled()) {
                MCMsg.log.debug(this.errInf, (Throwable) ee);
            }
            return false;
        }
    }

    public final boolean isReadOk() {
        return this.readOk;
    }

    public final String getReadErr() {
        return this.errInf;
    }

    public byte[] toBytesBin() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.writeOutBin(baos);
        return baos.toByteArray();
    }

    public byte[] toBytesAscii() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.writeOutAscii(baos);
        return baos.toByteArray();
    }
}
