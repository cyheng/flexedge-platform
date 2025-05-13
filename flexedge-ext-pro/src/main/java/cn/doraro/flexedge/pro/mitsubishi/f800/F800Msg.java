

package cn.doraro.flexedge.pro.mitsubishi.f800;

public class F800Msg {
    public static final byte STX = 2;
    public static final byte ETX = 3;
    public static final byte ENQ = 5;
    public static final byte ACK = 6;
    public static final byte LF = 10;
    public static final byte CR = 13;
    public static final byte NAK = 21;
    int stationNo;

    public F800Msg() {
        this.stationNo = 1;
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

    public F800Msg asStationNo(final int station_n) {
        if (station_n < 0 || station_n > 255) {
            throw new IllegalArgumentException("invalid station no [0,0xFF]");
        }
        this.stationNo = station_n;
        return this;
    }

    public int getStationNo() {
        return this.stationNo;
    }
}
