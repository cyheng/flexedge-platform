package cn.doraro.flexedge.core.conn;

import cn.doraro.flexedge.core.ConnDev;
import cn.doraro.flexedge.core.ConnProvider;
import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.conn.html.HtmlParser;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import cn.doraro.flexedge.core.util.xmldata.XmlData;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConnPtHTTP extends ConnPtMSGNor {
    static ILogger log = LoggerManager.getLogger(ConnPtHTTP.class);

    String url = null;

    String method = "GET";

    HashMap<String, String> params = null;

    String requestHeads = null;

    String postTxt = null;

    long intervalMS = 5000;

    /**
     * true will load url by htmlunit and run js page as xml page
     */
    boolean runJsPage = false;

    long runJsTO = 10000;
    private boolean bConnOk = false;
    transient private long lastChkDT = -1;

    @Override
    public String getConnType() {
        return "http";
    }

    public String getUrl() {
        if (url == null)
            return "";
        return url;
    }

    public String getMethod() {
        return this.method;
    }

    public String getRequestHeads() {
        if (this.requestHeads == null)
            return "";
        return this.requestHeads;
    }

    public Map<String, String> getRequestHeadMap() {
        return Convert.transPropStrToMap(this.requestHeads);
    }

    public String getPostTxt() {
        if (this.postTxt == null)
            return "";
        return this.postTxt;
    }

    public long getIntervalMS() {
        return this.intervalMS;
    }

    public boolean isRunJsPage() {
        return runJsPage;
    }

    public long getRunJsTO() {
        return this.runJsTO;
    }

    @Override
    public String getStaticTxt() {
        return null;
    }

    @Override
    public boolean isPassiveRecv() {
        return false;
    }

    @Override
    public XmlData toXmlData() {
        XmlData xd = super.toXmlData();
        xd.setParamValue("url", this.url);
        xd.setParamValue("method", this.method);
        xd.setParamValue("int_ms", intervalMS);
        xd.setParamValue("run_js_page", this.runJsPage);
        xd.setParamValue("run_js_to", this.runJsTO);
        if (requestHeads != null)
            xd.setParamValue("req_heads", this.requestHeads);
        if (postTxt != null)
            xd.setParamValue("post_txt", this.postTxt);

        if (params != null && params.size() > 0) {
            XmlData tmpxd = xd.getOrCreateSubDataSingle("params");
            for (Map.Entry<String, String> n2v : params.entrySet()) {
                tmpxd.setParamValue(n2v.getKey(), n2v.getValue());
            }
        }

        return xd;
    }

    @Override
    public boolean fromXmlData(XmlData xd, StringBuilder failedr) {
        boolean r = super.fromXmlData(xd, failedr);

        // this.appName = xd.getParamValueStr("opc_app_name",
        // "flexedge_opc_client_"+this.getName());
        this.url = xd.getParamValueStr("url", "");
        this.method = xd.getParamValueStr("method", "");
        this.intervalMS = xd.getParamValueInt64("int_ms", 5000);
        this.runJsPage = xd.getParamValueBool("run_js_page", false);
        this.runJsTO = xd.getParamValueInt64("run_js_to", 10000);
        this.requestHeads = xd.getParamValueStr("req_heads", null);
        this.postTxt = xd.getParamValueStr("post_txt", null);
        XmlData tmpxd = xd.getSubDataSingle("params");
        if (tmpxd != null) {
            params = tmpxd.toNameStrValMap();
        }
        return r;
    }

    private String optJSONString(JSONObject jo, String name, String defv) {
        String r = jo.optString(name);
        if (r == null)
            return defv;
        return r;
    }

    private long optJSONInt64(JSONObject jo, String name, long defv) {
        Object v = jo.opt(name);
        if (v == null)
            return defv;
        return jo.optLong(name);
    }

    protected void injectByJson(JSONObject jo) throws Exception {
        super.injectByJson(jo);

        // this.appName =optJSONString(jo,"opc_app_name",getOpcAppNameDef()) ;
        this.url = optJSONString(jo, "url", "");
        this.method = optJSONString(jo, "method", "GET");
        this.requestHeads = optJSONString(jo, "req_heads", null);
        this.postTxt = optJSONString(jo, "post_txt", null);
        this.intervalMS = optJSONInt64(jo, "int_ms", 5000);
        this.runJsPage = jo.optBoolean("run_js_page", false);
        this.runJsTO = jo.optLong("run_js_to", 30000);
    }

    @Override
    public boolean isConnReady() {
        ConnProvider cp = this.getConnProvider();
        if (cp == null)
            return false;
        if (!cp.isRunning())
            return false;
        return bConnOk;
    }

    public String getConnErrInfo() {
        return null;
    }

    private byte[] getData(String url, String token) throws Exception {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpGet git = new HttpGet(url);
        //String result = "";
        try (CloseableHttpClient chc = httpClientBuilder.build()) {
            // HttpEntity entity = new StringEntity(post_txt, "UTF-8");
            // git.setEntity(entity);
            git.setHeader("Content-type", "application/json");
            if (Convert.isNotNullEmpty(token))
                git.setHeader("token", token);
            if (Convert.isNotNullEmpty(this.requestHeads)) {
                Map<String, String> hs = this.getRequestHeadMap();
                for (Map.Entry<String, String> n2v : hs.entrySet()) {
                    git.setHeader(n2v.getKey(), n2v.getValue());
                }
            }
            HttpResponse resp = chc.execute(git);
            InputStream respIs = resp.getEntity().getContent();
            return IOUtils.toByteArray(respIs);

            // result = new String(rbs, this.getEncod());
            // return result;
        }
    }

    private byte[] postData(String url, Map<String, Object> params) throws Exception {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpPost git = new HttpPost(url);
        try (CloseableHttpClient chc = httpClientBuilder.build()) {
            // HttpEntity entity = new StringEntity(post_txt, "UTF-8");
            // git.setEntity(entity);
            git.setHeader("Content-type", "application/json");
//			if (Convert.isNotNullEmpty(token))
//				git.setHeader("token", token);
            if (Convert.isNotNullEmpty(this.requestHeads)) {
                Map<String, String> hs = this.getRequestHeadMap();
                for (Map.Entry<String, String> n2v : hs.entrySet()) {
                    git.setHeader(n2v.getKey(), n2v.getValue());
                }
            }

            if (params != null && params.size() > 0) {
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                for (Iterator<String> iter = params.keySet().iterator(); iter.hasNext(); ) {
                    String key = (String) iter.next();
                    String value = String.valueOf(params.get(key));
                    nvps.add(new BasicNameValuePair(key, value));
                }
                git.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));
            }
            HttpResponse resp = chc.execute(git);
            InputStream respIs = resp.getEntity().getContent();
            return IOUtils.toByteArray(respIs);

            // result = new String(rbs, this.getEncod());
            // return result;
        }
    }

    private byte[] postData(String url, String post_txt, String token) throws Exception {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpPost post = new HttpPost(url);
        String result = "";
        try (CloseableHttpClient chc = httpClientBuilder.build()) {
            if (Convert.isNotNullEmpty(post_txt)) {
                HttpEntity entity = new StringEntity(post_txt, "UTF-8");
                post.setEntity(entity);
            }

            post.setHeader("Content-type", "application/json");

            if (Convert.isNotNullEmpty(this.requestHeads)) {
                Map<String, String> hs = this.getRequestHeadMap();
                for (Map.Entry<String, String> n2v : hs.entrySet()) {
                    post.setHeader(n2v.getKey(), n2v.getValue());
                }
            }

            if (Convert.isNotNullEmpty(token))
                post.setHeader("token", token);
            HttpResponse resp = chc.execute(post);
            InputStream respIs = resp.getEntity().getContent();
            return IOUtils.toByteArray(respIs);
            // result = new String(rbs, this.getEncod());
            // return result;
        }
    }

    @Override
    public LinkedHashMap<String, ConnDev> getFoundConnDevs() {
        return null;
    }

    protected boolean readMsgToFile(File f) throws Exception {
        byte[] bs = null;
        if (!runJsPage) {
            bs = getData(this.getUrl(), "");
        } else {
            String str = HtmlParser.getRunJsPageXml(this.getUrl(), this.runJsTO);
            bs = str.getBytes("UTF-8");
        }

        if (bs == null)
            return false;

        File pf = f.getParentFile();
        if (!pf.exists())
            pf.mkdirs();

        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(bs);
        }
        return true;
    }

    public void RT_checkConn() {

    }

    /**
     * will be run interval in loop
     */
    void checkUrl() {
        if (System.currentTimeMillis() - lastChkDT < this.intervalMS)
            return;

        try {
            if (!runJsPage) {
                byte[] res = null;
                if ("POST".equals(this.method))
                    res = postData(this.getUrl(), postTxt, null);
                else
                    res = getData(this.getUrl(), "");
                this.onRecvedMsg("", res);
            } else {
                String str = HtmlParser.getRunJsPageXml(this.getUrl(), this.runJsTO);
                this.onRecvedUrlHtml(str);
            }
            bConnOk = true;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("", e);
            }
            // e.printStackTrace();
            bConnOk = false;
        } finally {
            lastChkDT = System.currentTimeMillis();
        }
    }
    // @Override
    // public List<String> getMsgTopics()
    // {
    // // TODO Auto-generated method stub
    // return null;
    // }

    @Override
    public boolean sendMsg(String topic, byte[] bs) throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void runOnWrite(UATag tag, Object val) throws Exception {
        // TODO Auto-generated method stub

    }

}
