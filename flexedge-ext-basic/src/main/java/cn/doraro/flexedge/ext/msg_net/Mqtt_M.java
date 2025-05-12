// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.ext.msg_net;

import cn.doraro.flexedge.core.msgnet.MNBase;
import cn.doraro.flexedge.core.msgnet.RTOut;
import cn.doraro.flexedge.core.msgnet.MNMsg;
import java.sql.SQLException;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import cn.doraro.flexedge.core.msgnet.MNNet;
import java.util.List;
import cn.doraro.flexedge.core.msgnet.MNNode;
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONObject;
import cn.doraro.flexedge.core.util.Convert;
import java.util.Iterator;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttClient;
import cn.doraro.flexedge.core.conn.mqtt.MqttEndPoint;
import java.util.ArrayList;
import cn.doraro.flexedge.core.util.logger.ILogable;
import cn.doraro.flexedge.core.msgnet.IMNRunner;
import cn.doraro.flexedge.core.msgnet.MNModule;

public class Mqtt_M extends MNModule implements IMNRunner, ILogable
{
    String brokerHost;
    int brokerPort;
    String user;
    String psw;
    int connTimeoutSec;
    int connKeepAliveInterval;
    ArrayList<SendConf> sendConfs;
    ArrayList<RecvConf> recvConfs;
    private Mqtt_SQLitePersistence sqlitePsersis;
    private transient MqttEndPoint mqttEP;
    MqttClient mqttClient;
    MqttConnectOptions options;
    private boolean bRTInitOk;
    Thread RT_th;
    boolean RT_bRun;
    private MqttCallback RT_mqttCB;
    private Runnable mqttMRunner;
    
    public Mqtt_M() {
        this.brokerPort = 1883;
        this.user = "";
        this.psw = "";
        this.connTimeoutSec = 30;
        this.connKeepAliveInterval = 60;
        this.sendConfs = new ArrayList<SendConf>();
        this.recvConfs = new ArrayList<RecvConf>();
        this.sqlitePsersis = null;
        this.mqttEP = null;
        this.mqttClient = null;
        this.options = null;
        this.bRTInitOk = false;
        this.RT_th = null;
        this.RT_bRun = false;
        this.RT_mqttCB = (MqttCallback)new MqttCallback() {
            public void connectionLost(final Throwable cause) {
                System.out.println(" Mqtt_M conn lost");
            }
            
            public void messageArrived(final String topic, final MqttMessage message) throws Exception {
                Mqtt_M.this.RT_onRecvedMsg(topic, message.getPayload());
            }
            
            public void deliveryComplete(final IMqttDeliveryToken token) {
                try {
                    final MqttMessage mm = token.getMessage();
                }
                catch (final MqttException e) {
                    e.printStackTrace();
                }
            }
        };
        this.mqttMRunner = new Runnable() {
            @Override
            public void run() {
                Mqtt_M.this.consumerRun();
            }
        };
    }
    
    public SendConf getSendConfById(final String id) {
        for (final SendConf sc : this.sendConfs) {
            if (sc.id.equals(id)) {
                return sc;
            }
        }
        return null;
    }
    
    public RecvConf getRecvConfById(final String id) {
        for (final RecvConf sc : this.recvConfs) {
            if (sc.id.equals(id)) {
                return sc;
            }
        }
        return null;
    }
    
    public String getTP() {
        return "mqtt";
    }
    
    public String getTPTitle() {
        return "MQTT";
    }
    
    public String getColor() {
        return "#debed7";
    }
    
    public String getIcon() {
        return "PK_bridge";
    }
    
    public boolean isParamReady(final StringBuilder failedr) {
        if (Convert.isNullOrEmpty(this.brokerHost)) {
            failedr.append("no broker host");
            return false;
        }
        return true;
    }
    
    public JSONObject getParamJO() {
        final JSONObject jo = new JSONObject();
        jo.put("host", (Object)this.brokerHost);
        jo.put("port", this.brokerPort);
        jo.put("user", (Object)this.user);
        jo.put("psw", (Object)this.psw);
        jo.put("to_sec", this.connTimeoutSec);
        jo.put("keep_intv", this.connKeepAliveInterval);
        JSONArray jar = new JSONArray();
        for (final SendConf sc : this.sendConfs) {
            jar.put((Object)sc.toJO());
        }
        jo.put("send_confs", (Object)jar);
        jar = new JSONArray();
        for (final RecvConf rc : this.recvConfs) {
            jar.put((Object)rc.toJO());
        }
        jo.put("recv_confs", (Object)jar);
        return jo;
    }
    
