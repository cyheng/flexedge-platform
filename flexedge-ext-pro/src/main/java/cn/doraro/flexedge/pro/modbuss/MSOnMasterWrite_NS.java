

package cn.doraro.flexedge.pro.modbuss;

import cn.doraro.flexedge.core.msgnet.MNMsg;
import cn.doraro.flexedge.core.msgnet.MNNodeStart;
import cn.doraro.flexedge.core.msgnet.RTOut;
import org.json.JSONObject;

public class MSOnMasterWrite_NS extends MNNodeStart {
    public static final String TP = "ms_on_master_w";
    private static String OUT0;
    private static String OUT1;

    static {
        MSOnMasterWrite_NS.OUT0 = "Out By Var";
        MSOnMasterWrite_NS.OUT1 = "Out By Bind Tag<br><br><pre>";
        final JSONObject tmpjo = new JSONObject();
        tmpjo.put("tag", (Object) "ch1.dev1.tag1");
        tmpjo.put("value", true);
        MSOnMasterWrite_NS.OUT1 = String.valueOf(MSOnMasterWrite_NS.OUT1) + tmpjo.toString(4);
        MSOnMasterWrite_NS.OUT1 = String.valueOf(MSOnMasterWrite_NS.OUT1) + "</pre>";
    }

    String devId;

    public MSOnMasterWrite_NS() {
        this.devId = null;
    }

    public int getOutNum() {
        return 2;
    }

    public String getTP() {
        return "ms_on_master_w";
    }

    public String getTPTitle() {
        return "On Master Write";
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

    void RT_fireVarWriteOut(final JSONObject jo) {
        final MNMsg msg = new MNMsg().asPayload((Object) jo);
        this.RT_sendMsgOut(RTOut.createOutIdx().asIdxMsg(0, msg));
    }

    void RT_fireTagWriteOut(final JSONObject jo) {
        final MNMsg msg = new MNMsg().asPayload((Object) jo);
        this.RT_sendMsgOut(RTOut.createOutIdx().asIdxMsg(1, msg));
    }

    public String RT_getOutTitle(final int idx) {
        if (idx == 0) {
            return MSOnMasterWrite_NS.OUT0;
        }
        if (idx == 1) {
            return MSOnMasterWrite_NS.OUT1;
        }
        return null;
    }
}
