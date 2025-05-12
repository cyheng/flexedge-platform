// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.f800;

import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.UADev;
import java.util.Arrays;
import java.util.List;

class RSCOutU extends RespSnifferCmd
{
    static List<String> tagNames;
    
    static {
        RSCOutU.tagNames = Arrays.asList("out_u");
    }
    
    public RSCOutU(final RespSnifferDev dev, final F800MsgReq req) {
        super(dev, req);
    }
    
    @Override
    public void reconstructTags(final UADev dev) throws Exception {
        this.addOrUpTag("out_u", "\u8f93\u51fa\u7535\u538b", UAVal.ValTP.vt_float);
    }
    
    @Override
    protected List<String> listTagNames() {
        return RSCOutU.tagNames;
    }
    
    @Override
    public boolean RT_injectResp(final F800MsgResp resp) {
        final Integer intv = resp.getRespVal();
        if (intv == null) {
            return false;
        }
        final UADev dev = this.getBelongTo().getUADev();
        if (dev == null) {
            return false;
        }
        final float v = intv;
        RespSnifferCmd.RT_setTagVal(dev, "out_u", v / 10.0f);
        return true;
    }
}
