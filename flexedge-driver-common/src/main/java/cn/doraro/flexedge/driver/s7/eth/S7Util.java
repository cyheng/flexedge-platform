

package cn.doraro.flexedge.driver.s7.eth;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

public class S7Util {
    public static boolean getBit(final byte[] bs, final int idx, int bit_in_byte) {
        if (bit_in_byte < 0) {
            bit_in_byte = 0;
        }
        if (bit_in_byte > 7) {
            bit_in_byte = 7;
        }
        final int v = bs[idx] & 0xFF;
        return (v & 1 << bit_in_byte) != 0x0;
    }

    public static int getUInt16(final byte[] bs, final int idx) {
        final int hi = bs[idx] & 0xFF;
        final int lo = bs[idx + 1] & 0xFF;
        return (hi << 8) + lo;
    }

    public static short getInt16(final byte[] bs, final int idx) {
        final int hi = bs[idx];
        final int lo = bs[idx + 1] & 0xFF;
        return (short) ((hi << 8) + lo);
    }

    public static long getUint32(final byte[] bs, final int idx) {
        long v = bs[idx] & 0xFF;
        v <<= 8;
        v += (bs[idx + 1] & 0xFF);
        v <<= 8;
        v += (bs[idx + 2] & 0xFF);
        v <<= 8;
        v += (bs[idx + 3] & 0xFF);
        return v;
    }

    public static int getInt32(final byte[] bs, final int idx) {
        int v = bs[idx];
        v <<= 8;
        v += (bs[idx + 1] & 0xFF);
        v <<= 8;
        v += (bs[idx + 2] & 0xFF);
        v <<= 8;
        v += (bs[idx + 3] & 0xFF);
        return v;
    }

    public static float getFloatAt(final byte[] bs, final int idx) {
        final int iv = getInt32(bs, idx);
        return Float.intBitsToFloat(iv);
    }

    public static String getStr(final byte[] bs, final int idx, final int len) {
        try {
            return new String(bs, idx, len, "UTF-8");
        } catch (final UnsupportedEncodingException ex) {
            return "";
        }
    }

    public static String getPrintableStr(final byte[] bs, final int idx, final int len) {
        final byte[] tmpbs = new byte[len];
        System.arraycopy(bs, idx, tmpbs, 0, len);
        for (int c = 0; c < len; ++c) {
            if (tmpbs[c] < 31 || tmpbs[c] > 126) {
                tmpbs[c] = 46;
            }
        }
        try {
            return new String(tmpbs, "UTF-8");
        } catch (final UnsupportedEncodingException ex) {
            return "";
        }
    }

    public static Date getDate(final byte[] bs, final int idx) {
        final Calendar cal = Calendar.getInstance();
        int y = transBCDtoByte(bs[idx]);
        if (y < 90) {
            y += 2000;
        } else {
            y += 1900;
        }
        final int m = transBCDtoByte(bs[idx + 1]) - 1;
        final int d = transBCDtoByte(bs[idx + 2]);
        final int h = transBCDtoByte(bs[idx + 3]);
        final int min = transBCDtoByte(bs[idx + 4]);
        final int s = transBCDtoByte(bs[idx + 5]);
        cal.set(y, m, d, h, min, s);
        return cal.getTime();
    }

    public static void setBit(final byte[] bs, final int idx, int bit_in_byte, final boolean v) {
        if (bit_in_byte < 0) {
            bit_in_byte = 0;
        }
        if (bit_in_byte > 7) {
            bit_in_byte = 7;
        }
        if (v) {
            bs[idx] |= (byte) (1 << bit_in_byte);
        } else {
            bs[idx] &= (byte) ~(1 << bit_in_byte);
        }
    }

    public static void setUInt16(final byte[] bs, final int idx, int v) {
        v &= 0xFFFF;
        bs[idx] = (byte) (v >> 8);
        bs[idx + 1] = (byte) (v & 0xFF);
    }

    public static void setInt16(final byte[] bs, final int idx, final int v) {
        bs[idx] = (byte) (v >> 8);
        bs[idx + 1] = (byte) (v & 0xFF);
    }

    public static void setUint32(final byte[] bs, final int idx, long v) {
        v &= -1L;
        bs[idx + 3] = (byte) (v & 0xFFL);
        bs[idx + 2] = (byte) (v >> 8 & 0xFFL);
        bs[idx + 1] = (byte) (v >> 16 & 0xFFL);
        bs[idx] = (byte) (v >> 24 & 0xFFL);
    }

    public static void setInt32(final byte[] bs, final int idx, final int v) {
        bs[idx + 3] = (byte) (v & 0xFF);
        bs[idx + 2] = (byte) (v >> 8 & 0xFF);
        bs[idx + 1] = (byte) (v >> 16 & 0xFF);
        bs[idx] = (byte) (v >> 24 & 0xFF);
    }

    public static void setFloat(final byte[] bs, final int idx, final float v) {
        final int iv = Float.floatToIntBits(v);
        setInt32(bs, idx, iv);
    }

    public static void setDate(final byte[] bs, final int idx, final Date v) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(v);
        int y = cal.get(1);
        final int m = cal.get(2) + 1;
        final int d = cal.get(5);
        final int h = cal.get(11);
        final int min = cal.get(12);
        final int s = cal.get(13);
        final int wk = cal.get(7);
        if (y > 1999) {
            y -= 2000;
        }
        bs[idx] = transByteToBCD(y);
        bs[idx + 1] = transByteToBCD(m);
        bs[idx + 2] = transByteToBCD(d);
        bs[idx + 3] = transByteToBCD(h);
        bs[idx + 4] = transByteToBCD(min);
        bs[idx + 5] = transByteToBCD(s);
        bs[idx + 6] = 0;
        bs[idx + 7] = transByteToBCD(wk);
    }

    public static int transBCDtoByte(final byte B) {
        return (B >> 4) * 10 + (B & 0xF);
    }

    public static byte transByteToBCD(final int v) {
        return (byte) (v / 10 << 4 | v % 10);
    }
}
