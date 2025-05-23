package cn.doraro.flexedge.core.node;

import cn.doraro.flexedge.core.UAHmi;
import cn.doraro.flexedge.core.UAManager;
import cn.doraro.flexedge.core.UAPrj;
import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.node.NodeMsg.MsgTp;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import cn.doraro.flexedge.core.util.xmldata.DataTranserXml;
import cn.doraro.flexedge.core.util.xmldata.XmlData;
import cn.doraro.flexedge.core.util.xmldata.XmlDataWithFile;
import org.json.JSONObject;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * project share as a node,it will use
 *
 * @author jason.zhu
 */
public abstract class PrjSharer extends PrjNode {
    static ILogger log = LoggerManager.getLogger(PrjSharer.class);

    private transient long lastR = -1;

    private long pushInterval = 10000;

    /**
     * allow write or not
     */
    private boolean bWrite = false;
//	public abstract void start() ;
//	
//	public abstract void stop() ;
//	
//	public abstract boolean isRunning() ;

    public long getPushInterval() {
        return pushInterval;
    }

    public boolean isWritable() {
        return bWrite;
    }

    public PrjSharer withWritable(boolean bw) {
        this.bWrite = bw;
        return this;
    }

    public PrjSharer withPushInterval(long intv) {
        this.pushInterval = intv;
        return this;
    }

    public void runInLoop() {
        if (System.currentTimeMillis() - lastR < pushInterval)
            return;

        try {
            pushRtData();
        } catch (Exception e) {
            log.info(e.getMessage());

            if (log.isDebugEnabled())
                log.error("", e);
        } finally {
            lastR = System.currentTimeMillis();
        }

    }

    private void pushRtData() throws Exception {
        UAPrj p = UAManager.getInstance().getPrjById(this.getPrjId());
        if (p == null)
            throw new Exception("no prj found");
        StringWriter sw = new StringWriter();

        HashMap<String, Object> extpms = new HashMap<>();
        extpms.put("share_writable", bWrite);
        extpms.put("share_dt", System.currentTimeMillis());
        p.CXT_renderJson(sw, null, extpms);

        byte[] bs = sw.toString().getBytes("UTF-8");

        this.sendMsg(null, NodeMsg.MsgTp.push, bs);
        //this.sendMsg(nm);
    }

    protected void SW_sharerOnReq(String callerprjid, byte[] cont) throws Exception {
        //System.out.println("SW_sharerOnReq ") ;

        UAPrj p = UAManager.getInstance().getPrjById(this.getPrjId());
        if (p == null)
            return;

        XmlData xd = DataTranserXml.extractXmlDataFromObj(p);

        // add hmi file
        ArrayList<File> fs = new ArrayList<>();
        List<UAHmi> hmis = p.listSubHmiNodesAll();
        for (UAHmi hmi : hmis) {
            File f = hmi.getRelatedFile();
            if (log.isDebugEnabled())
                log.debug(" SW_sharerOnReq hmi f=" + f.getCanonicalPath());
            if (!f.exists())
                continue;
            fs.add(f);
        }

        XmlDataWithFile xdf = XmlDataWithFile.createFrom(xd, fs);
        byte[] resp_cont = xdf.writeToPkBuf();//.writeToStream(outputs);
        this.sendMsg(callerprjid, MsgTp.resp, resp_cont);
    }

    protected void SW_shareOnWrite(String callerprjid, byte[] cont) throws Exception {
        //System.out.println("SW_sharerOnReq ") ;
        if (!this.bWrite)
            return;

        UAPrj p = UAManager.getInstance().getPrjById(this.getPrjId());
        if (p == null)
            return;
        String jstr = new String(cont, "UTF-8");
        JSONObject jo = new JSONObject(jstr);
        String path = jo.optString("path");
        String strv = jo.optString("strv");
        if (Convert.isNullOrEmpty(path))
            return;

        UATag t = (UATag) p.getDescendantNodeByPath(path);
        if (t == null)
            return;
        t.RT_writeValStr(strv);
        //this.sendMsg(callerprjid, MsgTp.resp, xd.toBytesWithUTF8());
    }


    public abstract void runStop();


    public abstract boolean isRunning();


    public XmlData toXmlData() {
        XmlData xd = super.toXmlData();
        xd.setParamValue("push_int", this.pushInterval);
        xd.setParamValue("w", bWrite);
        return xd;
    }

    public void fromXmlData(XmlData xd) {
        super.fromXmlData(xd);
        this.pushInterval = xd.getParamValueInt64("push_int", 10000);
        this.bWrite = xd.getParamValueBool("w", false);
    }

    public void fromJSON(JSONObject jo) throws Exception {
        super.fromJSON(jo);
        this.pushInterval = jo.optLong("push_int", 10000);
    }

    public XmlData transParamsJSON2Xml(JSONObject jo) throws Exception {
        return null;
    }
}
