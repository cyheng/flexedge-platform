// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.nbiot.msg;

import cn.doraro.flexedge.core.util.Convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class WMMsg {
    byte[] meterAddr;
    byte[] func;
    private transient long parseReadTO;

    public WMMsg() {
        this.meterAddr = null;
        this.func = null;
        this.parseReadTO = 1000L;
    }

    protected static final int checkSum(final byte[] addr, final byte[] func, final List<byte[]> body_bs) {
        int r = 104;
        for (int i = 0; i < 8; ++i) {
            r += (addr[i] & 0xFF);
        }
        for (int i = 0; i < 2; ++i) {
            r += (func[i] & 0xFF);
        }
        if (body_bs != null) {
            for (final byte[] bs : body_bs) {
                for (int i = 0; i < bs.length; ++i) {
                    r += (bs[i] & 0xFF);
                }
            }
        }
        return r;
    }

    protected static final byte[] readLenTimeout(final InputStream inputs, final int rlen, final long to_ms) throws IOException {
        final byte[] ret = new byte[rlen];
        final long st = System.currentTimeMillis();
        while (inputs.available() < rlen) {
            try {
                Thread.sleep(1L);
            } catch (final Exception ex) {
            }
            if (System.currentTimeMillis() - st >= to_ms) {
                break;
            }
        }
        final int len = inputs.available();
        if (len >= rlen) {
            inputs.read(ret);
            return ret;
        }
        throw new IOException("time out");
    }

    public static WMMsg parseMsg(final InputStream inputs) throws IOException {
        byte[] addr = null;
        byte[] func = null;
        int st = 0;
        ArrayList<byte[]> body_bs = null;
        WMMsg msg = null;
        while (true) {
            switch (st) {
                case 0: {
                    if (inputs.available() < 12) {
                        return null;
                    }
                    final int c = inputs.read();
                    if (c == 161) {
                        st = 1;
                        continue;
                    }
                    if (inputs.available() < 12) {
                        return null;
                    }
                    continue;
                }
                case 1: {
                    final int c = inputs.read();
                    if (c != 104) {
                        st = 0;
                        return null;
                    }
                    st = 2;
                    continue;
                }
                case 2: {
                    addr = new byte[8];
                    inputs.read(addr);
                    st = 3;
                    continue;
                }
                case 3: {
                    func = new byte[2];
                    inputs.read(func);
                    final int f1 = func[0] & 0xFF;
                    final int f2 = func[1] & 0xFF;
                    if (f1 == 129 && (f2 == 1 || f2 == 16)) {
                        msg = new WMMsgReport();
                        msg.setMeterAddr(addr);
                        msg.setMsgFunc(func);
                    } else {
                        if (f1 != 130 || f2 != 2) {
                            return null;
                        }
                        msg = new WMMsgValveResp();
                        msg.setMeterAddr(addr);
                    }
                    st = 4;
                    continue;
                }
                case 4: {
                    body_bs = msg.parseMsgBody(inputs);
                    if (body_bs == null) {
                        return null;
                    }
                    st = 5;
                    continue;
                }
                case 5: {
                    if (inputs.available() < 2) {
                        return null;
                    }
                    final byte[] endbs = new byte[2];
                    inputs.read(endbs);
                    final int chksum = checkSum(addr, func, body_bs);
                    if ((endbs[0] & 0xFF) == (chksum & 0xFF) && endbs[1] == 22) {
                        return msg;
                    }
                    return null;
                }
            }
        }
    }

    public void setParseReadTimeout(final long toms) {
        this.parseReadTO = toms;
    }

    public void setMsgFunc(final byte[] func) {
        if (func.length != 2) {
            throw new IllegalArgumentException("invalid func info");
        }
        this.func = func;
    }

    public byte[] getMeterAddr() {
        return this.meterAddr;
    }

    public void setMeterAddr(final byte[] addr) {
        if (addr.length != 8) {
            throw new IllegalArgumentException("invalid addr info");
        }
        this.meterAddr = addr;
    }

    public byte[] getFuncCode() {
        return this.func;
    }

    private void writeOutInner(final OutputStream outputs) throws IOException {
        final byte[] bs01 = {-95, 104};
        final ArrayList<byte[]> bss = this.getMsgBody();
        final int chksum = checkSum(this.meterAddr, this.func, bss) & 0xFF;
        outputs.write(bs01);
        outputs.write(this.meterAddr);
        outputs.write(this.func);
        if (bss != null) {
            for (final byte[] bs2 : bss) {
                outputs.write(bs2);
            }
        }
        outputs.write(chksum);
        outputs.write(22);
    }

    public byte[] toWriteOutBytes() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.writeOutInner(baos);
        return baos.toByteArray();
    }

    public String toWriteOutHexStr() throws IOException {
        final byte[] bs = this.toWriteOutBytes();
        return Convert.byteArray2HexStr(bs);
    }

    public final void writeOut(final OutputStream outputs) throws IOException {
        final byte[] bs = this.toWriteOutBytes();
        outputs.write(bs);
    }

    final int bcd2int(final byte b) {
        final String s = Integer.toHexString(b & 0xFF);
        return Integer.parseInt(s);
    }

    final byte int2bcd(final int v) {
        return Byte.parseByte("" + v, 16);
    }

    final long bcd2long(final byte[] bs, final int offset, final int len) {
        String tmps = "";
        for (int i = 0; i < len; ++i) {
            final int vh = bs[i + offset] >> 4 & 0xF;
            final int vl = bs[i + offset] & 0xF;
            tmps += vh;
            tmps += vl;
        }
        return Long.parseLong(tmps);
    }

    protected final byte[] readLenTimeout(final InputStream inputs, final int rlen) throws IOException {
        return readLenTimeout(inputs, rlen, this.parseReadTO);
    }

    protected ArrayList<byte[]> getMsgBody() {
        return new ArrayList<byte[]>();
    }

    protected abstract ArrayList<byte[]> parseMsgBody(final InputStream p0) throws IOException;

    @Override
    public String toString() {
        String ret = "addr=" + Convert.byteArray2HexStr(this.meterAddr);
        ret = ret + " func=" + Convert.byteArray2HexStr(this.func);
        return ret;
    }
}
