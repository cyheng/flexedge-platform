// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink.fins;

import cn.doraro.flexedge.core.util.xmldata.DataUtil;
import cn.doraro.flexedge.driver.omron.fins.FinsMode;
import cn.doraro.flexedge.driver.omron.hostlink.HLException;
import cn.doraro.flexedge.driver.omron.hostlink.HLMsg;
import cn.doraro.flexedge.driver.omron.hostlink.HLMsgReq;

import java.util.ArrayList;
import java.util.List;

public class HLFinsRespMemR extends HLFinsResp {
    byte[] retBS;
    private HLFinsReqMemR myReq;

    public HLFinsRespMemR(final HLMsgReq req) {
        super(req);
        this.myReq = null;
        this.retBS = null;
        this.myReq = (HLFinsReqMemR) req;
    }

    @Override
    protected void parseFinsRet(final String fins_ret) throws HLException {
        final FinsMode.AreaCode ac = this.myReq.getAreaCode();
        this.retBS = HLMsg.hex2bytes(fins_ret);
        if (ac.isBit()) {
            if (this.retBS.length != this.myReq.itemNum) {
                throw new HLException(0, "response data num is not match to request");
            }
        } else if (this.retBS.length != this.myReq.itemNum * 2) {
            throw new HLException(0, "response data num is not match to request");
        }
    }

    public String getBitStr() {
        if (this.retBS == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (final byte b : this.retBS) {
            sb.append((b == 0) ? "0" : "1");
        }
        return sb.toString();
    }

    public List<Boolean> getBitList() {
        if (this.retBS == null) {
            return null;
        }
        final ArrayList<Boolean> rets = new ArrayList<Boolean>(this.retBS.length);
        for (final byte b : this.retBS) {
            rets.add(b != 0);
        }
        return rets;
    }

    public List<Short> getWordList() {
        if (this.retBS == null) {
            return null;
        }
        final int n = this.retBS.length / 2;
        final ArrayList<Short> rets = new ArrayList<Short>(this.retBS.length / 2);
        for (int i = 0; i < n; ++i) {
            final short s = DataUtil.bytesToShort(this.retBS, i * 2);
            rets.add(s);
        }
        return rets;
    }

    public String getWordStr() {
        final List<Short> ws = this.getWordList();
        if (ws == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        boolean bfirst = true;
        for (final Short s : ws) {
            if (bfirst) {
                bfirst = false;
            } else {
                sb.append(',');
            }
            sb.append(s);
        }
        return sb.toString();
    }

    public byte[] getReturnBytes() {
        return this.retBS;
    }
}
