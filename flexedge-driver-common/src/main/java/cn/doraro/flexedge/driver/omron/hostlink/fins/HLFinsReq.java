// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink.fins;

import cn.doraro.flexedge.core.util.xmldata.DataUtil;
import cn.doraro.flexedge.core.util.IBSOutput;
import cn.doraro.flexedge.core.util.BSOutputBuf;
import cn.doraro.flexedge.driver.omron.hostlink.HLMsg;
import cn.doraro.flexedge.driver.omron.fins.FinsMode;
import cn.doraro.flexedge.driver.omron.hostlink.HLMsgReq;

public abstract class HLFinsReq extends HLMsgReq
{
    short respWaitTime;
    boolean bHeaderNet;
    short icf;
    short gct;
    short dna;
    short da1;
    short da2;
    short sna;
    short sa1;
    short sa2;
    short sid;
    FinsMode mode;
    
    public HLFinsReq(final FinsMode mode) {
        this.respWaitTime = 0;
        this.bHeaderNet = false;
        this.icf = -1;
        this.gct = -1;
        this.dna = -1;
        this.da1 = -1;
        this.da2 = -1;
        this.sna = -1;
        this.sa1 = -1;
        this.sa2 = -1;
        this.sid = -1;
        this.mode = mode;
    }
    
    public HLFinsReq asRespWaitTime(final short wt) {
        if (wt < 0 || wt > 15) {
            throw new IllegalArgumentException("Response Wait Time must in [0,F]");
        }
        this.respWaitTime = wt;
        return this;
    }
    
    @Override
    public String getHeadCode() {
        return "FA";
    }
    
    public HLFinsReq asFinsHeaderSerial(final int da2, final int sa2, final int sid) {
        this.bHeaderNet = false;
        this.icf = 0;
        this.da2 = (short)da2;
        this.sa2 = (short)sa2;
        this.sid = (short)sid;
        return this;
    }
    
    public HLFinsReq asFinsHeaderSerial() {
        return this.asFinsHeaderSerial(0, 0, 0);
    }
    
    public HLFinsReq asFinsHeaderNet(final int icf, final int gct, final int dna, final int da1, final int da2, final int sna, final int sa1, final int sa2, final int sid) {
        this.bHeaderNet = true;
        this.icf = (short)icf;
        this.gct = (short)gct;
        this.dna = (short)dna;
        this.da1 = (short)da1;
        this.da2 = (short)da2;
        this.sna = (short)sna;
        this.sa1 = (short)sa1;
        this.sa2 = (short)sa2;
        this.sid = (short)sid;
        return this;
    }
    
    @Override
    protected final void packContent(final StringBuilder sb) {
        sb.append(HLMsg.byte2hex(this.respWaitTime, false));
        if (!this.bHeaderNet) {
            sb.append(HLMsg.byte2hex(this.icf, true));
            sb.append(HLMsg.byte2hex(this.da2, true));
            sb.append(HLMsg.byte2hex(this.sa2, true));
            sb.append(HLMsg.byte2hex(this.sid, true));
        }
        else {
            sb.append(HLMsg.byte2hex(this.icf, true));
            sb.append(HLMsg.byte2hex(0, true));
            sb.append(HLMsg.byte2hex(this.gct, true));
            sb.append(HLMsg.byte2hex(this.dna, true));
            sb.append(HLMsg.byte2hex(this.da1, true));
            sb.append(HLMsg.byte2hex(this.da2, true));
            sb.append(HLMsg.byte2hex(this.sna, true));
            sb.append(HLMsg.byte2hex(this.sa1, true));
            sb.append(HLMsg.byte2hex(this.sa2, true));
            sb.append(HLMsg.byte2hex(this.sid, true));
        }
        sb.append(HLMsg.byte2hex(this.getMR(), true));
        sb.append(HLMsg.byte2hex(this.getSR(), true));
        this.packCmdText(sb);
    }
    
    protected abstract short getMR();
    
    protected abstract short getSR();
    
    private void packCmdText(final StringBuilder sb) {
        final BSOutputBuf bso = new BSOutputBuf();
        this.packOutCmdParam((IBSOutput)bso);
        sb.append(bso.toHexStr(true));
    }
    
    protected abstract void packOutCmdParam(final IBSOutput p0);
    
    protected static final byte[] int2bytes(final int i) {
        return DataUtil.intToBytes(i);
    }
    
    protected static final byte[] short2bytes(final short i) {
        return DataUtil.shortToBytes(i);
    }
    
    protected static final void short2bytes(final short i, final byte[] bs, final int offset) {
        DataUtil.shortToBytes(i, bs, offset);
    }
}
