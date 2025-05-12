// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink;

import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;

import java.io.IOException;
import java.io.InputStream;

public abstract class HLMsg {
    public static ILogger log;
    public static ILogger log_w;

    static {
        HLMsg.log = LoggerManager.getLogger((Class) HLMsg.class);
        HLMsg.log_w = LoggerManager.getLogger(HLMsg.class.getCanonicalName() + "_w");
    }

    int plcUnit;

    public HLMsg() {
        this.plcUnit = 0;
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

    public static int bcd2_to_byte(final char h, final char l) {
        return (h - '0') * 10 + (l - '0');
    }

    public static String calFCS(final String str) {
        final byte[] bs = str.getBytes();
        return calFCS(bs);
    }

    public static String calFCS(final byte[] bs) {
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

    public HLMsg asPlcUnit(final int plc_unit) {
        if (plc_unit < 0 || plc_unit > 31) {
            throw new IllegalArgumentException("plc unit must in 0-31");
        }
        this.plcUnit = plc_unit;
        return this;
    }

    public abstract String getHeadCode();
}
