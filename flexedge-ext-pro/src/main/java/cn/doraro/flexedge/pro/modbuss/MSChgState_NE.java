// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.modbuss;

import cn.doraro.flexedge.core.msgnet.MNConn;
import cn.doraro.flexedge.core.msgnet.MNMsg;
import cn.doraro.flexedge.core.msgnet.MNNodeEnd;
import cn.doraro.flexedge.core.msgnet.RTOut;
import cn.doraro.flexedge.core.util.Convert;
import org.json.JSONArray;
import org.json.JSONObject;

public class MSChgState_NE extends MNNodeEnd {
    public String getTP() {
        return "ms_chg_state";
    }

    public String getTPTitle() {
        return "Change Bus/Device State";
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
        return jo;
    }

    protected void setParamJO(final JSONObject jo) {
    }

    protected RTOut RT_onMsgIn(final MNConn in_conn, final MNMsg msg) throws Exception {
        final MSBus_M bus = (MSBus_M) this.getOwnRelatedModule();
        final JSONObject jo = msg.getPayloadJO((JSONObject) null);
        if (jo == null) {
            return null;
        }
        final JSONObject busjo = jo.optJSONObject("bus");
        if (busjo != null && busjo.has("valid")) {
            bus.RT_setBusValid(busjo.getBoolean("valid"));
        }
        final JSONArray devjarr = jo.optJSONArray("devs");
        if (devjarr != null) {
            for (int n = devjarr.length(), i = 0; i < n; ++i) {
                final JSONObject devjo = devjarr.getJSONObject(i);
                final String devn = devjo.optString("name");
                if (!Convert.isNullOrEmpty(devn)) {
                    if (devjo.has("valid")) {
                        final SlaveDev sdev = bus.getDevByName(devn);
                        if (sdev != null) {
                            final boolean v = devjo.getBoolean("valid");
                            sdev.RT_setDevValid(v);
                        }
                    }
                }
            }
        }
        return null;
    }

    public String RT_getInTitle() {
        final MSBus_M bus = (MSBus_M) this.getOwnRelatedModule();
        final StringBuilder sb = new StringBuilder();
        sb.append("<pre>{");
        sb.append("\r\n  \"bus\":{\"valid\":bool},");
        sb.append("\r\n  \"devs\":[");
        boolean bfirst = true;
        for (final SlaveDev dev : bus.listDevItems()) {
            if (bfirst) {
                bfirst = false;
                sb.append("\r\n     {\"name\":\"" + dev.getName() + "\",\"valid\":bool}");
            } else {
                sb.append("\r\n    ,{\"name\":\"" + dev.getName() + "\",\"valid\":bool}");
            }
        }
        sb.append("\r\n  ]");
        sb.append("\r\n}</pre>");
        return sb.toString();
    }
}
