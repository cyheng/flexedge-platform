package cn.doraro.flexedge.core.msgnet.nodes;

import cn.doraro.flexedge.core.msgnet.IMNOnOff;
import cn.doraro.flexedge.core.msgnet.MNMsg;
import cn.doraro.flexedge.core.msgnet.MNNodeStart;
import cn.doraro.flexedge.core.msgnet.RTOut;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.jt.JSONTemp;
import org.json.JSONObject;

public class NS_OuterTrigger extends MNNodeStart implements IMNOnOff {
    String triggerName = null;

    @Override
    public JSONTemp getInJT() {
        return null;
    }

    @Override
    public JSONTemp getOutJT() {
        return null;
    }

    @Override
    public int getOutNum() {
        return 1;
    }

    @Override
    public String getTP() {
        return "outer";
    }

    @Override
    public String getTPTitle() {
        return g("outer");
    }

    @Override
    public String getColor() {
        return "#dddddd";
    }

    @Override
    public String getIcon() {
        return "PK_outer";// "\\uf0a4";
    }

    @Override
    public boolean isParamReady(StringBuilder failedr) {
        return true;
    }

    @Override
    public JSONObject getParamJO() {
        JSONObject jo = new JSONObject();
        jo.putOpt("trigger_name", this.triggerName);
        return jo;
    }

    @Override
    protected void setParamJO(JSONObject jo) {
        this.triggerName = jo.optString("trigger_name");
    }

    public boolean RT_triggerByOnOff(StringBuilder failedr) {
        if (!this.supportInOnOff()) {
            failedr.append("not support");
            return false;
        }

        MNMsg msg = new MNMsg();

        RT_sendMsgOut(RTOut.createOutAll(msg));
        return true;
    }

    public boolean RT_triggerByOuter(String triggername, JSONObject payload) {
        if (Convert.isNotNullEmpty(this.triggerName)
                && !this.triggerName.equals(triggername))
            return false;

        MNMsg msg = new MNMsg().asPayload(payload);
        RT_sendMsgOut(RTOut.createOutAll(msg));
        return true;
    }
}
