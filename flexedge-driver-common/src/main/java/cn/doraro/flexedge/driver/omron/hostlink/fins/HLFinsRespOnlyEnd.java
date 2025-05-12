// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink.fins;

import cn.doraro.flexedge.driver.omron.hostlink.HLException;
import cn.doraro.flexedge.driver.omron.hostlink.HLMsgReq;

public class HLFinsRespOnlyEnd extends HLFinsResp {
    private HLFinsReqMemW myReq;

    public HLFinsRespOnlyEnd(final HLMsgReq req) {
        super(req);
        this.myReq = null;
        this.myReq = (HLFinsReqMemW) req;
    }

    @Override
    protected void parseFinsRet(final String fins_ret) throws HLException {
    }
}
