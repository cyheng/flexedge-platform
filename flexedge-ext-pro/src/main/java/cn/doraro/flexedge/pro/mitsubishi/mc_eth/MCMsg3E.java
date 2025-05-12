// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class MCMsg3E extends MCMsg {
    public static ILogger log;
    public static ILogger log_w;

    static {
        MCMsg3E.log = LoggerManager.getLogger((Class) MCMsg3E.class);
        MCMsg3E.log_w = LoggerManager.getLogger(MCMsg3E.class.getCanonicalName() + "_w");
    }

    int netNo;
    int plcNo;
    int moduleIO_No;
    int moduleStationNo;

    public MCMsg3E() {
        this.netNo = 0;
        this.plcNo = 255;
        this.moduleIO_No = 1023;
        this.moduleStationNo = 0;
    }

    @Override
    public final int getSubframeReq() {
        return 20480;
    }

    @Override
    public final int getSubframeResp() {
        return 53248;
    }

    public MCMsg3E asModuleIO_NO(final int module_io_no) {
        this.moduleIO_No = module_io_no;
        return this;
    }

    public MCMsg3E asModuleStationNo(final int mn) {
        this.moduleStationNo = mn;
        return this;
    }

    @Override
    public void writeOutAscii(final OutputStream outputs) throws Exception {
        super.writeOutAscii(outputs);
        final byte[] bs = new byte[10];
        MCMsg.toAsciiHexBytes(this.netNo, bs, 0, 2);
        MCMsg.toAsciiHexBytes(this.plcNo, bs, 2, 2);
        MCMsg.toAsciiHexBytes(this.moduleIO_No, bs, 4, 4);
        MCMsg.toAsciiHexBytes(this.moduleStationNo, bs, 8, 2);
        outputs.write(bs);
    }

    @Override
    public void writeOutBin(final OutputStream outputs) throws Exception {
        super.writeOutBin(outputs);
        final byte[] bs = {(byte) this.netNo, (byte) this.plcNo, 0, 0, 0};
        MCMsg.toBinHexBytes(this.moduleIO_No, bs, 2, 2);
        bs[4] = (byte) this.moduleStationNo;
        outputs.write(bs);
    }

    @Override
    protected boolean readFromAscii(final InputStream inputs, final long timeout, final StringBuilder failedr) throws IOException {
        if (!super.readFromAscii(inputs, timeout, failedr)) {
            return false;
        }
        final byte[] bs = new byte[10];
        MCMsg.readBytesTimeout(inputs, timeout, bs, 0, 10);
        this.netNo = MCMsg.fromAsciiHexBytes(bs, 0, 2);
        this.plcNo = MCMsg.fromAsciiHexBytes(bs, 2, 2);
        this.moduleIO_No = MCMsg.fromAsciiHexBytes(bs, 4, 4);
        this.moduleStationNo = MCMsg.fromAsciiHexBytes(bs, 8, 2);
        return true;
    }

    @Override
    protected boolean readFromBin(final InputStream inputs, final long timeout, final StringBuilder failedr) throws IOException {
        if (!super.readFromBin(inputs, timeout, failedr)) {
            return false;
        }
        final byte[] bs = new byte[5];
        MCMsg.readBytesTimeout(inputs, timeout, bs, 0, 5);
        this.netNo = (bs[0] & 0xFF);
        this.plcNo = (bs[1] & 0xFF);
        this.moduleIO_No = (bs[3] & 0xFF);
        this.moduleIO_No <<= 8;
        this.moduleIO_No += (bs[2] & 0xFF);
        this.moduleStationNo = (bs[4] & 0xFF);
        return true;
    }
}
