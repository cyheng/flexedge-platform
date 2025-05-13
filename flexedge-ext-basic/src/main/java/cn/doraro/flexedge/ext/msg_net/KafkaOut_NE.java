

package cn.doraro.flexedge.ext.msg_net;

import cn.doraro.flexedge.core.msgnet.MNConn;
import cn.doraro.flexedge.core.msgnet.MNMsg;
import cn.doraro.flexedge.core.msgnet.MNNodeEnd;
import cn.doraro.flexedge.core.msgnet.RTOut;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.jt.JSONTemp;
import org.json.JSONObject;

public class KafkaOut_NE extends MNNodeEnd {
    public static final String TP = "kafka_out";
    String topic;

    public KafkaOut_NE() {
        this.topic = null;
    }

    public String getTP() {
        return "kafka_out";
    }

    public String getTPTitle() {
        return "Kafka Out";
    }

    public JSONTemp getInJT() {
        return null;
    }

    public JSONTemp getOutJT() {
        return null;
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

    protected RTOut RT_onMsgIn(final MNConn in_conn, final MNMsg msg) {
        final Kafka_M km = (Kafka_M) this.getOwnRelatedModule();
        if (km == null) {
            return null;
        }
        km.RT_send(this.topic, msg.getPayloadStr());
        this.RT_DEBUG_INF.fire("msg_in", "topic=" + this.topic + " out ", msg.getPayloadStr());
        return null;
    }
}
