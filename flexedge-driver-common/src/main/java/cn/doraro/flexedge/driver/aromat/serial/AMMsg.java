// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.aromat.serial;

import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;

import java.io.IOException;
import java.io.InputStream;

public abstract class AMMsg {
    public static ILogger log;
    public static ILogger log_w;

    static {
        AMMsg.log = LoggerManager.getLogger((Class) AMMsg.class);
        AMMsg.log_w = LoggerManager.getLogger(AMMsg.class.getCanonicalName() + "_w");
    }

    char head;
    int plcAddr;

    public AMMsg() {
        this.head = '%';
        this.plcAddr = 0;
    }

    protected static String byte2hex(final int b, final boolean fix_len2) {
        String s = Integer.toHexString(b).toUpperCase();
        if (fix_len2 && s.length() == 1) {
            s = "0" + s;
        }
        return s;
    }

    protected static short hex2byte(final String hex) {
        return Short.parseShort(hex, 16);
    }

    public static byte[] hex2bytes(final String hex) {
        final int len = hex.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("invalid hex str,length must be odd");
        }
        final byte[] bs = new byte[len / 2];
        for (int i = 0; i < bs.length; ++i) {
            final int idx = i * 2;
            bs[i] = (byte) Short.parseShort(hex.substring(idx, idx + 2), 16);
        }
        return bs;
    }

    public static String byte_to_bcd2(final int b) {
        return "" + b / 10 + b % 10;
    }

    public static String byte_to_bcd4(int b) {
        final StringBuilder sb = new StringBuilder();
        sb.append(b / 1000);
        b %= 1000;
        sb.append(b / 100);
        b %= 100;
        sb.append(b / 10);
        sb.append(b % 10);
        return sb.toString();
    }

    public static int bcd2_to_byte(final char h, final char l) {
        return (h - '0') * 10 + (l - '0');
    }

    public static String calBCC(final String str) {
        final byte[] bs = str.getBytes();
        return calBCC(bs);
    }

    public static String calBCC(final byte[] bs) {
        int chksum = 0;
        for (final byte b : bs) {
            chksum ^= b;
        }
        final int v = chksum & 0xFF;
        final String r = Integer.toHexString(v).toUpperCase();
        if (r.length() == 1) {
            return "0" + r;
        }
        return r;
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

    public AMMsg asHead(final char head) {
        if (head != '%' && head != '<') {
            throw new IllegalArgumentException("invalid head,it must be % or <");
        }
        this.head = head;
        return this;
    }

    public AMMsg asPlcAddr(final int plc_addr) {
        if (plc_addr <= 0 || plc_addr > 32) {
            throw new IllegalArgumentException("plc unit must in 1-32");
        }
        this.plcAddr = plc_addr;
        return this;
    }
}
