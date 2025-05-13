

package cn.doraro.flexedge.pro.mitsubishi.f800;

import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.UAVal;

import java.util.Arrays;
import java.util.List;

class RSCStatus extends RespSnifferCmd {
    static List<String> tagNames;

    static {
        RSCStatus.tagNames = Arrays.asList("st_run", "st_forward", "st_reverse", "st_su", "st_ol", "st_ipf", "st_fu", "st_abc1");
    }

    public RSCStatus(final RespSnifferDev dev, final F800MsgReq req) {
        super(dev, req);
    }

    @Override
    public void reconstructTags(final UADev dev) throws Exception {
        this.addOrUpTag("st_run", "RUN", UAVal.ValTP.vt_bool);
        this.addOrUpTag("st_forward", "\u6b63\u8f6c\u4e2d", UAVal.ValTP.vt_bool);
        this.addOrUpTag("st_reverse", "\u53cd\u8f6c\u4e2d", UAVal.ValTP.vt_bool);
        this.addOrUpTag("st_su", "\u9891\u7387\u5230\u8fbe", UAVal.ValTP.vt_bool);
        this.addOrUpTag("st_ol", "\u8fc7\u8d1f\u8f7d", UAVal.ValTP.vt_bool);
        this.addOrUpTag("st_ipf", "\u77ac\u65f6\u505c\u7535", UAVal.ValTP.vt_bool);
        this.addOrUpTag("st_fu", "\u9891\u7387\u68c0\u6d4b", UAVal.ValTP.vt_bool);
        this.addOrUpTag("st_abc1", "\u5f02\u5e38", UAVal.ValTP.vt_bool);
    }

    @Override
    protected List<String> listTagNames() {
        return RSCStatus.tagNames;
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
        final int v = intv;
        RespSnifferCmd.RT_setTagVal(dev, "st_run", (v & 0x1) > 0);
        RespSnifferCmd.RT_setTagVal(dev, "st_forward", (v & 0x2) > 0);
        RespSnifferCmd.RT_setTagVal(dev, "st_reverse", (v & 0x4) > 0);
        RespSnifferCmd.RT_setTagVal(dev, "st_su", (v & 0x8) > 0);
        RespSnifferCmd.RT_setTagVal(dev, "st_ol", (v & 0x10) > 0);
        RespSnifferCmd.RT_setTagVal(dev, "st_ipf", (v & 0x20) > 0);
        RespSnifferCmd.RT_setTagVal(dev, "st_fu", (v & 0x40) > 0);
        RespSnifferCmd.RT_setTagVal(dev, "st_abc1", (v & 0x80) > 0);
        return true;
    }
}
