// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.mitsubishi.fx;

import java.io.IOException;
import java.io.InputStream;

public class FxMsgRespR extends FxMsg {
    int byteNum;
    byte[] byteBuf;
    boolean readOk;
    String errInf;

    public FxMsgRespR(final int bytenum) {
        this.byteBuf = null;
        this.readOk = false;
        this.errInf = null;
        this.byteNum = bytenum;
    }

    @Override
    public byte[] toBytes() {
        return null;
    }

    public boolean readFromStream(final InputStream inputs, final long timeout) throws IOException {
        int st = 0;
        final int len = 3 + this.byteNum * 2;
        final byte[] ret = new byte[len];
        while (true) {
            switch (st) {
                case 0: {
                    int c;
                    do {
                        c = FxMsg.readCharTimeout(inputs, timeout);
                        if (c == 21) {
                            this.readOk = false;
                            this.errInf = "recv NAK";
                            return false;
                        }
                    } while (c != 2);
                    st = 1;
                    continue;
                }
                case 1: {
                    FxMsg.checkStreamLenTimeout(inputs, len, timeout);
                    inputs.read(ret);
                    final int retcrc = FxMsg.fromAsciiHexBytes(ret, len - 2, 2);
                    if (ret[len - 3] != 3) {
                        throw new IOException("no ETX found");
                    }
                    final int crc = FxMsg.calCRC(ret, 0, len - 2);
                    if (retcrc != crc) {
                        throw new IOException("check crc error");
                    }
                    this.byteBuf = new byte[this.byteNum];
                    for (int i = 0; i < this.byteNum; ++i) {
                        final int bt = FxMsg.fromAsciiHexBytes(ret, i * 2, 2);
                        this.byteBuf[i] = (byte) bt;
                    }
                    return this.readOk = true;
                }
            }
        }
    }

    public byte[] getRetData() {
        return this.byteBuf;
    }
}
