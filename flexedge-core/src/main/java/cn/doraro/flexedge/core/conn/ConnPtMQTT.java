package cn.doraro.flexedge.core.conn;

import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.conn.mqtt.MqttEndPoint;
import cn.doraro.flexedge.core.util.xmldata.XmlData;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConnPtMQTT extends ConnPtMSGNor // implements ConnDevFindable
{
    MqttCallback mqttCB = new MqttCallback() {

        @Override
        public void connectionLost(Throwable cause) {

        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }

    };
    private List<String> topics = null;

    public ConnPtMQTT() {

    }

    @Override
    public String getConnType() {
        return "mqtt";
    }

    public List<String> getMsgTopics() {
        return topics;
    }

    public void RT_checkConn() {
    }

    @Override
    public XmlData toXmlData() {
        XmlData xd = super.toXmlData();

//		xd.setParamValue("sor_tp", sorTp.toString());
//		if(initJS!=null)
//			xd.setParamValue("init_js", initJS);
//		if (transJS != null)
//			xd.setParamValue("trans_js", transJS);
        if (topics != null)
            xd.setParamValues("topics", topics);
//		if (encod != null)
//			xd.setParamValue("encod", encod);
        return xd;
    }

    @Override
    public boolean fromXmlData(XmlData xd, StringBuilder failedr) {
        boolean r = super.fromXmlData(xd, failedr);

//		String stp = xd.getParamValueStr("sor_tp", null);
//		if (Convert.isNotNullEmpty(stp))
//			sorTp = DataTp.valueOf(stp);
//		if (sorTp == null)
//			sorTp = DataTp.json;
//		initJS = xd.getParamValueStr("init_js");
//		transJS = xd.getParamValueStr("trans_js");

        topics = xd.getParamXmlValStrs("topics");
//		encod = xd.getParamValueStr("encod");
//		clearCache();
        return r;
    }

    protected void injectByJson(JSONObject jo) throws Exception {
        JSONArray jotps = jo.optJSONArray("topics");
        ArrayList<String> tps = new ArrayList<>();
        if (jotps != null) {
            for (int i = 0, n = jotps.length(); i < n; i++) {
                String tp = jotps.getString(i);
                StringBuilder failedr = new StringBuilder();
                if (!MqttEndPoint.checkTopicValid(tp, failedr))
                    throw new Exception(failedr.toString());
                tps.add(tp);
            }
        }
        topics = tps;

        super.injectByJson(jo);

//		String stp = jo.optString("sor_tp");
//		if (Convert.isNotNullEmpty(stp))
//			sorTp = DataTp.valueOf(stp);
//		if (sorTp == null)
//			sorTp = DataTp.json;
//		this.topics = tps;
//		this.initJS = jo.optString("init_js");
//		this.transJS = jo.optString("trans_js");
//		this.encod = jo.optString("encod");
//		clearCache();
    }

    //transient private int initInitOk = 0 ;

    @Override
    public String getStaticTxt() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isPassiveRecv() {
        return true;
    }

    @Override
    public boolean isConnReady() {
        ConnProMQTT cp = (ConnProMQTT) this.getConnProvider();
        return cp.isMQTTConnected();
    }

    public String getConnErrInfo() {
        return "";

    }

    public boolean sendMsg(String topic, byte[] bs) throws Exception {
        // this.publish(topic, bs, 0);
        return true;
    }

    protected boolean readMsgToFile(File f) throws Exception {
        return false;
    }

//	synchronized void disconnect() // throws IOException
//	{
//		// getMqttEP().disconnect();
//	}

    public void runOnWrite(UATag tag, Object val) throws Exception {
        throw new Exception("no impl");
        // it may send some msg
    }
}
