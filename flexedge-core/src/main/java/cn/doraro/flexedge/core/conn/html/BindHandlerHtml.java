package cn.doraro.flexedge.core.conn.html;

import cn.doraro.flexedge.core.IJoinedNode;
import cn.doraro.flexedge.core.UACh;
import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.conn.ConnPtMSGNor;
import cn.doraro.flexedge.core.conn.ConnPtMSGNor.BindHandler;
import cn.doraro.flexedge.core.conn.html.HtmlBlockLocator.ExtractPoint;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.*;

public class BindHandlerHtml extends BindHandler {
    static ILogger log = LoggerManager.getLogger(BindHandlerHtml.class);

    ArrayList<HtmlBlockLocator> hbLocs = new ArrayList<>();

    private transient HashMap<HtmlBlockLocator.ExtractPoint, List<String>> ep2tags = null;

    public BindHandlerHtml(ConnPtMSGNor cpm) {
        super(cpm);
    }

    public ExtractPoint getExtractPt(String blkname, String epname) {
        if (hbLocs == null)
            return null;

        for (HtmlBlockLocator hbl : hbLocs) {
            if (hbl.getName().equals(blkname)) {
                return hbl.getExtractPt(epname);
            }
        }
        return null;
    }

    @Override
    protected boolean initBind() {
        if (Convert.isNullOrEmpty(bindProbeStr)) {
            bindRunErr = "no bind setup";
            return false;
        }

        try {
            JSONArray bps = new JSONArray(bindProbeStr);
            int len = bps.length();

            for (int i = 0; i < len; i++) {
                JSONObject ob = bps.getJSONObject(i);
                HtmlBlockLocator hbl = HtmlBlockLocator.fromJsonObj(ob);
                if (hbl == null)
                    continue;
                hbLocs.add(hbl);
            }


            bps = new JSONArray(this.bindMapStr);
            len = bps.length();
            ep2tags = new HashMap<>();

            for (int i = 0; i < len; i++) {
                JSONObject ob = bps.getJSONObject(i);
                String bindp = ob.optString("bindp");
                String tagp = ob.optString("tagp");
                if (Convert.isNotNullEmpty(bindp) && Convert.isNotNullEmpty(tagp)) {
                    int k = bindp.indexOf(":");
                    if (k <= 0)
                        continue;
                    String pp = bindp.substring(0, k);
                    String vt = bindp.substring(k + 1);

                    List<String> pss = Convert.splitStrWith(pp, "/");
                    if (pss.size() != 2)
                        continue;

                    String blkname = pss.get(0);
                    String epname = pss.get(1);
                    ExtractPoint ep = getExtractPt(blkname, epname);
                    if (ep == null)
                        continue;
                    List<String> tagps = ep2tags.get(ep);
                    if (tagps == null) {
                        tagps = new ArrayList<>();
                        ep2tags.put(ep, tagps);
                    }
                    tagps.add(tagp);
                }
            }

            if (ep2tags.size() <= 0) {
                bindRunErr = "no valid bind setup";
                return false;
            }
            return true;
        } catch (Exception e) {
            bindRunErr = "bind init err:" + e.getMessage();
            return false;
        }

    }


    public HtmlBlockLocator getBlockLocator(String id) {
        for (HtmlBlockLocator bl : hbLocs) {
            if (bl.getId().equals(id))
                return bl;
        }
        return null;
    }

    @Override
    protected boolean runBind(String topic, String txt) throws Exception {
        if (Convert.isNullOrEmpty(txt))
            return false;

        Document doc = Jsoup.parse(txt);
        if (doc == null)
            return false;

        if (hbLocs == null || hbLocs.size() <= 0)
            return false;

        IJoinedNode jn = this.connPtMsg.getJoinedNode();
        if (jn == null || !(jn instanceof UACh))
            return false;
        UACh joinedch = (UACh) jn;
        HtmlParser hp = new HtmlParser();
        for (HtmlBlockLocator hbl : this.hbLocs) {
            try {
                hp.setDoc(doc);
                HtmlBlockLocator tmphbl = hbl.locateToBlock(hp);
                if (tmphbl == null)
                    continue;
                Element blkroot = tmphbl.getBlockRoot();// hp.findBlockRootByTracePts(hbl.getTracePts(),hbl.getTraceUpLvl()) ;
                if (blkroot == null)
                    continue;

                StringBuilder ressb = new StringBuilder();
                LinkedHashMap<String, ExtractPoint> epts = hbl.getExtractPts();
                if (epts != null) {

                    for (HtmlBlockLocator.ExtractPoint ei : epts.values()) {
                        List<String> tagps = ep2tags.get(ei);
                        if (tagps == null || tagps.size() <= 0)
                            continue;

                        XPath xp = ei.getXPath();
                        if (xp == null)
                            continue;

                        String strv = HtmlParser.findStrValByXPath(blkroot, xp);
                        if (Convert.isNullOrEmpty(strv))
                            continue;

                        if (joinedch != null) {
                            for (String tagp : tagps) {
                                UATag t = joinedch.getTagByPath(tagp);
                                if (t == null) {
                                    continue;
                                }

                                ressb.append(ei.getTitle() + "/" + hbl.getName() + "/" + ei.getName() + "]" + ei.getPath() + " → " + tagp + "=" + strv + "\r\n");
                                t.RT_setValRawStr(strv, true, System.currentTimeMillis());

                            }
                        }
                    }

                }
                bindRunRes = ressb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                if (log.isDebugEnabled())
                    log.error("", e);
            }
        }
        return true;
    }

    @Override
    public List<String> listBindTagPathInCh() {
        if (ep2tags == null || ep2tags.size() <= 0)
            return null;

        ArrayList<String> rets = new ArrayList<>();
        for (Map.Entry<HtmlBlockLocator.ExtractPoint, List<String>> k2v : ep2tags.entrySet()) {
            List<String> tagps = k2v.getValue();
            for (String ps : tagps) {
                if (!rets.contains(ps))
                    tagps.add(ps);
            }
        }

        Collections.sort(rets);
        return rets;
    }

}


