// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.ext.msg_net;

import cn.doraro.flexedge.core.msgnet.MNConn;
import cn.doraro.flexedge.core.msgnet.MNMsg;
import cn.doraro.flexedge.core.msgnet.MNNodeMid;
import cn.doraro.flexedge.core.msgnet.RTOut;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.Lan;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import org.json.JSONObject;

public class TCPClient_NM extends MNNodeMid {
    static ILogger log;
    static Lan lan;

    static {
        TCPClient_NM.log = LoggerManager.getLogger((Class) TCPClient_NM.class);
        TCPClient_NM.lan = Lan.getLangInPk((Class) TCPClient_NM.class);
    }

    String serverAddr;
    int serverPort;
    int localPort;
    RetMode retMode;
    CloseMode closeMode;

    public TCPClient_NM() {
        this.localPort = -1;
        this.retMode = RetMode.buf;
        this.closeMode = CloseMode.never;
    }

    public int getOutNum() {
        return 3;
    }

    public String getTP() {
        return "tcp_client";
    }

    public String getTPTitle() {
        return "TCP Client";
    }

    public String getColor() {
        return "#c0c0c0";
    }

    public String getIcon() {
        return "\\uf0ec";
    }

    public boolean isParamReady(final StringBuilder failedr) {
        if (Convert.isNullOrEmpty(this.serverAddr)) {
            failedr.append("no server set");
            return false;
        }
        if (this.serverPort <= 0) {
            failedr.append("no server port set");
            return false;
        }
        return true;
    }

    public JSONObject getParamJO() {
        final JSONObject jo = new JSONObject();
        jo.putOpt("server", (Object) this.serverAddr);
        jo.putOpt("port", (Object) this.serverPort);
        jo.putOpt("loc_port", (Object) this.serverAddr);
        jo.putOpt("server", (Object) this.serverAddr);
        jo.putOpt("server", (Object) this.serverAddr);
        return jo;
    }

    protected void setParamJO(final JSONObject jo) {
    }

    protected RTOut RT_onMsgIn(final MNConn in_conn, final MNMsg msg) throws Exception {
        return null;
    }

    public String RT_getOutColor(final int idx) {
        if (idx == 1) {
            return "#25b541";
        }
        if (idx == 2) {
            return "#e13c2f";
        }
        return null;
    }

    public String RT_getOutTitle(final int idx) {
        switch (idx) {
            case 0: {
                return "Received Data in payload";
            }
            case 1: {
                return "Connect server ready msg";
            }
            case 2: {
                return "Connection is broken msg";
            }
            default: {
                return null;
            }
        }
    }

    public enum CloseMode {
        never("never", 0, 0),
        aft_fixed_to("aft_fixed_to", 1, 1),
        recv_char("recv_char", 2, 2),
        recv_char_num("recv_char_num", 3, 3),
        immediately("immediately", 4, 4);

        private final int val;

        private CloseMode(final String name, final int ordinal, final int v) {
            this.val = v;
        }

        public int getVal() {
            return this.val;
        }

        public String getTitle() {
            return TCPClient_NM.lan.g(this.name());
        }
    }

    public enum RetMode {
        buf("buf", 0),
        str("str", 1);

        private RetMode(final String name, final int ordinal) {
        }

        public String getTitle() {
            return TCPClient_NM.lan.g(this.name());
        }
    }
}
