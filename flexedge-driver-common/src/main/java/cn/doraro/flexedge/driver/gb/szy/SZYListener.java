// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.gb.szy;

import java.util.ArrayList;

public class SZYListener
{
    private SZYFrame curSingleF;
    private ArrayList<SZYFrame> curMultiFs;
    private SZYRecvBufferFix sniBuf;
    private int curFrameL;
    
    public SZYListener() {
        this.curSingleF = null;
        this.curMultiFs = null;
        this.sniBuf = new SZYRecvBufferFix(256);
        this.curFrameL = -1;
    }
    
    public void onRecvedData(final byte[] bs, final IRecvCallback cb) {
        if (bs == null || bs.length <= 0) {
            return;
        }
        try {
            this.sniBuf.addData(bs);
        }
        catch (final Exception e) {
            e.printStackTrace();
        }
        int buflen;
        while ((buflen = this.sniBuf.getBufLen()) > 0) {
            if (this.curFrameL <= 0) {
                if ((buflen = this.sniBuf.getBufLen()) < 3) {
                    return;
                }
                final byte[] tmpbs = new byte[3];
                this.sniBuf.peekData(tmpbs, 0, 3);
                if (tmpbs[0] != 104) {
                    this.sniBuf.readNextChar();
                }
                else if (tmpbs[2] != 104) {
                    this.sniBuf.readNextChar();
                }
                else {
                    this.curFrameL = (tmpbs[1] & 0xFF);
                    this.sniBuf.skipLen(3);
                }
            }
            else {
                if (buflen < this.curFrameL + 2) {
                    return;
                }
                final byte[] tmpbs = new byte[this.curFrameL];
                final byte[] endbs = new byte[2];
                this.sniBuf.readData(tmpbs, 0, this.curFrameL);
                this.sniBuf.readData(endbs, 0, 2);
                if (endbs[1] != 22) {
                    return;
                }
                final byte crc = SZYMsg.calcCrc8(tmpbs, 0, this.curFrameL);
                if (crc != endbs[0]) {
                    return;
                }
                final SZYFrame f = new SZYFrame(tmpbs);
                if (f.parseData() && cb != null) {
                    cb.onRecvFrame(f);
                }
                this.curFrameL = -1;
            }
        }
    }
}