    protected void setParamJO(final JSONObject jo) {
        this.brokerHost = jo.optString("host", "");
        this.brokerPort = jo.optInt("port", 9092);
        this.user = jo.optString("user", "");
        this.psw = jo.optString("psw", "");
        this.connTimeoutSec = jo.optInt("to_sec", 30);
        this.connKeepAliveInterval = jo.optInt("keep_intv", -1);
        JSONArray jarr = jo.optJSONArray("send_confs");
        final ArrayList<SendConf> scs = new ArrayList<SendConf>();
        if (jarr != null) {
            for (int n = jarr.length(), i = 0; i < n; ++i) {
                final JSONObject tmpjo = jarr.getJSONObject(i);
                final SendConf sc = new SendConf();
                sc.fromJO(tmpjo);
                scs.add(sc);
            }
            this.sendConfs = scs;
        }
        jarr = jo.optJSONArray("recv_confs");
        final ArrayList<RecvConf> rcs = new ArrayList<RecvConf>();
        if (jarr != null) {
            for (int n2 = jarr.length(), j = 0; j < n2; ++j) {
                final JSONObject tmpjo2 = jarr.getJSONObject(j);
                final RecvConf sc2 = new RecvConf();
                sc2.fromJO(tmpjo2);
                rcs.add(sc2);
            }
            this.recvConfs = rcs;
        }
    }
    
    public void checkAfterSetParam() {
        try {
            this.updateSendRecvNodes();
        }
        catch (final Exception ee) {
            ee.printStackTrace();
        }
    }
    
    private void updateSendRecvNodes() throws Exception {
        final List<MNNode> nodes = this.getRelatedNodes();
        final float me_x = this.getX();
        final float me_y = this.getY();
        final HashSet<String> sendmids = new HashSet<String>();
        final HashSet<String> recvids = new HashSet<String>();
        if (this.sendConfs != null) {
            for (final SendConf sc : this.sendConfs) {
                sendmids.add(sc.id);
            }
        }
        if (this.recvConfs != null) {
            for (final RecvConf rc : this.recvConfs) {
                recvids.add(rc.id);
            }
        }
        final MNNet net = this.getBelongTo();
        boolean bdirty = false;
        if (nodes != null) {
            for (final MNNode n : nodes) {
                if (n instanceof MqttOut_NE) {
                    final MqttOut_NE num = (MqttOut_NE)n;
                    if (Convert.isNullOrEmpty(num.sendId) || !sendmids.contains(num.sendId)) {
                        net.delNodeById(n.getId(), false);
                        bdirty = true;
                    }
                    else {
                        final SendConf dev = this.getSendConfById(num.sendId);
                        sendmids.remove(num.sendId);
                        num.setTitle(String.valueOf(dev.getShowTitle()) + "[" + dev.topic + "]");
                    }
                }
                if (n instanceof MqttIn_NS) {
                    final MqttIn_NS nnn = (MqttIn_NS)n;
                    if (Convert.isNullOrEmpty(nnn.recvId) || !recvids.contains(nnn.recvId)) {
                        net.delNodeById(n.getId(), false);
                        bdirty = true;
                    }
                    else {
                        final RecvConf dev2 = this.getRecvConfById(nnn.recvId);
                        recvids.remove(nnn.recvId);
                        nnn.setTitle(String.valueOf(dev2.getShowTitle()) + "[" + dev2.topic + "]");
                    }
                }
            }
        }
        final MNNode sup_in = this.getSupportedNodeByTP("mqtt_in");
        final MNNode sup_out = this.getSupportedNodeByTP("mqtt_out");
        int newcc = 0;
        for (final String addid : sendmids) {
            ++newcc;
            final SendConf dev3 = this.getSendConfById(addid);
            final MqttOut_NE newn = (MqttOut_NE)net.createNewNodeInModule((MNModule)this, sup_out, me_x - 100.0f, me_y - 20.0f - 33 * newcc, (String)null, false);
            newn.sendId = addid;
            newn.setTitle(String.valueOf(dev3.getShowTitle()) + "[" + dev3.topic + "]");
            bdirty = true;
        }
        newcc = 0;
        for (final String addid : recvids) {
            ++newcc;
            final RecvConf dev4 = this.getRecvConfById(addid);
            final MqttIn_NS newn2 = (MqttIn_NS)net.createNewNodeInModule((MNModule)this, sup_in, me_x + 200.0f, me_y - 20.0f - 53 * newcc, (String)null, false);
            newn2.recvId = addid;
            newn2.setTitle(String.valueOf(dev4.getShowTitle()) + "[" + dev4.topic + "]");
            bdirty = true;
        }
        if (bdirty) {
            net.save();
        }
    }
    
