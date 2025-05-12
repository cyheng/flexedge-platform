// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus.sniffer;

import cn.doraro.flexedge.driver.common.modbus.ModbusCmd;
import cn.doraro.flexedge.driver.common.modbus.ModbusCmdRead;

import java.util.HashMap;

public class SnifferRTUCh {
    private static final int PST_NOR = 0;
    private static final int PST_REQ = 1;
    private static final int PST_RESP = 2;
    HashMap<String, SnifferCmd> id2scmd;
    private int pst;
    private SnifferCmd curCmd;
    private SnifferBufferFix sniBuf;

    public SnifferRTUCh() {
        this.id2scmd = new HashMap<String, SnifferCmd>();
        this.pst = 0;
        this.curCmd = null;
        this.sniBuf = new SnifferBufferFix(1024);
    }

    public void onSniffedData(final byte[] bs, final ISnifferCallback cb) {
        if (bs == null || bs.length <= 0) {
            return;
        }
        try {
            this.sniBuf.addData(bs);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        int buflen;
        Label_0324:
        while ((buflen = this.sniBuf.getBufLen()) > 0) {
            switch (this.pst) {
                case 0: {
                    while ((buflen = this.sniBuf.getBufLen()) >= 8) {
                        final byte[] tmpbs = new byte[8];
                        this.sniBuf.peekData(tmpbs, 0, 8);
                        this.curCmd = this.parseReqCmd(tmpbs);
                        if (this.curCmd != null) {
                            this.sniBuf.skipLen(8);
                            this.pst = 1;
                            continue Label_0324;
                        }
                        this.sniBuf.readNextChar();
                        if (this.curCmd != null) {
                            continue Label_0324;
                        }
                    }
                    return;
                }
                case 1: {
                    if (buflen < 3) {
                        return;
                    }
                    byte[] tmpbs = new byte[3];
                    this.sniBuf.peekData(tmpbs, 0, 3);
                    if (tmpbs[0] != this.curCmd.getDevId() || tmpbs[1] != this.curCmd.getFC()) {
                        this.pst = 0;
                        continue;
                    }
                    final int resplen = this.curCmd.getRespLen();
                    if ((0xFF & tmpbs[2]) != resplen - 5) {
                        this.pst = 0;
                        continue;
                    }
                    if (buflen < resplen) {
                        return;
                    }
                    tmpbs = new byte[resplen];
                    this.sniBuf.peekData(tmpbs, 0, resplen);
                    if (this.curCmd.parseResp(tmpbs)) {
                        if (cb != null) {
                            cb.onSnifferCmd(this.curCmd);
                        }
                        this.onSnifferCmdFound(this.curCmd);
                        this.sniBuf.skipLen(resplen);
                    }
                    this.pst = 0;
                    continue;
                }
            }
        }
    }

    private SnifferCmd parseReqCmd(final byte[] bs) {
        final int[] pl = {0};
        final ModbusCmd mc = ModbusCmd.parseRequest(bs, pl);
        if (mc == null) {
            return null;
        }
        if (!(mc instanceof ModbusCmdRead)) {
            return null;
        }
        final ModbusCmdRead mcr = (ModbusCmdRead) mc;
        final int resplen = mc.calRespLenRTU();
        if (resplen <= 0) {
            return null;
        }
        return new SnifferCmd(mcr);
    }

    private void onSnifferCmdFound(final SnifferCmd sc) {
        this.id2scmd.put(sc.getUniqueId(), sc);
    }

    public SnifferCmd getSnifferCmd(final String id) {
        return this.id2scmd.get(id);
    }
}
