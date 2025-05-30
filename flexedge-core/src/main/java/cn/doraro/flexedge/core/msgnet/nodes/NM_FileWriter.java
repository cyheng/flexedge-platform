package cn.doraro.flexedge.core.msgnet.nodes;

import cn.doraro.flexedge.core.msgnet.MNConn;
import cn.doraro.flexedge.core.msgnet.MNMsg;
import cn.doraro.flexedge.core.msgnet.MNNodeMid;
import cn.doraro.flexedge.core.msgnet.RTOut;
import org.json.JSONObject;

public class NM_FileWriter extends MNNodeMid {

    @Override
    public int getOutNum() {
        return 1;
    }

    @Override
    public String getTP() {
        return "file_w";
    }

    @Override
    public String getTPTitle() {
        return g("file_w");
    }

    @Override
    public String getColor() {
        return "#e7b686";
    }

    @Override
    public String getIcon() {
        return "PK_filew";
    }

    @Override
    public boolean isParamReady(StringBuilder failedr) {
        return false;
    }

    @Override
    public JSONObject getParamJO() {
        return null;
    }

    @Override
    protected void setParamJO(JSONObject jo) {

    }


    @Override
    protected RTOut RT_onMsgIn(MNConn in_conn, MNMsg msg) throws Exception {
        msg.getHeadVal("create_file");
        return null;
    }

    @Override
    public String RT_getInTitle() {
        return "In Msg Payload";
    }
}