    public void publish(final String topic, final byte[] data) throws Exception {
        this.publish(topic, data, 0);
    }
    
    public void publish(final String topic, final byte[] data, final int qos) throws Exception {
        this.getMqttClient().publish(topic, data, qos, false);
    }
    
    public void publish(final String topic, final String txt) throws Exception {
        this.publish(topic, txt.getBytes("utf-8"), 1);
    }
    
    public Mqtt_SQLitePersistence getPersistence() {
        if (this.sqlitePsersis != null) {
            return this.sqlitePsersis;
        }
        try {
            final String client_id = "mn_" + this.getId();
            return this.sqlitePsersis = new Mqtt_SQLitePersistence(client_id);
        }
        catch (final Exception ee) {
            ee.printStackTrace();
            return null;
        }
    }
    
    protected MqttClient getMqttClient() throws MqttException, SQLException {
        if (this.mqttClient != null) {
            return this.mqttClient;
        }
        final String broker_url = "tcp://" + this.brokerHost + ":" + this.brokerPort;
        final String client_id = "mn_" + this.getId();
        final MqttClient mc = new MqttClient(broker_url, client_id, (MqttClientPersistence)this.getPersistence());
        (this.options = new MqttConnectOptions()).setAutomaticReconnect(true);
        this.options.setCleanSession(false);
        this.options.setUserName(this.user);
        this.options.setPassword(this.psw.toCharArray());
        this.options.setConnectionTimeout(this.connTimeoutSec);
        if (this.connKeepAliveInterval >= 0) {
            this.options.setKeepAliveInterval(this.connKeepAliveInterval);
        }
        mc.setCallback(this.RT_mqttCB);
        return this.mqttClient = mc;
    }
    
    protected boolean RT_init(final StringBuilder failedr) {
        this.bRTInitOk = false;
        if (Convert.isNullOrEmpty(this.brokerHost) || this.brokerPort <= 0) {
            failedr.append("no borker host port set");
            return false;
        }
        try {
            final MqttClient mc = this.getMqttClient();
            final ArrayList<String> recv_tps = new ArrayList<String>();
            if (this.recvConfs != null && this.recvConfs.size() > 0) {
                for (final RecvConf rc : this.recvConfs) {
                    final String tps = rc.topic;
                    if (Convert.isNullOrEmpty(tps)) {
                        continue;
                    }
                    recv_tps.add(tps);
                }
            }
            if (recv_tps.size() > 0) {
                for (final String topic : recv_tps) {
                    mc.subscribe(topic);
                }
            }
            return this.bRTInitOk = true;
        }
        catch (final Exception ee) {
            ee.printStackTrace();
            failedr.append(ee.getMessage());
            return false;
        }
    }
    
    private void checkConn() {
        try {
            Thread.sleep(5000L);
        }
        catch (final Exception ex) {}
        try {
            final MqttClient mc = this.getMqttClient();
            if (mc.isConnected()) {
                return;
            }
            mc.connect(this.options);
        }
        catch (final Exception ee) {
            this.LOG_warn_debug("Mqtt_M checkConn err", (Throwable)ee);
        }
    }
    
    protected void RT_onRecvedMsg(final String topic, final byte[] bs) throws Exception {
        final String txt = new String(bs, "UTF-8");
        final List<MNNode> ns = this.getRelatedNodes();
        if (ns == null) {
            return;
        }
        for (final MNNode n : ns) {
            if (n instanceof MqttIn_NS) {
                final MqttIn_NS nin = (MqttIn_NS)n;
                final RecvConf rc = this.getRecvConfById(nin.recvId);
                if (rc == null) {
                    continue;
                }
                if (!MqttEndPoint.checkTopicMatch(rc.topic, topic)) {
                    continue;
                }
                final MNMsg msg = new MNMsg();
                if (rc.fmt == OutFmt.json) {
                    msg.asPayloadJO((Object)txt);
                }
                else {
                    msg.asPayload((Object)txt);
                }
                this.RT_sendMsgByRelatedNode(n, RTOut.createOutAll(msg));
            }
        }
    }
    
    public synchronized boolean RT_start(final StringBuilder failedr) {
        if (this.RT_bRun) {
            return true;
        }
        if (!this.RT_init(failedr)) {
            return false;
        }
        this.RT_bRun = true;
        (this.RT_th = new Thread(this.mqttMRunner)).start();
        return true;
    }
    
