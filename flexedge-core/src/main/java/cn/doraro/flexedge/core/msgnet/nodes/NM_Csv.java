package cn.doraro.flexedge.core.msgnet.nodes;

import cn.doraro.flexedge.core.msgnet.MNConn;
import cn.doraro.flexedge.core.msgnet.MNMsg;
import cn.doraro.flexedge.core.msgnet.MNNodeMid;
import cn.doraro.flexedge.core.msgnet.RTOut;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.CsvUtil;
import cn.doraro.flexedge.core.util.Lan;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

public class NM_Csv extends MNNodeMid {
    static Lan lan = Lan.getLangInPk(NM_FileReader.class);
    int skipFirstLines = 0;
    String colNames = null;
    OutTP outTP = OutTP.msg_per_row_jo;
    boolean parseNum = false;
    boolean parseBool = false; //parse true false
    private List<String> colNameList = null;

    @Override
    public int getOutNum() {
        return 1;
    }

    @Override
    public String getTP() {
        return "csv";
    }

    @Override
    public String getTPTitle() {
        return "CSV";
    }

    @Override
    public String getColor() {
        return "#ddc069";
    }

    @Override
    public String getIcon() {
        return "\\uf0f6";
    }

    @Override
    public boolean isParamReady(StringBuilder failedr) {
        return true;
    }

    @Override
    public JSONObject getParamJO() {
        JSONObject jo = new JSONObject();
        jo.put("parse_num", parseNum);
        jo.put("out_tp", outTP.name());
        jo.put("skip_first_lns", this.skipFirstLines);
        jo.putOpt("col_names", colNames);
        return jo;
    }

    @Override
    protected void setParamJO(JSONObject jo) {
        this.parseNum = jo.optBoolean("parse_num", false);
        this.outTP = OutTP.valueOf(jo.optString("out_tp", "msg_per_row_jo"));
        if (this.outTP == null)
            this.outTP = OutTP.msg_per_row_jo;
        this.skipFirstLines = jo.optInt("skip_first_lns", 0);
        this.colNames = jo.optString("col_names");
        colNameList = Convert.splitStrWith(this.colNames, ",|");
    }

    @Override
    protected RTOut RT_onMsgIn(MNConn in_conn, MNMsg msg) throws Exception {
        String txt = msg.getPayloadStr();
        if (Convert.isNullOrEmpty(txt)) {
            return null;
        }
        StringReader sr = new StringReader(txt);
        BufferedReader br = new BufferedReader(sr);
        String ln = null;
        int cc = 0;
        if (this.outTP == OutTP.msg_per_row_jo || this.outTP == OutTP.msg_per_row_jarr) {
            while ((ln = br.readLine()) != null) {
                cc++;
                if (this.skipFirstLines > 0 && cc <= this.skipFirstLines)
                    continue;

                List<Object> ss = CsvUtil.parseCSVLine(ln, parseNum);
                MNMsg outm = new MNMsg();
                if (this.outTP == OutTP.msg_per_row_jo) {
                    JSONObject tmpjo = new JSONObject();
                    int n = ss.size();
                    for (int i = 0; i < n; i++) {
                        String coln = "" + i;
                        if (colNameList != null && colNameList.size() > i)
                            coln = colNameList.get(i);
                        tmpjo.put(coln, ss.get(i));
                    }
                    outm.asPayload(tmpjo);
                } else {
                    JSONArray jarr = new JSONArray(ss);
                    outm.asPayload(jarr);
                }
                this.RT_sendMsgOut(RTOut.createOutAll(outm));
            }
            return null;
        }

        JSONArray pld = new JSONArray();
        while ((ln = br.readLine()) != null) {
            cc++;
            if (this.skipFirstLines > 0 && cc <= this.skipFirstLines)
                continue;

            List<Object> ss = CsvUtil.parseCSVLine(ln, parseNum);
            JSONArray jarr = new JSONArray(ss);
            pld.put(jarr);
        }
        MNMsg outm = new MNMsg();
        outm.asPayload(pld);
        this.RT_sendMsgOut(RTOut.createOutAll(outm));
        return null;
    }

    public static enum OutTP {
        msg_per_row_jo,
        msg_per_row_jarr,
        single_msg;

        public String getTitle() {
            return lan.g("csv_" + this.name());
        }
    }


}
