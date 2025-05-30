package cn.doraro.flexedge.core.msgnet.modules;

import cn.doraro.flexedge.core.msgnet.MNConn;
import cn.doraro.flexedge.core.msgnet.MNMsg;
import cn.doraro.flexedge.core.msgnet.MNNodeMid;
import cn.doraro.flexedge.core.msgnet.RTOut;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.IdCreator;
import cn.doraro.flexedge.core.util.Lan;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * open file
 *
 * @author jason.zhu
 */
public class FileDir_Open extends MNNodeMid {
    static Lan lan = Lan.getLangInPk(FileDir_Open.class);
    TP openTP = TP.auto_yyyy_mm;
    String fileExt = null;
    FileDir_M.FileItem curFI = null;

    @Override
    public int getOutNum() {
        return 1;
    }

    @Override
    public String getTP() {
        return "file_dir_open";
    }

    @Override
    public String getTPTitle() {
        return g("file_dir_open");
    }

    @Override
    public String getColor() {
        return "#e7b686";
    }

    @Override
    public String getIcon() {
        //return "\\uf15b";
        return "\\uf477";
    }

    @Override
    public boolean isParamReady(StringBuilder failedr) {
        return true;
    }

    @Override
    public JSONObject getParamJO() {
        JSONObject jo = new JSONObject();
        jo.put("open_tp", this.openTP.getVal());
        jo.putOpt("ext", this.fileExt);
        return jo;
    }

    @Override
    protected void setParamJO(JSONObject jo) {
        this.openTP = TP.fromVal(jo.optInt("open_tp", 0));
        this.fileExt = jo.optString("ext");
    }

    @Override
    protected RTOut RT_onMsgIn(MNConn in_conn, MNMsg msg) throws Exception {
        FileDir_M mnm = (FileDir_M) this.getOwnRelatedModule();
        String fn = IdCreator.newSeqId();
        long ms = IdCreator.extractTimeInMillInSeqId(fn);
        if (Convert.isNotNullEmpty(this.fileExt))
            fn += "." + this.fileExt;

        String sub_fp = null;
        switch (this.openTP) {
            case msg_in:
                throw new RuntimeException("no impl");
            case auto_yyyy_mm_dd:
                sub_fp = new SimpleDateFormat("yyyy/MM/dd").format(new Date(ms));
                break;
            case auto_yyyy_mm:
            default:
                sub_fp = new SimpleDateFormat("yyyy/MM").format(new Date(ms));
                break;
        }
        StringBuilder failedr = new StringBuilder();
        curFI = mnm.RT_openFile(sub_fp + "/" + fn, failedr);
        if (curFI == null) {
            RT_DEBUG_WARN.fire("file_dir_open", failedr.toString());
            return null;
        }
        RT_DEBUG_WARN.clear("file_dir_open");
        return RTOut.createOutAll(new MNMsg().asPayload(curFI.file.getAbsoluteFile()));
    }

    public static enum TP {
        auto_yyyy_mm(0),
        auto_yyyy_mm_dd(1),
        msg_in(2);

        public final int val;

        TP(int v) {
            val = v;
        }

        public static TP fromVal(int v) {
            switch (v) {
                case 1:
                    return auto_yyyy_mm_dd;
                case 2:
                    return msg_in;
                default:
                    return auto_yyyy_mm;
            }
        }

        public int getVal() {
            return this.val;
        }

        public String getTitle() {
            return lan.g("file_dir_o_" + this.name());
        }
    }

}
