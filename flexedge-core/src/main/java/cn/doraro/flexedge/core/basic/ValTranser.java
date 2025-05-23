package cn.doraro.flexedge.core.basic;

import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.UAVal.ValTP;
import cn.doraro.flexedge.core.util.Convert;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * single value transformer
 *
 * @author jason.zhu
 */
public abstract class ValTranser {


    private static ArrayList<ValTranser> TRANSERS = null;// new ArrayList<>() ;
    UAVal.ValTP transVT = UAVal.ValTP.vt_double;
    String units = null;
//	/**
//	 * value type after transfered
//	 */
//	XmlValType transValTp = null ;
//	
    private UATag belongTo = null;
    private Exception lastTransErr = null;

    public static ValTranser parseValTranser(UATag tag, String str) {
        if (Convert.isNullOrEmpty(str))
            return null;

        JSONObject jo = new JSONObject(str);
        String n = jo.optString("_n");
        if (Convert.isNullOrEmpty(n))
            return null;

        ValTranser vt = null;
        switch (n) {
            case ValTransScaling.NAME:
                vt = new ValTransScaling();
                break;
            case ValTransJS.NAME:
                vt = new ValTransJS();
                break;
            case ValTransCalc.NAME:
                vt = new ValTransCalc();
                break;
            default:
                break;

        }

        if (vt != null) {
            if (!vt.fromTransJO(jo))
                return null;
            vt.belongTo = tag;
        }
        return vt;
    }

    public static List<ValTranser> listValTransers() {
        if (TRANSERS != null)
            return TRANSERS;
        ArrayList<ValTranser> ss = new ArrayList<>();
        ss.add(new ValTransCalc());
        ss.add(new ValTransScaling());
        ss.add(new ValTransJS());
        TRANSERS = ss;
        return ss;
    }

    public UATag getBelongTo() {
        return belongTo;
    }

    public abstract String getName();

    public abstract String getTitle();//may multi language

    public abstract Object transVal(Object v) throws Exception;

    public abstract Object inverseTransVal(Object v) throws Exception;

    public ValTP getTransValTP() {
        return transVT;
    }


    public Exception getTransErr() {
        return lastTransErr;
    }

    protected final void setTransErr(Exception e) {
        this.lastTransErr = e;
    }

    public JSONObject toTransJO() {
        JSONObject jo = new JSONObject();
        jo.put("_n", this.getName());
        if (units != null)
            jo.put("_u", this.units);
        if (transVT != null) {
            jo.put("_vt", transVT.getInt());
            jo.put("_vtt", transVT.getStr());
        }
        return jo;
    }


    public boolean fromTransJO(JSONObject m) {
        String n = m.optString("_n");
        if (!this.getName().equals(n))
            return false;
        this.units = m.optString("_u");
        int vt = m.optInt("_vt", UAVal.ValTP.vt_double.getInt());
        transVT = UAVal.getValTp(vt);
        return true;
    }

    public String toString() {
        return toTransJO().toString();
    }

    public String toTitleString() {
        return this.getTitle() + " - " + this.getTransValTP().getStr();
    }
}