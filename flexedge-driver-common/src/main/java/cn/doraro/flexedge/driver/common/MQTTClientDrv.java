// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.*;
import cn.doraro.flexedge.core.basic.PropGroup;
import cn.doraro.flexedge.core.basic.PropItem;
import cn.doraro.flexedge.core.util.CompressUUID;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.Lan;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MQTTClientDrv extends DevDriver {
    String host;
    int port;
    ArrayList<String> topics;
    private MqttClient client;
    private MqttConnectOptions options;

    public MQTTClientDrv() {
        this.host = null;
        this.port = 1883;
        this.topics = null;
        this.client = null;
        this.options = null;
    }

    public DevDriver copyMe() {
        return new MQTTClientDrv();
    }

    public String getName() {
        return "mqtt_client";
    }

    public String getTitle() {
        return "MQTT Client";
    }

    public List<PropGroup> getPropGroupsForCh(final UACh ch) {
        final Lan lan = Lan.getPropLangInPk((Class) this.getClass());
        final PropGroup r = new PropGroup("mqtt_conn", lan);
        r.addPropItem(new PropItem("server_host", lan, PropItem.PValTP.vt_str, false, (String[]) null, (Object[]) null, (Object) "localhost"));
        r.addPropItem(new PropItem("server_port", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 1883));
        r.addPropItem(new PropItem("clientid", lan, PropItem.PValTP.vt_str, false, (String[]) null, (Object[]) null, (Object) CompressUUID.createNewId()));
        r.addPropItem(new PropItem("username", lan, PropItem.PValTP.vt_str, false, (String[]) null, (Object[]) null, (Object) ""));
        r.addPropItem(new PropItem("userpsw", lan, PropItem.PValTP.vt_str, false, (String[]) null, (Object[]) null, (Object) ""));
        r.addPropItem(new PropItem("conn_to_sec", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 10));
        r.addPropItem(new PropItem("ka_int", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 10));
        final ArrayList<PropGroup> pgs = new ArrayList<PropGroup>();
        pgs.add(r);
        return pgs;
    }

    public List<PropGroup> getPropGroupsForDev() {
        final Lan lan = Lan.getPropLangInPk((Class) this.getClass());
        final PropGroup gp = new PropGroup("mqtt_tag", lan);
        gp.addPropItem(new PropItem("topic", lan, PropItem.PValTP.vt_str, false, (String[]) null, (Object[]) null, (Object) ""));
        final List<PropGroup> pgs = new ArrayList<PropGroup>();
        pgs.add(gp);
        return pgs;
    }

    public DevAddr getSupportAddr() {
        return new MQTTClientAddr();
    }

    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return true;
    }

    protected boolean RT_initDriver(final StringBuilder failedr) throws Exception {
        final UACh ch = this.getBelongToCh();
        this.host = ch.getOrDefaultPropValueStr("mqtt_conn", "server_host", (String) null);
        if (Convert.isNullOrEmpty(this.host)) {
            failedr.append("no server host set");
            return false;
        }
        this.topics = new ArrayList<String>();
        this.port = ch.getOrDefaultPropValueInt("mqtt_conn", "server_port", 1833);
        final String serveruri = "tcp://" + this.host + ":" + this.port;
        final int qos = 1;
        final String clientId = ch.getOrDefaultPropValueStr("mqtt_conn", "clientid", "client1");
        final String userName = ch.getOrDefaultPropValueStr("mqtt_conn", "username", "");
        final String userPsw = ch.getOrDefaultPropValueStr("mqtt_conn", "userpsw", "");
        final int connTimeout = ch.getOrDefaultPropValueInt("mqtt_conn", "conn_to_sec", 10);
        final int keepAliveInterval = ch.getOrDefaultPropValueInt("mqtt_conn", "ka_int", 20);
        final List<UADev> devs = this.getBelongToCh().getDevs();
        if (devs == null || devs.size() <= 0) {
            failedr.append("no device found ");
            return false;
        }
        for (final UADev dev : devs) {
            final String topic = dev.getOrDefaultPropValueStr("mqtt_tag", "topic", (String) null);
            if (Convert.isNullOrEmpty(topic)) {
                continue;
            }
            this.topics.add(topic);
        }
        if (this.topics.size() <= 0) {
            failedr.append("no device mqtt topic found");
            return false;
        }
        this.client = new MqttClient(serveruri, clientId, (MqttClientPersistence) new MemoryPersistence());
        (this.options = new MqttConnectOptions()).setCleanSession(true);
        this.options.setUserName(userName);
        this.options.setPassword(userPsw.toCharArray());
        this.options.setConnectionTimeout(connTimeout);
        this.options.setKeepAliveInterval(keepAliveInterval);
        this.client.setCallback((MqttCallback) new MqttCallback() {
            public void connectionLost(final Throwable cause) {
            }

            public void messageArrived(final String topic, final MqttMessage message) throws Exception {
                final String cont = new String(message.getPayload());
                MQTTClientDrv.this.onJsonMsgArrived(topic, cont);
            }

            public void deliveryComplete(final IMqttDeliveryToken token) {
                System.out.println("deliveryComplete---------" + token.isComplete());
            }
        });
        return true;
    }

    private void onJsonMsgArrived(final String topic, final String jsonstr) {
        JSONObject jobj = null;
        try {
            jobj = new JSONObject(jsonstr);
        } catch (final Exception e) {
            System.out.println("invalid payload,it's not json {} format");
            return;
        }
        final UACh ch = this.getBelongToCh();
        for (final UADev uad : ch.getDevs()) {
            final String topicfilter = uad.getOrDefaultPropValueStr("mqtt_tag", "topic", (String) null);
            if (Convert.isNullOrEmpty(topicfilter)) {
                continue;
            }
            if (!MqttTopic.isMatched(topicfilter, topic)) {
                continue;
            }
            final List<UATag> tags = uad.listTags();
            final StringBuilder failedr = new StringBuilder();
            for (final UATag tag : tags) {
                final MQTTClientAddr da = (MQTTClientAddr) tag.getDevAddr(failedr);
                if (da == null) {
                    continue;
                }
                final String tagtopic = da.getMQTTTopic();
                if (!topic.contentEquals(tagtopic)) {
                    continue;
                }
                final List<String> jsonpath = da.getPayloadJSONPath();
                if (jsonpath == null) {
                    continue;
                }
                final int s;
                if ((s = jsonpath.size()) <= 0) {
                    continue;
                }
                JSONObject curjob = jobj;
                Object objv = null;
                for (int i = 0; i < s; ++i) {
                    final String pn = jsonpath.get(i);
                    if (i >= s - 1) {
                        objv = jobj.opt(pn);
                        break;
                    }
                    curjob = curjob.optJSONObject(pn);
                    if (curjob == null) {
                        break;
                    }
                }
                da.RT_setVal(objv);
            }
        }
    }

    protected void RT_runInLoop() throws Exception {
        if (this.client.isConnected()) {
            return;
        }
        try {
            this.client.connect(this.options);
            for (final String topic : this.topics) {
                this.client.subscribe(topic, 1);
            }
            System.out.println(String.valueOf(this.getBelongToCh().getTitle()) + " MQTTClientDrv connect to " + this.host + ":" + this.port + " ok");
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    protected void RT_endDriver() throws Exception {
        this.client.disconnect();
    }

    public void publish(final String topic, final int qos, final String message) throws Exception {
        final MqttTopic mt = this.client.getTopic(topic);
        final MqttMessage msg = new MqttMessage();
        msg.setQos(qos);
        msg.setRetained(false);
        msg.setPayload(message.getBytes());
        final MqttDeliveryToken token = mt.publish(msg);
    }

    public boolean supportDevFinder() {
        return false;
    }

    public List<PropGroup> getPropGroupsForDevDef() {
        return null;
    }

    public Class<? extends ConnPt> supportConnPtClass() {
        return null;
    }

    public List<PropGroup> getPropGroupsForDevInCh(final UADev d) {
        return null;
    }

    protected void RT_onConnReady(final ConnPt cp, final UACh ch, final UADev dev) {
    }

    protected void RT_onConnInvalid(final ConnPt cp, final UACh ch, final UADev dev) {
    }

    protected boolean RT_runInLoop(final UACh ch, final UADev dev, final StringBuilder failedr) throws Exception {
        return false;
    }

    public boolean RT_writeVal(final UACh ch, final UADev dev, final UATag tag, final DevAddr da, final Object v) {
        return false;
    }

    public boolean RT_writeVals(final UACh ch, final UADev dev, final UATag[] tags, final DevAddr[] da, final Object[] v) {
        return false;
    }
}
