

package cn.doraro.flexedge.driver.s7.ppi;

import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;

import java.io.IOException;
import java.io.InputStream;

public abstract class PPIMsg {
    public static final short PK_ACK = 229;
    public static final short SD_REQ_CONFIRM = 16;
    public static final short SD_REQ = 104;
    public static final short ED = 22;
    public static ILogger log;
    public static ILogger log_w;

    static {
        PPIMsg.log = LoggerManager.getLogger((Class) PPIMsg.class);
        PPIMsg.log_w = LoggerManager.getLogger(PPIMsg.class.getCanonicalName() + "_w");
    }

    protected static byte calChkSum(final byte[] bs, final int idx, final int num) {
        int r = 0;
        for (int i = 0; i < num; ++i) {
            r += (bs[idx + i] & 0xFF);
        }
        return (byte) (r & 0xFF);
    }

    private static void checkStreamLenTimeout(final InputStream inputs, final int len, final long timeout) throws IOException {
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

    public static byte[] readFromStream(final InputStream inputs, final long timeout) throws IOException {
        int st = 0;
        int le = 0;
        byte[] ret = null;
        final long curt = System.currentTimeMillis();
        while (st >= 4 || System.currentTimeMillis() - curt <= timeout * 2L) {
            switch (st) {
                case 0: {
                    int c;
                    do {
                        c = readCharTimeout(inputs, timeout);
                        if (c == 229) {
                            return new byte[]{-27};
                        }
                    } while (c != 104);
                    st = 1;
                    continue;
                }
                case 1: {
                    le = readCharTimeout(inputs, timeout);
                    st = 2;
                    continue;
                }
                case 2: {
                    final int ler = readCharTimeout(inputs, timeout);
                    if (le != ler) {
                        st = 0;
                        continue;
                    }
                    st = 3;
                    continue;
                }
                case 3: {
                    final int c = readCharTimeout(inputs, timeout);
                    if (c != 104) {
                        st = 0;
                        continue;
                    }
                    st = 4;
                    continue;
                }
                case 4: {
                    ret = new byte[le + 6];
                    ret[0] = (ret[3] = 104);
                    ret[1] = (ret[2] = (byte) le);
                    checkStreamLenTimeout(inputs, le + 2, timeout);
                    inputs.read(ret, 4, le + 2);
                    if (ret[le + 4] != calChkSum(ret, 4, le)) {
                        return null;
                    }
                    if (ret[le + 5] != 22) {
                        return null;
                    }
                    return ret;
                }
            }
        }
        throw new IOException("time out");
    }

    public static PPIMsgReq createReqReadByAddr(final boolean bwrite, final short da, final short sa, final String addr, final UAVal.ValTP vtp) {
        if (Convert.isNullOrEmpty(addr)) {
            return null;
        }
        final StringBuilder failedr = new StringBuilder();
        final PPIAddr ppiaddr = PPIAddr.parsePPIAddr(addr, vtp, failedr);
        if (ppiaddr == null) {
            return null;
        }
        final PPIMsgReqR ret = new PPIMsgReqR();
        ret.sa = sa;
        ret.da = da;
        ret.memTp = ppiaddr.getMemTp();
        ret.offsetBytes = ppiaddr.getOffsetBytes();
        ret.inBit = ppiaddr.getInBits();
        return ret;
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

    protected abstract short getStartD();

    protected short getEndD() {
        return 22;
    }

    public abstract byte[] toBytes();
}
