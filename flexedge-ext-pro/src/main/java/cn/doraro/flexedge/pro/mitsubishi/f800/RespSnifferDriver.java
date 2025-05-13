

package cn.doraro.flexedge.pro.mitsubishi.f800;

import cn.doraro.flexedge.core.*;
import cn.doraro.flexedge.core.basic.PropGroup;
import cn.doraro.flexedge.core.basic.PropItem;
import cn.doraro.flexedge.core.conn.ConnPtMSGMultiTcp;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.Lan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class RespSnifferDriver extends DevDriverMsgOnly {
    List<RespSnifferDev> snifferDevs;
    int RT_curDevIdx;
    private List<PropGroup> repPGS;
    private long parseIntv;
    private long parseTO;
    private UACh ch;
    private transient long rt_lastParseDT;

    public RespSnifferDriver() {
        this.repPGS = null;
        this.snifferDevs = null;
        this.parseIntv = 1000L;
        this.parseTO = 10000L;
        this.ch = null;
        this.rt_lastParseDT = -1L;
        this.RT_curDevIdx = 0;
    }

    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return false;
    }

    public DevDriver copyMe() {
        return (DevDriver) new RespSnifferDriver();
    }

    public String getName() {
        return "mitsubishi_f800_snif";
    }

    public String getTitle() {
        return "\u4e09\u83f1\u53d8\u9891\u5668\u76d1\u542c\u5668";
    }

    public Class<? extends ConnPt> supportConnPtClass() {
        return (Class<? extends ConnPt>) ConnPtMSGMultiTcp.class;
    }

    public List<PropGroup> getPropGroupsForDevDef() {
        return null;
    }

    public List<PropGroup> getPropGroupsForCh(final UACh ch) {
        if (this.repPGS != null) {
            return this.repPGS;
        }
        final ArrayList<PropGroup> pgs = new ArrayList<PropGroup>();
        pgs.add(this.getPropGroup());
        return this.repPGS = pgs;
    }

    public List<PropGroup> getPropGroupsForDevInCh(final UADev d) {
        return null;
    }

    private PropGroup getPropGroup() {
        final Lan lan = Lan.getPropLangInPk((Class) this.getClass());
        final PropGroup r = new PropGroup("f800_lis", lan);
        r.addPropItem(new PropItem("f800_req", lan, PropItem.PValTP.vt_str, false, (String[]) null, (Object[]) null, (Object) "").withTxtMultiLine(true));
        r.addPropItem(new PropItem("f800_p_intv", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 1000));
        r.addPropItem(new PropItem("f800_p_timeout", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 10000));
        return r;
    }

    public DevAddr getSupportAddr() {
        return null;
    }

    public List<ConnMsg> getConnMsgs() {
        return null;
    }

    private List<RespSnifferDev> parseReqStr(final String reqstr, final StringBuilder failedr) throws IOException {
        if (Convert.isNullOrEmpty(reqstr)) {
            failedr.append("no req str input");
            return null;
        }
        final ArrayList<RespSnifferDev> rets = new ArrayList<RespSnifferDev>();
        RespSnifferDev curdev = null;
        final BufferedReader br = new BufferedReader(new StringReader(reqstr));
        String ln;
        while ((ln = br.readLine()) != null) {
            ln = ln.trim();
            if (Convert.isNullOrEmpty(ln)) {
                continue;
            }
            final byte[] bs = Convert.hexStr2ByteArray(ln);
            final F800MsgReq req = F800MsgReq.parseFrom(bs);
            if (req == null) {
                failedr.append("invalid req cmd=" + ln + ",no F800MsgReq found");
                return null;
            }
            final int stationno = req.getStationNo();
            if (curdev == null || curdev.getStationNo() != stationno) {
                curdev = new RespSnifferDev(this, stationno);
                rets.add(curdev);
            }
            curdev.reqs.add(req);
        }
        return rets;
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
        final String reqstr = (String) this.ch.getPropValue("f800_lis", "f800_req");
        List<RespSnifferDev> snf_devs = null;
        try {
            snf_devs = this.parseReqStr(reqstr, failedr);
            if (snf_devs == null || snf_devs.size() <= 0) {
                failedr.append("no sniffer device found by request cmd or it is not be set");
                return false;
            }
        } catch (final Exception ee) {
            failedr.append(ee.getMessage());
            return false;
        }
        try {
            for (final RespSnifferDev snfdev : snf_devs) {
                snfdev.reconstructDevTree();
            }
            return true;
        } catch (final Exception ee) {
            failedr.append(ee.getMessage());
            return false;
        }
    }

    protected boolean initDriver(final StringBuilder failedr) throws Exception {
        if (!super.initDriver(failedr)) {
            return false;
        }
        this.ch = this.getBelongToCh();
        if (this.ch == null) {
            failedr.append("no ch found");
            return false;
        }
        final String reqstr = (String) this.ch.getPropValue("f800_lis", "f800_req");
        this.snifferDevs = this.parseReqStr(reqstr, failedr);
        this.parseIntv = ((Number) this.ch.getPropValue("f800_lis", "f800_p_intv", (Object) 1000)).longValue();
        this.parseTO = ((Number) this.ch.getPropValue("f800_lis", "f800_p_timeout", (Object) 10000)).longValue();
        if (this.parseTO <= 0L) {
            this.parseTO = 10000L;
        }
        this.rt_lastParseDT = -1L;
        return this.snifferDevs != null;
    }

    public long RT_getLastParseDT() {
        return this.rt_lastParseDT;
    }

    public void RT_onConnMsgIn(final byte[] msgbs) {
        if (this.parseIntv > 0L && System.currentTimeMillis() - this.rt_lastParseDT < this.parseIntv) {
            return;
        }
        if (this.snifferDevs == null || this.snifferDevs.size() <= 0) {
            return;
        }
        if (this.RT_onConnMsgParse(msgbs)) {
            this.RT_useParsed();
            this.RT_resetDevs();
            this.rt_lastParseDT = System.currentTimeMillis();
        }
    }

    private void RT_resetDevs() {
        for (final RespSnifferDev d : this.snifferDevs) {
            d.parsedResps = null;
        }
        this.RT_curDevIdx = 0;
    }

    private boolean RT_onConnMsgParse(final byte[] msgbs) {
        final F800MsgResp resp = F800MsgResp.parseFrom(msgbs);
        if (resp == null) {
            this.RT_resetDevs();
            return false;
        }
        final RespSnifferDev dev = this.snifferDevs.get(this.RT_curDevIdx);
        final int stationno = dev.getStationNo();
        final int resp_no = resp.getStationNo();
        if (resp_no != stationno) {
            this.RT_resetDevs();
            return false;
        }
        if (dev.parsedResps == null) {
            dev.parsedResps = new ArrayList<F800MsgResp>();
        }
        dev.parsedResps.add(resp);
        if (dev.parsedResps.size() != dev.reqs.size()) {
            return false;
        }
        if (this.snifferDevs.size() == this.RT_curDevIdx + 1) {
            return true;
        }
        ++this.RT_curDevIdx;
        return false;
    }

    private void RT_useParsed() {
        for (final RespSnifferDev d : this.snifferDevs) {
            d.RT_updateDevice(this.ch);
        }
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
}
