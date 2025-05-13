

package cn.doraro.flexedge.driver.gb.szy;

import cn.doraro.flexedge.core.*;
import cn.doraro.flexedge.core.basic.PropGroup;
import cn.doraro.flexedge.core.util.ILang;

import java.util.HashMap;
import java.util.List;

public class SZY206_2016DriverLisOnly extends DevDriverMsgOnly implements ILang {
    SZYListener szyLis;
    IRecvCallback cb;
    private UACh ch;
    private HashMap<String, TermItem> addrHex2Term;

    public SZY206_2016DriverLisOnly() {
        this.szyLis = new SZYListener();
        this.ch = null;
        this.addrHex2Term = new HashMap<String, TermItem>();
        this.cb = new IRecvCallback() {
            @Override
            public void onRecvFrame(final SZYFrame f) {
                SZY206_2016DriverLisOnly.this.RT_onFrameRecved(f);
            }
        };
    }

    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return false;
    }

    private synchronized TermItem getOrCreateTermItem(final String addrhex) {
        TermItem ti = this.addrHex2Term.get(addrhex);
        if (ti == null) {
            ti = new TermItem(addrhex);
            this.addrHex2Term.put(addrhex, ti);
            this.RT_fireDrvWarn("\u627e\u5230\u65b0\u7ec8\u7aef-" + addrhex);
        }
        return ti;
    }

    private void RT_onFrameRecved(final SZYFrame f) {
        final String addrhex = f.getAddrHex();
        final TermItem ti = this.getOrCreateTermItem(addrhex);
        final SZYFrame.UserData ud = f.getUserData();
        if (ud == null) {
            return;
        }
        if (ud instanceof SZYFrame.UDTermUpFlow) {
            ti.lastUpFlow = (SZYFrame.UDTermUpFlow) ud;
            this.RT_updateUpFlow(ti);
        }
    }

    public void RT_onConnMsgIn(final byte[] msgbs) {
        this.szyLis.onRecvedData(msgbs, this.cb);
    }

    public DevDriver copyMe() {
        return (DevDriver) new SZY206_2016DriverLisOnly();
    }

    public String getName() {
        return "szy206_2016_lis_only";
    }

    public String getTitle() {
        return "SZY206-2016\u53ea\u76d1\u542c";
    }

    public boolean supportDevFinder() {
        return true;
    }

    public boolean updateFindedDevs(final StringBuilder failedr) {
        this.ch = this.getBelongToCh();
        if (this.ch == null) {
            failedr.append("no ch found");
            return false;
        }
        try {
            for (final TermItem ti : this.addrHex2Term.values()) {
                this.reconstructDevTree(ti);
            }
            return true;
        } catch (final Exception ee) {
            failedr.append(ee.getMessage());
            return false;
        }
    }

    private void reconstructDevTree(final TermItem ti) throws Exception {
        final String devn = "t_" + ti.getAddrHex();
        UADev dev = this.ch.getDevByName(devn);
        if (dev == null) {
            dev = this.ch.addDev(devn, "\u7ec8\u7aef-" + ti.getAddrHex(), "", (String) null, (String) null, (String) null);
        }
        if (ti.lastUpFlow != null) {
            this.reconstructUpFlowTags(dev, ti.lastUpFlow);
        }
    }

    private void reconstructUpFlowTags(final UADev dev, final SZYFrame.UDTermUpFlow upf) throws Exception {
        this.addNotExistedTag(dev, "flow", "\u6d41\u901f", UAVal.ValTP.vt_float);
        this.addNotExistedTag(dev, "flow_t", "\u7d2f\u79ef\u6d41\u91cf", UAVal.ValTP.vt_float);
    }

    private void RT_updateUpFlow(final TermItem ti) {
        SZYFrame.UDTermUpFlow upf = null;
        if (ti == null || (upf = ti.lastUpFlow) == null) {
            return;
        }
        this.ch = this.getBelongToCh();
        if (this.ch == null) {
            return;
        }
        final String devn = "t_" + ti.getAddrHex();
        final UADev dev = this.ch.getDevByName(devn);
        if (dev == null) {
            return;
        }
        final Float f1 = upf.getFlow();
        if (f1 != null) {
            final UATag tag = dev.getTagByName("flow");
            tag.RT_setValRaw((Object) f1);
        }
        final Float f2 = upf.getFlowT();
        if (f2 != null) {
            final UATag tag2 = dev.getTagByName("flow_t");
            tag2.RT_setValRaw((Object) f2);
        }
    }

    private UATag addNotExistedTag(final UADev dev, final String tagn, final String title, final UAVal.ValTP vt) throws Exception {
        final UATag tag = dev.getTagByName(tagn);
        if (tag != null) {
            return tag;
        }
        dev.addOrUpdateTag((String) null, false, tagn, title, "", (String) null, vt, -1, (String) null, -1L, (String) null, (String) null);
        return tag;
    }

    public List<PropGroup> getPropGroupsForDevDef() {
        return null;
    }

    public List<PropGroup> getPropGroupsForCh(final UACh ch) {
        return null;
    }

    public List<PropGroup> getPropGroupsForDevInCh(final UADev d) {
        return null;
    }

    public DevAddr getSupportAddr() {
        return null;
    }

    protected void RT_onConnReady(final ConnPt cp, final UACh ch, final UADev dev) throws Exception {
    }

    protected void RT_onConnInvalid(final ConnPt cp, final UACh ch, final UADev dev) throws Exception {
    }

    protected boolean RT_runInLoop(final UACh ch, final UADev dev, final StringBuilder failedr) throws Exception {
        return true;
    }

    public boolean RT_writeVal(final UACh ch, final UADev dev, final UATag tag, final DevAddr da, final Object v) {
        return false;
    }

    public boolean RT_writeVals(final UACh ch, final UADev dev, final UATag[] tags, final DevAddr[] da, final Object[] v) {
        return false;
    }

    public static class TermItem {
        String addrHex;
        SZYFrame.UDTermUpFlow lastUpFlow;

        public TermItem(final String addrhex) {
            this.addrHex = null;
            this.lastUpFlow = null;
            this.addrHex = addrhex;
        }

        public String getAddrHex() {
            return this.addrHex;
        }

        public SZYFrame.UDTermUpFlow getUpFlow() {
            return this.lastUpFlow;
        }
    }
}
