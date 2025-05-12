package cn.doraro.flexedge.core.conn;

import cn.doraro.flexedge.core.*;
import cn.doraro.flexedge.core.conn.mqtt.MqttEndPoint;
import cn.doraro.flexedge.core.node.PrjCaller;
import cn.doraro.flexedge.core.node.PrjCallerMQTT;
import cn.doraro.flexedge.core.node.PrjNodeAdapter;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import cn.doraro.flexedge.core.util.xmldata.XmlData;
import cn.doraro.flexedge.core.util.xmldata.XmlDataFilesMem;
import cn.doraro.flexedge.core.util.xmldata.XmlDataWithFile;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class ConnPtIOTTreeNode extends ConnPtMSGNor {
    private static final int MAX_NUM = 5;
//	private String mqttHost = null;
//	private int mqttPort = 1883;
//	private String mqttUser = null;
//
//	private String mqttPsw = null;
//
//	private int mqttConnTimeoutSec = 30;
//	private int mqttConnKeepAliveInterval = 60;
//
//	private ArrayList<String> topics = new ArrayList<>();

    //private transient MqttEndPoint mqttEP = null ;
    static ILogger log = LoggerManager.getLogger("ConnPtIOTTreeNode");
    MqttCallback mqttCB = new MqttCallback() {

        @Override
        public void connectionLost(Throwable cause) {
            //MqttConnectionUtils.r();\
            System.out.println(" * conn lost");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

            onRecvedMsg(topic, message.getPayload());
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            MqttMessage mm;
            try {
                mm = token.getMessage();
                System.out.println("mqtt msg deliveryComplete=" + mm.getPayload().length);
            } catch (MqttException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // .getPayload().length

        }
    };
    private transient PrjCaller prjCaller = null;
    private transient long shareDT = -1;
    private transient boolean shareWritable = false;
    private transient long lastPushDT = -1;
    private transient SynTreeInf synTreeInf = new SynTreeInf();
    private transient HashMap<String, Tag2Up> tagp2upMap = new HashMap<>();
    PrjNodeAdapter nodeAdp = new PrjNodeAdapter() {
        public void SW_callerOnResp(String shareprjid, byte[] cont) throws Exception {
            //if(!shareprjid.equals(this.sharePrjId))
            //	return ;

            //XmlData xd = XmlData.parseFromByteArray(cont, "UTF-8");
            StringBuilder failedr = new StringBuilder();
            XmlDataFilesMem xdf = XmlDataWithFile.readFromBuf(cont, failedr);
            if (xdf == null) {
                if (log.isErrorEnabled())
                    log.error("SW_callerOnResp read XmlDataFilesMem failed=" + failedr.toString());
                return;
            }
            onNodeShareRespXmlData(xdf);

        }

        public void SW_callerOnPush(byte[] cont) throws Exception {
            String jsonstr = new String(cont, "UTF-8");
            //System.out.println(" caller on push="+jsonstr) ;
            onNodeSharePush(jsonstr);
        }
    };

    public ConnPtIOTTreeNode() {
//		this.topics.add("flexedge/node");
//		this.topics.add("flexedge/syn");
    }

    @Override
    public String getConnType() {
        return ConnProIOTTreeNode.TP;
    }

    public PrjCaller getCaller() {
        if (prjCaller != null)
            return prjCaller;

        synchronized (this) {
            if (prjCaller != null)
                return prjCaller;

            prjCaller = new PrjCallerMQTT();
            prjCaller.withAdapter(nodeAdp);
            //prjCaller.init();
            //prjCaller.fromXmlData(xd);
            return prjCaller;
        }
    }

    public boolean isShareWritable() {
        return shareWritable;
    }

    public long getShareDT() {
        return this.shareDT;
    }

    public long getLastPushDT() {
        return this.lastPushDT;
    }

    private void onNodeShareRespXmlData(XmlDataFilesMem xdf) throws Exception {
        SynTreeInf sti = synTreeInf;

        sti.respXDF = xdf;
        sti.respDT = System.currentTimeMillis();

        UACh ch = this.getJoinedCh();
        if (ch == null) {
            sti.bSynOk = false;
            sti.synErr = "no joined channel";
            return;
        }

        try {
            //System.out.println("xmldata==="+xd.toXmlString());
            ch.Node_refreshByPrjXmlData(xdf);

            sti.bSynOk = true;
            sti.synErr = "";
        } catch (Exception e) {
            e.printStackTrace();
            sti.bSynOk = false;
            sti.synErr = e.getMessage();
        }
        //System.out.println("ch refresh by prj ok ") ;
    }

    private void onNodeSharePush(String jsonstr) throws Exception {
        try {
            if (log.isDebugEnabled())
                log.debug("onNodeSharePush=" + jsonstr);
            UACh ch = this.getJoinedCh();
            if (ch == null)
                return;

            if (Convert.isNullOrEmpty(jsonstr))
                return;
            JSONObject jo = new JSONObject(jsonstr);
            shareWritable = jo.optBoolean("share_writable", false);
            shareDT = jo.optLong("share_dt", -1);
            //if(log.isDebugEnabled())
            //	log.debug("onNodeSharePush before updateChCxtDyn");
            updateChCxtDyn(ch, jo);
        } finally {
            lastPushDT = System.currentTimeMillis();
        }
    }

    private void setToBuf(UATag tag, UAVal val) {
        String tagp = tag.getNodeCxtPathInPrj();
        Tag2Up t2u = tagp2upMap.get(tagp);
        if (t2u != null) {
            t2u.putVal(val);
            return;
        }

        t2u = new Tag2Up(tag, val);
        tagp2upMap.put(tagp, t2u);
        return;
    }

    private void setTagErrInBuf(long dt) {
        for (Tag2Up t2u : tagp2upMap.values()) {
            UAVal lastv = t2u.getLastVal();
            if (!lastv.isValid())
                continue; //
            UATag tag = t2u.tag;


            UAVal uav = new UAVal(false, null, dt, dt);
            tag.RT_setUAVal(uav);

            setToBuf(tag, uav);
        }
    }

    private void updateChCxtDyn(UANodeOCTagsGCxt p, JSONObject curcxt) {
        JSONArray jos = curcxt.optJSONArray("tags");
        if (jos != null) {
            for (int i = 0, n = jos.length(); i < n; i++) {
                JSONObject tg = jos.getJSONObject(i);
                String name = tg.getString("n");
                UATag tag = p.getTagByName(name);
                if (tag == null)
                    continue;
                //var tagp =p+n ;
                boolean bvalid = tg.optBoolean("valid", false);
                long dt = tg.optLong("dt", -1);
                long chgdt = tg.optLong("chgdt", -1);

                Object ov = tg.opt("v");
                String strv = "";
                if (ov != null && ov != JSONObject.NULL)
                    strv = "" + ov;
                //set to cxt
                ov = UAVal.transStr2ObjVal(tag.getValTp(), strv);
                UAVal uav = new UAVal(bvalid, ov, dt, chgdt);
                //tag.RT_setValStr(strv, true);
                tag.RT_setUAVal(uav);

                setToBuf(tag, uav);
            }
        }

        JSONArray subs = curcxt.optJSONArray("subs");
        if (subs != null) {
            for (int i = 0, n = subs.length(); i < n; i++) {
                JSONObject sub = subs.getJSONObject(i);

                String subn = sub.getString("n");

                UANode uan = p.getSubNodeByName(subn);
                if (uan == null)
                    continue;
                if (!(uan instanceof UANodeOCTagsGCxt))
                    continue;

                updateChCxtDyn((UANodeOCTagsGCxt) uan, sub);
            }
        }
    }

    public void runOnWrite(UATag tag, Object val) throws Exception {
        if (!this.shareWritable)
            throw new Exception("remote node cannot be write");

        UACh ch = this.getJoinedCh();
        if (ch == null) {
            return;
        }

        String path = tag.getNodeCxtPathIn(ch);
        String strv = "";
        if (val != null)
            strv = val.toString();
        this.getCaller().callShareWriter(path, strv);
    }


//	/**
//	 * based on channel joined to this connpt.
//	 * write 
//	 * @param nodepath
//	 * @param strv
//	 */
//	public boolean writeShareTag(String nodepath,String strv)
//	{
//		if(!shareWritable)
//			return false;//cannot write
//		
//		
//		this.getCaller().sendMsg(tarprjid, mt, msg);
//		UATag uat = (UATag)UAUtil.findNodeByPath(nodepath);
//		this.
//	}
//	public MqttEndPoint getMqttEP()
//	{
//		if(mqttEP!=null)
//			return mqttEP ;
//		mqttEP = new MqttEndPoint("flexedge_cpt_" + this.getId())
//				.withCallback(this.mqttCB);;
//		return mqttEP ;
//	}

    @Override
    public XmlData toXmlData() {
        XmlData xd = super.toXmlData();

        if (prjCaller != null) {
            XmlData tmpxd = prjCaller.toXmlData();
            xd.setSubDataSingle("caller", tmpxd);
        }
        // xd.setParamValue("opc_app_name", this.appName);
//		if(mqttEP!=null)
//			mqttEP.transParamsToXml(xd);

//		xd.setParamValue("mqtt_host", this.mqttHost);
//		xd.setParamValue("mqtt_port", this.mqttPort);
//		xd.setParamValue("mqtt_user", this.mqttUser);
//		xd.setParamValue("mqtt_psw", this.mqttPsw);
//		xd.setParamValue("mqtt_conn_to", this.mqttConnTimeoutSec);
//		xd.setParamValue("mqtt_conn_int", this.mqttConnKeepAliveInterval);

        return xd;
    }

//	@Override
//	public  void writeBindBeSelectedTreeJson(Writer w,boolean list_tags_only,boolean force_refresh) throws Exception
//	{
//		
//	}
//	
//	@Override
//	public int writeBindBeSelectedListRows(Writer w, int idx, int size)
//	{
//		
//		return 0;
//	}

    @Override
    public boolean fromXmlData(XmlData xd, StringBuilder failedr) {
        boolean r = super.fromXmlData(xd, failedr);

        PrjCaller pc = getCaller();

//		MqttEndPoint ep = getMqttEP() ;
//		ep.withParamsXml(xd) ;

        XmlData tmpxd = xd.getSubDataSingle("caller");
        if (tmpxd != null)
            pc.fromXmlData(tmpxd);

        // this.appName = xd.getParamValueStr("opc_app_name",
        // "flexedge_opc_client_"+this.getName());
//		this.mqttHost = xd.getParamValueStr("mqtt_host", "");
//		this.mqttPort = xd.getParamValueInt32("mqtt_port", -1);
//		this.mqttUser = xd.getParamValueStr("mqtt_user", "");
//		this.mqttPsw = xd.getParamValueStr("mqtt_psw", "");
//		this.mqttConnTimeoutSec = xd.getParamValueInt32("mqtt_conn_to", -1);
//		this.mqttConnKeepAliveInterval = xd.getParamValueInt32("mqtt_conn_int", -1);
        return r;
    }

    protected void injectByJson(JSONObject jo) throws Exception {
        super.injectByJson(jo);

        PrjCaller pc = getCaller();
        pc.fromJSON(jo);

//		this.mqttHost = optJSONString(jo, "mqtt_host", "");
//		this.mqttPort = optJSONInt(jo, "mqtt_port", -1);
//		this.mqttUser = optJSONString(jo, "mqtt_user", "");
//		this.mqttPsw = optJSONString(jo, "mqtt_psw", "");
//		this.mqttConnTimeoutSec = optJSONInt(jo, "mqtt_conn_to", -1);
//		this.mqttConnKeepAliveInterval = optJSONInt(jo, "mqtt_conn_int", -1);

    }

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
        if (prjCaller == null)
            return false;
        PrjCallerMQTT pc = (PrjCallerMQTT) this.getCaller();
        MqttEndPoint mep = pc.getMqttEP();
        if (mep == null)
            return false;
        long intv = mep.getMQTTKeepAliveInterval();
        if (intv <= 0)
            intv = 30000;
        if (System.currentTimeMillis() - this.lastPushDT > intv)
            return false;
        return prjCaller.isConnReady();
    }

    public String getConnErrInfo() {
        if (prjCaller == null)
            return null;
        if (prjCaller.isConnReady())
            return null;
        return prjCaller.getConnErrInfo();
    }

    public boolean sendMsg(String topic, byte[] bs) throws Exception {
        //this.publish(topic, bs, 0);
        if (prjCaller == null)
            return false;

        //prjCaller.sendMsg(tarprjid, mt, msg);
        return false;//not support in it
    }

    protected boolean readMsgToFile(File f) throws Exception {
        return false;
    }

    @Override
    protected void onRecvedMsg(String topic, byte[] bs) throws Exception {

        System.out.println("ConnPtIOTTreeNode onRecvedMsg=" + topic + " " + new String(bs, "utf-8"));


    }

    public void RT_writeValByBind(String tagpath, String strv) {
        //TODO
    }

    synchronized void disconnect() // throws IOException
    {
        prjCaller.disconnect();
    }

    public void RT_checkConn() {
        PrjCaller pc = getCaller();
        pc.checkConn();

        if (!isConnReady()) {
            long dt = System.currentTimeMillis();
            setTagErrInBuf(dt);
        }
    }

    @Override
    public LinkedHashMap<String, ConnDev> getFoundConnDevs() {
        return null;
    }


//	@Override
//	public List<String> getMsgTopics()
//	{
//		return null;
//	}

//	public void publish(String topic, byte[] data) throws MqttPersistenceException, MqttException
//	{
//		publish(topic, data, 0);
//	}
//
//	public void publish(String topic, byte[] data, int qos) throws MqttPersistenceException, MqttException
//	{
//		getMqttEP().publish(topic, data, qos);
//	}
//
//	public void publish(String topic, String txt) throws Exception
//	{
//		publish(topic, txt.getBytes("utf-8"), 1);
//	}

    public boolean RT_synTree(StringBuilder failedr) {
        try {
            this.getCaller().callShareTree();

            //synTreeInf = new SynTreeInf() ;
            synTreeInf.startDT = System.currentTimeMillis();
            synTreeInf.bSynOk = null;
            synTreeInf.synErr = null;
            synTreeInf.synErrTimeout = false;
            return true;
        } catch (Exception e) {
            failedr.append(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean RT_isInSynTree(long timeout) {
        if (synTreeInf == null)
            return false;

        if (System.currentTimeMillis() - synTreeInf.startDT > timeout) {
            if (synTreeInf.startDT > 0) {
                synTreeInf.bSynOk = false;
                //synTreeInf.synErr = "timeout" ;
                synTreeInf.synErrTimeout = true;
            }
            return false;
        }

        return synTreeInf.bSynOk == null;
    }

    public JSONObject RT_getSynTreeInf(long timeout) {
        JSONObject jo = new JSONObject();
        boolean binsyn = this.RT_isInSynTree(timeout);
        jo.put("in_syn", binsyn);
        jo.put("start_dt", synTreeInf.startDT);

        if (binsyn) {
            //jo.put("start_dt", synTreeInf.startDT) ;
        } else {

            jo.putOpt("syn_ok", synTreeInf.bSynOk);
            jo.putOpt("syn_err", synTreeInf.synErr);
            jo.put("resp_dt", synTreeInf.respDT);
            jo.put("resp_to", synTreeInf.synErrTimeout);

        }
        return jo;
    }

    public static class SynTreeInf {
        public XmlDataFilesMem respXDF = null;

        public long respDT = -1;

        public Boolean bSynOk = null;

        public String synErr = null;

        public boolean synErrTimeout = false;

        public long startDT = -1;
    }

    /**
     * record tag update info for later timeout checking
     *
     * @author jason.zhu
     */
    private static class Tag2Up {
        UATag tag;


        LinkedList<UAVal> prevVals = new LinkedList<>();

        public Tag2Up(UATag tag, UAVal val) {
            this.tag = tag;
            this.prevVals.addLast(val);
        }

        public void putVal(UAVal v) {
            prevVals.addLast(v);

            if (prevVals.size() > MAX_NUM)
                prevVals.removeFirst();
        }

        public UAVal getLastVal() {
            return prevVals.getLast();
        }
    }
}