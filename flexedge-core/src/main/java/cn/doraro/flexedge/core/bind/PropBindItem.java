package cn.doraro.flexedge.core.bind;

import cn.doraro.flexedge.core.UANodeOCTagsCxt;
import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.cxt.UACodeItem;
import cn.doraro.flexedge.core.cxt.UAContext;

/**
 * provide some hmi component‘s properties to bind cxt data item or js expression
 * <p>
 * server may use this to send dyn data to ui
 *
 * @author jason.zhu
 */
public class PropBindItem {
    String name = null;

    boolean bExp = false;

    String txt = null;

    transient UACodeItem code = null;
    private UAVal lastVal = null;

    public PropBindItem() {

    }

    public PropBindItem(String name, boolean bexp, String txt) {
        this.name = name;
        this.bExp = bexp;
        this.txt = txt;
    }

    public String getName() {
        return name;
    }

    public boolean isExp() {
        return bExp;
    }

    public String getTxt() {
        return txt;
    }
    //private Object lastExpVal = null ;

//	public UAVal RT_getVal(UANodeOCTagsCxt tagn)
//	{
//		return RT_getVal(tagn,false) ;
//	}

    /**
     * @param tagn
     * @param chk_last when true,it will compare last val,and return null while last is equals now git
     * @return
     */
    public UAVal RT_getVal(UANodeOCTagsCxt tagn, long lastdt) {
        if (this.txt == null || this.txt.equals(""))
            return null;
        if (!bExp) {
            UATag tg = (UATag) tagn.getDescendantNodeByPath(txt);//.getTagByPath(this.txt) ;
            if (tg == null)
                return null;

            UAVal r = tg.RT_getVal();
            if (r == null)
                return null;

            if (r.getValDT() < lastdt)
                return null;
            //if(chk_last && r.equals(lastVal))
            //	return null ;

            lastVal = r.copyMe();
            return r;
        }
        //js
        UAContext cxt = tagn.RT_getContext();
        if (cxt == null)
            return null;

        if (this.code == null) {
            this.code = new UACodeItem("", this.txt);
            this.code.initItem(cxt);
        }

        if (!code.isValid())
            return null;

        try {
            Object v = code.runCode();
            if (v == null)
                return null;

            Object lastv = null;
            if (lastVal != null)
                lastv = lastVal.getObjVal();

            boolean veq = v.equals(lastv);
            if (lastVal.getValDT() < lastdt && veq)
                return null;

            if (veq)
                return lastVal;
            long cdt = System.currentTimeMillis();
            lastVal = new UAVal(true, v, cdt, cdt);
            return lastVal;
        } catch (Exception e) {
            UAVal r = new UAVal();
            r.setValException("jscode_err", e);
            return r;
        }
    }


    //public String RT_get
}
