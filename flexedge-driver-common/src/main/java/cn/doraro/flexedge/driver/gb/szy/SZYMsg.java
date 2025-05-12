// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.gb.szy;

public abstract class SZYMsg
{
    short len;
    byte[] data;
    
    public SZYMsg(final byte[] data) {
        if (data.length > 255) {
            throw new IllegalArgumentException("data len too long");
        }
        this.len = (short)data.length;
        this.data = data;
    }
    
    public short getLen() {
        return this.len;
    }
    
    public byte[] getData() {
        return this.data;
    }
    
    public byte[] packTo() {
        final byte[] ret = new byte[this.len + 5];
        ret[0] = (ret[2] = 104);
        ret[1] = (byte)(this.len & 0xFF);
        System.arraycopy(this.data, 0, ret, 3, this.len);
        ret[this.len + 3] = calcCrc8(this.data, 0, this.data.length);
        ret[this.len + 4] = 22;
        return ret;
    }
    
    static byte calcCrc8(final byte[] data, final int offset, final int len) {
        int crc = 0;
        final int dxs = 229;
        for (int i = 0; i < len; ++i) {
            final byte datum = data[i + offset];
            crc ^= datum;
            for (int j = 0; j < 8; ++j) {
                final int sbit = crc & 0x80;
                crc <<= 1;
                if (sbit != 0) {
                    crc ^= dxs;
                }
            }
        }
        return (byte)crc;
    }
    
    static String transBCD2Str(final byte[] bcd, final int offset, final int len) {
        final StringBuffer sb = new StringBuffer();
        for (int i = offset; i < len; ++i) {
            sb.append((bcd[i] & 0xF0) >> 4);
            sb.append(bcd[i] & 0xF);
        }
        return sb.toString();
    }
}
