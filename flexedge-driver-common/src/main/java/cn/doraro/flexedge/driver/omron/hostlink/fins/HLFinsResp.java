// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink.fins;

import cn.doraro.flexedge.driver.omron.hostlink.HLMsg;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.driver.omron.hostlink.HLException;
import cn.doraro.flexedge.driver.omron.hostlink.HLMsgReq;
import cn.doraro.flexedge.driver.omron.fins.FinsEndCode;
import cn.doraro.flexedge.driver.omron.fins.FinsMode;
import cn.doraro.flexedge.driver.omron.hostlink.HLMsgResp;

public abstract class HLFinsResp extends HLMsgResp
{
    HLFinsReq hlFinsReq;
    FinsMode mode;
    short icf;
    short gct;
    short dna;
    short da1;
    short da2;
    short sna;
    short sa1;
    short sa2;
    short sid;
    short finsMR;
    short finsSR;
    FinsEndCode finsEndCode;
    
    public HLFinsResp(final HLMsgReq req) {
        super(req);
        this.icf = -1;
        this.gct = -1;
        this.dna = -1;
        this.da1 = -1;
        this.da2 = -1;
        this.sna = -1;
        this.sa1 = -1;
        this.sa2 = -1;
        this.sid = -1;
        this.finsMR = -1;
        this.finsSR = -1;
        this.finsEndCode = null;
        this.hlFinsReq = (HLFinsReq)req;
        this.mode = this.hlFinsReq.mode;
    }
    
    @Override
    protected void parseHLTxt(final String hl_txt) throws HLException {
        if (!"FA".equals(this.getHeadCode())) {
            throw new HLException(0, "no FA Host link msg");
        }
        final String enc = this.getHLEndCode();
        if (!"00".equals(enc)) {
            String tt = HLMsgResp.getEndCodeTitle(enc);
            if (Convert.isNullOrEmpty(tt)) {
                tt = enc;
            }
            throw new HLException(0, "resp end err:" + tt);
        }
        this.parseFinsTxt(hl_txt);
    }
    
    private void parseFinsTxt(final String hl_txt) throws HLException {
        short main;
        short sub;
        String fins_txt;
        if (!this.hlFinsReq.bHeaderNet) {
            this.icf = HLMsg.hex2byte(hl_txt.substring(0, 2));
            this.da2 = HLMsg.hex2byte(hl_txt.substring(2, 4));
            this.sa2 = HLMsg.hex2byte(hl_txt.substring(4, 6));
            this.sid = HLMsg.hex2byte(hl_txt.substring(6, 8));
            this.finsMR = HLMsg.hex2byte(hl_txt.substring(8, 10));
            this.finsSR = HLMsg.hex2byte(hl_txt.substring(10, 12));
            main = HLMsg.hex2byte(hl_txt.substring(12, 14));
            sub = HLMsg.hex2byte(hl_txt.substring(14, 16));
            fins_txt = hl_txt.substring(16);
        }
        else {
            this.icf = HLMsg.hex2byte(hl_txt.substring(0, 2));
            this.gct = HLMsg.hex2byte(hl_txt.substring(4, 6));
            this.dna = HLMsg.hex2byte(hl_txt.substring(6, 8));
            this.da1 = HLMsg.hex2byte(hl_txt.substring(8, 10));
            this.da2 = HLMsg.hex2byte(hl_txt.substring(10, 12));
            this.sna = HLMsg.hex2byte(hl_txt.substring(12, 14));
            this.sa1 = HLMsg.hex2byte(hl_txt.substring(14, 16));
            this.sa2 = HLMsg.hex2byte(hl_txt.substring(16, 18));
            this.sid = HLMsg.hex2byte(hl_txt.substring(18, 20));
            this.finsMR = HLMsg.hex2byte(hl_txt.substring(20, 22));
            this.finsSR = HLMsg.hex2byte(hl_txt.substring(22, 24));
            main = HLMsg.hex2byte(hl_txt.substring(24, 26));
            sub = HLMsg.hex2byte(hl_txt.substring(26, 28));
            fins_txt = hl_txt.substring(28);
        }
        this.finsEndCode = new FinsEndCode(main, sub);
        if (this.finsEndCode.isNormal()) {
            this.parseFinsRet(fins_txt);
        }
    }
    
    public FinsEndCode getFinsEndCode() {
        return this.finsEndCode;
    }
    
    public boolean isFinsEndOk() {
        return this.finsEndCode.isNormal();
    }
    
    protected abstract void parseFinsRet(final String p0) throws HLException;
}
