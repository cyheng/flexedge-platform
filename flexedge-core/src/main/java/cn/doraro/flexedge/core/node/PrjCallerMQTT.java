package cn.doraro.flexedge.core.node;

import cn.doraro.flexedge.core.conn.mqtt.MqttEndPoint;
import cn.doraro.flexedge.core.node.NodeMsg.MsgTp;
import cn.doraro.flexedge.core.util.xmldata.XmlData;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.HashMap;

public class PrjCallerMQTT extends PrjCaller {
    static HashMap<String, MsgTp> topic2mtp = new HashMap<>();

    static {
        topic2mtp.put("_n/req/", MsgTp.req);
        topic2mtp.put("_n/resp/", MsgTp.resp);
        topic2mtp.put("_n/push/", MsgTp.push);
    }

    MqttEndPoint mqttEP = null;
    private MqttCallback mqttCB = new MqttCallback() {

        @Override
        public void connectionLost(Throwable cause) {

        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            NodeMsg nm = parseNodeMsg(topic, message.getPayload());
            if (nm == null)
                return;//do nothing
            onRecvedMsg(nm);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }

    };

    public void init() {
        if (mqttEP != null)
            return;

        String prjid = this.getPrjId();
        String shareid = this.getSharePrjId();
        mqttEP = new MqttEndPoint("flexedge_prj_caller_" + prjid);

        mqttEP.withParamsXml(this.getParamXD()).withCallback(mqttCB)
                .withListenTopic("_n/" + shareid + "/" + prjid + "/#")
                .withListenTopic("_n/" + shareid + "/_/#");
    }

    public MqttEndPoint getMqttEP() {
        init();

        return mqttEP;
    }

    public XmlData transParamsJSON2Xml(JSONObject jo) throws Exception {
        MqttEndPoint ep = getMqttEP();
        ep.withParamsJSON(jo, true);
        XmlData tmpxd = new XmlData();
        ep.transParamsToXml(tmpxd, true);
        return tmpxd;
    }

    @Override
    protected void sendMsg(NodeMsg nm) throws Exception {
        String topic = MqttUtil.calMqttTopic(nm);
        getMqttEP().publish(topic, nm.getContent());
    }

    @Override
    public boolean isValid() {
        return getMqttEP().isValid();
    }

    public boolean isConnReady() {
        if (mqttEP == null)
            return false;
        return mqttEP.isConnReady();
    }

    public String getConnErrInfo() {
        if (mqttEP == null)
            return "no connection";
        if (mqttEP.isConnReady())
            return null;
        return mqttEP.getConnErrInfo();
    }

    public void disconnect() {
        if (mqttEP == null)
            return;
        mqttEP.disconnect();
        mqttEP = null;
    }

    public void checkConn() {
        getMqttEP().checkConn();
    }

    protected NodeMsg parseNodeMsg(String mqtt_topic, byte[] msg) {
        return MqttUtil.calNodeMsg(mqtt_topic, msg);
    }


}
