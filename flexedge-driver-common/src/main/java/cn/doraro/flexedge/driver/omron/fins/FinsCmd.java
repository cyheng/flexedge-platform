

package cn.doraro.flexedge.driver.omron.fins;

import cn.doraro.flexedge.core.util.IBSOutput;
import cn.doraro.flexedge.core.util.xmldata.DataUtil;

import java.io.IOException;
import java.io.InputStream;

public abstract class FinsCmd {
    private static final byte[] HEADER;

    static {
        HEADER = new byte[]{70, 73, 78, 83};
    }

    protected FinsMode mode;
    short dna;
    short da1;
    short da2;
    short sna;
    short sa1;
    short sa2;

    public FinsCmd(final FinsMode fins_mode) {
        this.dna = -1;
        this.da1 = -1;
        this.da2 = -1;
        this.sna = -1;
        this.sa1 = -1;
        this.sa2 = -1;
        this.mode = fins_mode;
    }

    protected static final byte[] int2bytes(final int i) {
        return DataUtil.intToBytes(i);
    }

    protected static final void int2byte3(int i, final byte[] bytes, final int offset) {
        bytes[offset + 3] = (byte) (i & 0xFF);
        i >>>= 8;
        bytes[offset + 2] = (byte) (i & 0xFF);
        i >>>= 8;
        bytes[offset + 1] = (byte) (i & 0xFF);
    }

    protected static final byte[] short2bytes(final short i) {
        return DataUtil.shortToBytes(i);
    }

    protected static final void short2bytes(final short i, final byte[] bs, final int offset) {
        DataUtil.shortToBytes(i, bs, offset);
    }

    public static byte[] Handshake_createReq(final short client_pc_last_ip) {
        final byte[] rets = new byte[20];
        rets[0] = 70;
        rets[1] = 73;
        rets[2] = 78;
        rets[3] = 83;
        for (int i = 4; i < 20; ++i) {
            rets[i] = 0;
        }
        rets[7] = 12;
        rets[19] = (byte) client_pc_last_ip;
        return rets;
    }

    public static Short Handshake_checkResp(final InputStream inputs, final short client_pc_last_ip, final long timeout) throws IOException {
        checkStreamLenTimeout(inputs, 24, timeout);
        final byte[] bs = new byte[24];
        inputs.read(bs);
        if (bs[0] != 70 || bs[1] != 73 || bs[2] != 78 || bs[3] != 83) {
            return null;
        }
        if (bs[7] != 16) {
            return null;
        }
        if ((0xFF & bs[19]) != client_pc_last_ip) {
            return null;
        }
        return (short) (0xFF & bs[23]);
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

    public FinsCmd asDest(final short dna, final short da1, final short da2) {
        this.dna = dna;
        this.da1 = da1;
        this.da2 = da2;
        return this;
    }

    public FinsCmd asSor(final short sna, final short sa1, final short sa2) {
        this.sna = sna;
        this.sa1 = sa1;
        this.sa2 = sa2;
        return this;
    }

    public void writeOut(final IBSOutput outputs) throws IOException {
        final int len = 20;
        byte[] bs = int2bytes(len);
        outputs.write(bs);
        bs = int2bytes(2);
        outputs.write(bs);
        final byte[] array = bs;
        final int n = 0;
        final byte[] array2 = bs;
        final int n2 = 1;
        final byte[] array3 = bs;
        final int n3 = 2;
        final byte[] array4 = bs;
        final int n4 = 3;
        final byte b = 0;
        array3[n3] = (array4[n4] = b);
        array[n] = (array2[n2] = b);
        outputs.write(bs);
        bs = new byte[]{(byte) this.getICF(), 0, 2, (byte) this.dna, (byte) this.da1, (byte) this.da2, (byte) this.sna, (byte) this.sa1, (byte) this.sa2, 0, 0, 0};
        outputs.write(bs);
        this.writeParam(outputs);
    }

    protected abstract short getICF();

    protected abstract short getMRC();

    protected abstract short getSRC();

    protected abstract int getParamBytesNum();

    protected abstract void writeParam(final IBSOutput p0);
}
