// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.modbuss;

import cn.doraro.flexedge.core.msgnet.*;
import cn.doraro.flexedge.core.util.Convert;
import org.json.JSONObject;

import java.util.List;

public class MSUpdateMem_NE extends MNNodeEnd {
    public static final String TP = "ms_up_mem";
    String devId;

    public MSUpdateMem_NE() {
        this.devId = null;
    }

    public String getTP() {
        return "ms_up_mem";
    }

    public String getTPTitle() {
        return "Update Device Mem";
    }

    public String getColor() {
        return "#24acf2";
    }

    public String getIcon() {
        return "PK_bus";
    }

    public boolean isParamReady(final StringBuilder failedr) {
        return true;
    }

    public JSONObject getParamJO() {
        final JSONObject jo = new JSONObject();
        jo.putOpt("dev_id", (Object) this.devId);
        return jo;
    }

    protected void setParamJO(final JSONObject jo) {
        this.devId = jo.optString("dev_id");
    }

    protected RTOut RT_onMsgIn(final MNConn in_conn, final MNMsg msg) throws Exception {
        if (Convert.isNullOrEmpty(this.devId)) {
            return null;
        }
        final MSBus_M bus = (MSBus_M) this.getOwnRelatedModule();
        final JSONObject jo = msg.getPayloadJO((JSONObject) null);
        if (jo == null) {
            return null;
        }
        bus.RT_setMemData(this.devId, jo);
        return null;
    }

    protected void RT_renderDiv(final List<MNBase.DivBlk> divblks) {
        final StringBuilder divsb = new StringBuilder();
        final MSBus_M bus = (MSBus_M) this.getOwnRelatedModule();
        final SlaveDev dev = bus.getDev(this.devId);
        if (dev != null) {
            final String devst = dev.RT_isDevValid() ? "" : " <span style='color:red'>Invalid</span>";
            divsb.append("<div class=\"rt_blk\" style='position:relative;height:90%;'><div style='background-color:#aaaaaa;white-space: nowrap;'>Mem Date " + devst + "</div>");
            divsb.append("<div class=\"rt_debug_list\" id=\"ms_up_mem_" + this.getId() + "\" ></div>");
            for (final SlaveDevSeg seg : dev.getSegs()) {
                divsb.append("<div class='sub_span_nowrap' style='border:1px solid #dddddd;margin-top:3px;'>").append(seg.fc).append(seg.getFCTitle()).append(" - ").append(seg.getTitle()).append("<br>");
                this.RT_listSeg(seg, divsb);
                divsb.append("</div>");
            }
            divsb.append("</div>");
            divblks.add(new MNBase.DivBlk("ms_up_mem", divsb.toString()));
        }
        super.RT_renderDiv((List) divblks);
    }

    private void RT_listSeg(final SlaveDevSeg seg, final StringBuilder sb) {
        final int regidx = seg.regIdx;
        final int regnum = seg.regNum;
        final int fc = seg.fc;
        for (int i = 0; i < regnum; ++i) {
            sb.append("<span>").append(seg.getAddressStr(regidx + i)).append(": [").append(seg.getSlaveDataStr4Show(regidx + i));
            sb.append("]</span>&nbsp;");
        }
    }
}