    public void RT_stop() {
        final Thread th = this.RT_th;
        if (th == null) {
            return;
        }
        if (th != null) {
            th.interrupt();
        }
        this.RT_bRun = false;
        this.RT_th = null;
        if (this.mqttEP != null) {
            this.mqttEP.disconnect();
            this.mqttEP = null;
        }
    }
    
    public boolean RT_isRunning() {
        return this.RT_th != null;
    }
    
    public boolean RT_isSuspendedInRun(final StringBuilder reson) {
        return false;
    }
    
    public boolean RT_runnerEnabled() {
        return true;
    }
    
    public boolean RT_runnerStartInner() {
        return false;
    }
    
    private void consumerRun() {
        try {
            while (this.RT_bRun) {
                this.checkConn();
            }
        }
        finally {
            this.RT_bRun = false;
            this.RT_th = null;
            if (this.mqttEP != null) {
                this.mqttEP.disconnect();
                this.mqttEP = null;
            }
        }
        this.RT_bRun = false;
        this.RT_th = null;
        if (this.mqttEP != null) {
            this.mqttEP.disconnect();
            this.mqttEP = null;
        }
    }
    
    protected void RT_renderDiv(final List<MNBase.DivBlk> divblks) {
        boolean b_conned = false;
        try {
            final MqttClient mc = this.getMqttClient();
            b_conned = (mc != null && mc.isConnected());
        }
        catch (final Exception ex) {}
        if (b_conned) {
            final StringBuilder divsb = new StringBuilder();
            divsb.append("<div tp='run' class=\"rt_blk\"><span style=\"color:green\">Connected :</span>").append(this.brokerHost).append(":").append(this.brokerPort).append("Psersist Num=").append(this.getPersistence().getSavedNum()).append("</div>");
            divblks.add(new MNBase.DivBlk("mqtt_m", divsb.toString()));
        }
        else {
            final StringBuilder divsb = new StringBuilder();
            divsb.append("<div tp='run' class=\"rt_blk\"><span style=\"color:red\">Not Connect to :").append("</span>").append(this.brokerHost).append(":").append(this.brokerPort).append("<br>Psersist Num=").append(this.getPersistence().getSavedNum()).append("</div>");
            divblks.add(new MNBase.DivBlk("mqtt_m", divsb.toString()));
        }
        super.RT_renderDiv((List)divblks);
    }
    
    public enum OutFmt
    {
        txt("txt", 0), 
        json("json", 1);
        
        private OutFmt(final String name, final int ordinal) {
        }
    }
    
    public static class SendConf
    {
        String id;
        String topic;
        String title;
        String desc;
        
        public String getShowTitle() {
            if (Convert.isNotNullEmpty(this.title)) {
                return this.title;
            }
            return this.title;
        }
        
        public JSONObject toJO() {
            final JSONObject jo = new JSONObject();
            jo.put("id", (Object)this.id);
            jo.put("topic", (Object)this.topic);
            jo.putOpt("t", (Object)this.title);
            jo.putOpt("d", (Object)this.desc);
            return jo;
        }
        
        public boolean fromJO(final JSONObject jo) {
            this.id = jo.getString("id");
            this.topic = jo.getString("topic");
            this.title = jo.optString("t", "");
            this.desc = jo.optString("d", "");
            return true;
        }
    }
    
    public static class RecvConf
    {
        String id;
        String topic;
        String title;
        OutFmt fmt;
        String desc;
        
        public RecvConf() {
            this.fmt = OutFmt.txt;
        }
        
        public String getShowTitle() {
            if (Convert.isNotNullEmpty(this.title)) {
                return this.title;
            }
            return this.title;
        }
        
        public JSONObject toJO() {
            final JSONObject jo = new JSONObject();
            jo.put("id", (Object)this.id);
            jo.put("topic", (Object)this.topic);
            jo.putOpt("t", (Object)this.title);
            jo.putOpt("d", (Object)this.desc);
            if (this.fmt != null) {
                jo.putOpt("fmt", (Object)this.fmt.name());
            }
            return jo;
        }
        
        public boolean fromJO(final JSONObject jo) {
            this.id = jo.getString("id");
            this.topic = jo.getString("topic");
            this.title = jo.optString("t", "");
            this.desc = jo.optString("d", "");
            this.fmt = OutFmt.valueOf(jo.optString("fmt", "txt"));
            if (this.fmt == null) {
                this.fmt = OutFmt.txt;
            }
            return true;
        }
    }
}
