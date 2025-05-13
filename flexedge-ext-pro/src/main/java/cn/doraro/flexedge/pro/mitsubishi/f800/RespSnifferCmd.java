

package cn.doraro.flexedge.pro.mitsubishi.f800;

import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.UAVal;

import java.util.HashMap;
import java.util.List;

public abstract class RespSnifferCmd {
    RespSnifferDev belongTo;
    F800MsgReq req;
    HashMap<String, UATag> name2tag;

    public RespSnifferCmd(final RespSnifferDev dev, final F800MsgReq req) {
        this.req = null;
        this.name2tag = null;
        this.belongTo = dev;
        this.req = req;
    }

    protected static void RT_setTagVal(final UADev dev, final String tagn, final Object v) {
        final UATag tag = dev.getTagByName(tagn);
        if (tag != null) {
            tag.RT_setValRaw(v);
        }
    }

    public static RespSnifferCmd fromReq(final RespSnifferDev dev, final F800MsgReq req) {
        final int cc = req.getCmdCode();
        switch (cc) {
            case 122: {
                return new RSCStatus(dev, req);
            }
            case 111: {
                return new RSCOutHz(dev, req);
            }
            case 112: {
                return new RSCOutI(dev, req);
            }
            case 113: {
                return new RSCOutU(dev, req);
            }
            default: {
                return null;
            }
        }
    }

    public RespSnifferDev getBelongTo() {
        return this.belongTo;
    }

    public abstract void reconstructTags(final UADev p0) throws Exception;

    protected abstract List<String> listTagNames();

    protected UATag addOrUpTag(final String tagn, final String title, final UAVal.ValTP vt) throws Exception {
        final UADev dev = this.belongTo.getUADev();
        if (dev == null) {
            return null;
        }
        final UATag tag = dev.getTagByName(tagn);
        String tagid = null;
        if (tag != null) {
            tagid = tag.getId();
        }
        dev.addOrUpdateTag(tagid, false, tagn, title, "", (String) null, vt, -1, (String) null, -1L, (String) null, (String) null);
        synchronized (this) {
            this.name2tag = null;
        }
        return tag;
    }

    public synchronized HashMap<String, UATag> getName2Tag() {
        if (this.name2tag != null) {
            return this.name2tag;
        }
        final UADev dev = this.getBelongTo().getUADev();
        if (dev == null) {
            return null;
        }
        final HashMap<String, UATag> n2tag = new HashMap<String, UATag>();
        for (final String tagn : this.listTagNames()) {
            final UATag tag = dev.getTagByName(tagn);
            if (tag == null) {
                continue;
            }
            n2tag.put(tagn, tag);
        }
        return this.name2tag = n2tag;
    }

    public abstract boolean RT_injectResp(final F800MsgResp p0);
}
