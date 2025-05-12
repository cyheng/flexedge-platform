// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.ext.msg_net;

import cn.doraro.flexedge.core.msgnet.MNMsg;
import cn.doraro.flexedge.core.msgnet.MNNodeStart;
import cn.doraro.flexedge.core.msgnet.RTOut;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.jt.JSONTemp;
import org.json.JSONObject;

public class KafkaIn_NS extends MNNodeStart {
    String topic;

    public KafkaIn_NS() {
        this.topic = null;
    }

    public String getTP() {
        return "kafka_in";
    }

    public String getTPTitle() {
        return "Kafka In";
    }

    public JSONTemp getInJT() {
        return null;
    }

    public JSONTemp getOutJT() {
        return null;
    }

    public int getOutNum() {
        return 1;
    }

    public String getColor() {
        return "#debed7";
    }

    public String getIcon() {
        return "\\uf0ec";
    }

    public String getTopic() {
        return this.topic;
    }

    public boolean isParamReady(final StringBuilder failedr) {
        if (Convert.isNullOrEmpty(this.topic)) {
            failedr.append("no topic set");
            return false;
        }
        return true;
    }

    public JSONObject getParamJO() {
        final JSONObject jo = new JSONObject();
        jo.putOpt("topic", (Object) this.topic);
        return jo;
    }

    protected void setParamJO(final JSONObject jo) {
        this.topic = jo.optString("topic", (String) null);
    }

    void RT_onTopicMsgRecv(final String topic, final String msg) {
        final MNMsg m = new MNMsg().asPayload((Object) msg).asTopic(topic);
        this.RT_sendMsgOut(RTOut.createOutAll(m));
    }
}
