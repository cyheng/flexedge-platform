// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.ppi;

import cn.doraro.flexedge.core.basic.ByteOrder;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.xmldata.DataUtil;

import java.io.InputStream;
import java.io.OutputStream;

public class PPICmdW extends PPICmd {
    PPIAddr writeAddr;
    int wVal;
    private transient PPIMsgReqW req;
    private transient PPIMsgReqConfirm reqc;

    public PPICmdW(final short dev_addr, final PPIMemTp ppi_mtp, final PPIAddr addr, final Object wval) {
        super(dev_addr, ppi_mtp);
        this.writeAddr = null;
        this.req = null;
        this.reqc = null;
        this.writeAddr = addr;
        if (wval instanceof Number) {
            if (wval instanceof Float) {
                final byte[] bs = DataUtil.floatToBytes((float) wval);
                this.wVal = DataUtil.bytesToInt(bs, ByteOrder.LittleEndian);
            } else {
                this.wVal = ((Number) wval).intValue();
            }
        } else if (wval instanceof Boolean) {
            this.wVal = (((boolean) wval) ? 1 : 0);
        } else {
            this.wVal = Integer.parseInt(wval + "");
        }
    }

    @Override
    void initCmd(final PPIDriver drv) {
        super.initCmd(drv);
        final int offetbytes = this.writeAddr.getOffsetBytes();
        final int inbit = this.writeAddr.getInBits();
        this.req = new PPIMsgReqW();
        this.req.withWriteVal(this.writeAddr, this.wVal).withAddrByte(this.ppiMemTp, offetbytes, inbit).withSorAddr(this.ppiDrv.getMasterID()).withDestAddr(this.devAddr);
        this.reqc = new PPIMsgReqConfirm();
        this.reqc.withSorAddr(this.ppiDrv.getMasterID()).withDestAddr(this.devAddr);
    }

    @Override
    public boolean doCmd(final InputStream inputs, final OutputStream outputs) throws Exception {
        Thread.sleep(this.ppiDrv.getCmdInterval());
        final byte[] bs2 = this.reqc.toBytes();
        inputs.skip(inputs.available());
        outputs.write(bs2);
        final byte[] retbs = PPIMsg.readFromStream(inputs, this.ppiDrv.getReadTimeout());
        if (retbs == null) {
            return false;
        }
        if (retbs.length == 1) {
        }
        Thread.sleep(10L);
        inputs.skip(inputs.available());
        final byte[] bs3 = this.req.toBytes();
        if (PPIMsg.log_w.isTraceEnabled()) {
            PPIMsg.log_w.trace("req w->" + Convert.byteArray2HexStr(bs3, " "));
        }
        outputs.write(bs3);
        int c = PPIMsg.readCharTimeout(inputs, this.ppiDrv.getReadTimeout());
        if (c != 229 && c != 249) {
            return false;
        }
        Thread.sleep(10L);
        outputs.write(bs2);
        c = PPIMsg.readCharTimeout(inputs, this.ppiDrv.getReadTimeout());
        return c == 229 || c == 249;
    }
}
