package cn.doraro.flexedge.core.msgnet.nodes;

import cn.doraro.flexedge.core.UAPrj;
import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.msgnet.MNConn;
import cn.doraro.flexedge.core.msgnet.MNMsg;
import cn.doraro.flexedge.core.msgnet.MNNodeMid;
import cn.doraro.flexedge.core.msgnet.RTOut;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.jt.JSONTemp;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 选择特定标签，然后配置特定名称var进行对应。生成简单的JSON数据
 *
 * @author jason.zhu
 */
public class NM_TagReader extends MNNodeMid {
    ArrayList<TagItem> tagItems = new ArrayList<>();
    private transient long RT_lastInvalidDT = -1;
    private transient JSONArray RT_lastInvalidTags = null;

    @Override
    public String getColor() {
        return "#a1cbde";
    }

    @Override
    public String getIcon() {
        return "\\uf02c";
    }

    @Override
    public JSONTemp getInJT() {
        return null;
    }

    @Override
    public JSONTemp getOutJT() {
        return null;
    }

    @Override
    public int getOutNum() {
        return 2;
    }

    @Override
    public String getOutColor(int idx) {
        if (idx == 0)
            return null;
        if (idx == 1)
            return "red";
        return null;
    }

    @Override
    public String getTP() {
        return "tag_reader";
    }

    @Override
    public String getTPTitle() {
        return g("tag_reader");
    }

    @Override
    public boolean isParamReady(StringBuilder failedr) {
        if (tagItems == null || tagItems.size() <= 0)
            return true;
        for (TagItem ti : this.tagItems) {
            if (!ti.isValid(failedr))
                return false;
        }
        return true;
    }

    @Override
    public JSONObject getParamJO() {
        JSONObject jo = new JSONObject();
        JSONArray jarr = new JSONArray();
        if (tagItems != null) {
            for (TagItem ccr : this.tagItems) {
                JSONObject tmpjo = ccr.toJO();
                jarr.put(tmpjo);
            }
        }
        jo.put("tags", jarr);
        return jo;
    }

    // --------------

    @Override
    protected void setParamJO(JSONObject jo) {
        JSONArray jarr = jo.optJSONArray("tags");
        ArrayList<TagItem> ccrs = new ArrayList<>();
        UAPrj prj = (UAPrj) this.getBelongTo().getContainer();
        if (jarr != null) {
            int n = jarr.length();
            for (int i = 0; i < n; i++) {
                JSONObject tmpjo = jarr.getJSONObject(i);
                TagItem ccr = TagItem.fromJO(tmpjo);
                if (ccr != null)
                    ccrs.add(ccr);

                String tagpath = ccr.tagPath;
                if (Convert.isNotNullEmpty(tagpath)) {

                    UATag tag = prj.getTagByPath(tagpath);
                    ccr.tag = tag;
                }
            }
        }
        this.tagItems = ccrs;
    }

    @Override
    protected RTOut RT_onMsgIn(MNConn in_conn, MNMsg msg) {
        if (this.tagItems == null || this.tagItems.size() <= 0)
            return null;

        boolean bok = true;
        JSONObject tmpjo = new JSONObject();
        JSONArray errjarr = new JSONArray();
        bok = true;
        for (TagItem ti : this.tagItems) {
            UATag tag = ti.tag;
            if (tag == null)
                continue;
            UAVal uav = tag.RT_getVal();
            if (uav == null || !uav.isValid()) {
                if (ti.bMustOk)
                    bok = false;
                errjarr.put(ti.tagPath);
            } else {
                tmpjo.putOpt(ti.getVarName(), uav.getObjVal());
            }
        }

        if (tmpjo.isEmpty() && errjarr.length() <= 0)
            return null;

        RTOut rto = RTOut.createOutIdx();
        if (bok && !tmpjo.isEmpty()) {
            rto.asIdxMsg(0, new MNMsg().asPayload(tmpjo));
        }
        if (errjarr.length() > 0) {
            rto.asIdxMsg(1, new MNMsg().asPayload(errjarr));
            RT_lastInvalidDT = System.currentTimeMillis();
            RT_lastInvalidTags = errjarr;

        }
        return rto;
    }

    @Override
    public String RT_getOutTitle(int idx) {
        if (idx == 0)
            return g("valid_data");
        if (idx == 1) {
            if (RT_lastInvalidTags == null || RT_lastInvalidDT <= 0)
                return g("invalid_tags");
            else {
                return Convert.calcDateGapToNow(RT_lastInvalidDT) + "<br><pre>" +
                        Convert.plainToHtml(RT_lastInvalidTags.toString(2)) + "</pre>";
            }
        }
        return null;
    }

    @Override
    public String RT_getOutColor(int idx) {
        if (idx == 0)
            return null;
        if (idx == 1) {
            if (RT_lastInvalidTags == null || RT_lastInvalidDT <= 0)
                return null;
            return "red";
        }

        return null;
    }

    public static class TagItem {
        String tagPath;

        String varName;

        boolean bMustOk = false;


        private UATag tag = null;

        public TagItem(String tagpath, String var_n, boolean b_must_ok) {
            this.tagPath = tagpath;
            this.varName = var_n;
            this.bMustOk = b_must_ok;
        }

        private TagItem() {
        }

        public static TagItem fromJO(JSONObject jo) {
            TagItem ret = new TagItem();
            ret.tagPath = jo.optString("tag");
            ret.varName = jo.optString("varn");
            ret.bMustOk = jo.optBoolean("must_ok", false);
            return ret;
        }

        public String getVarName() {
            if (Convert.isNullOrEmpty(this.varName))
                return tagPath;
            return varName;
        }

        public boolean isValid(StringBuilder failedr) {
            if (Convert.isNullOrEmpty(this.tagPath)) {
                failedr.append("tag path cannot be null or empty");
                return false;
            }

            if (tag == null) {
                failedr.append("not tag with path =" + this.tagPath);
                return false;
            }
            return true;
        }

        public JSONObject toJO() {
            JSONObject jo = new JSONObject();
            jo.putOpt("tag", this.tagPath);
            jo.putOpt("varn", varName);
            jo.putOpt("must_ok", this.bMustOk);
            return jo;
        }
    }

}
