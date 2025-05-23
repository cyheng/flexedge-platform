package cn.doraro.flexedge.core;

import cn.doraro.flexedge.core.UAVal.ValTP;
import cn.doraro.flexedge.core.util.Lan;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * for every defferent device driver,each driver may has it's own addr rule.
 * e.g: 400001  A001 Q0.1
 * address rule may infer data type. bool,int8 int16 etc.
 * and other address may has some function or script to
 * <p>
 * so,it has a abstract class and driver may override it,to support it's own
 *
 * @author zzj
 */
public abstract class DevAddr {
    public static final ChkRes CHK_RES_OK = new ChkRes(1, null, null, null);
    protected String addr = null;
    protected ValTP valTP = null;
    UATag belongTo = null;
    private transient UAVal uaVal = new UAVal();

    /**
     * for config load
     */
    public DevAddr() {
    }

    public DevAddr(String addr, ValTP vtp) {
        this.addr = addr;
        this.valTP = vtp;
    }

    public String getAddr() {
        return addr;
    }

    public UATag getBelongTo() {
        return belongTo;
    }

    /**
     * @param str
     * @return failed return null,and may fill failedr reson desc
     */
    public abstract DevAddr parseAddr(UADev dev, String str, ValTP vtp, StringBuilder failedr);


    public ChkRes checkAddr(UADev dev, String addr, ValTP vtp) {
        return null;
    }


    public abstract boolean isSupportGuessAddr();

    /**
     * support make addr fit to correct automatically
     *
     * @param str
     * @return
     */
    public abstract DevAddr guessAddr(UADev dev, String str, ValTP vtp);

    public abstract List<String> listAddrHelpers();

    public abstract UAVal.ValTP[] getSupportValTPs();

    public final ValTP getValTP() {
        return valTP;
    }


//	public abstract int getRegPos();
//	
//	public abstract int getBitPos() ;

    public abstract boolean canRead();

    public abstract boolean canWrite();


    public abstract String toCheckAdjStr();

    //public abstract int getAddrBitNum() ;

    //public abstract int getAddrByteNum() ;
    //---------------rt run

//	private transient Object rtVal = null ;
//	
//	private transient long lastValDT= System.currentTimeMillis() ;
//	
//	/**
//	 * quality
//	 */
//	private transient boolean lastQT= false ;

    public void writeGuessAdjOut(Writer out) throws IOException {
        String addr = this.toCheckAdjStr();
        if (addr == null)
            addr = "";
        out.write("{\"guess\":true,\"addr\":\"" + addr + "\",\"vt\":" + this.getValTP().getInt() + ",\"canw\":" + this.canWrite() + "}");
    }

    public UAVal RT_getVal() {
        return uaVal;
    }

    public long RT_getValDT() {
        return uaVal.getValDT();
    }

    public Object RT_getValObj() {
        return uaVal.getObjVal();
    }

    public boolean RT_getValQT() {
        return uaVal.isValid();
    }

    public void RT_setVal(Object v) {
        RT_setVal(v, true);
    }

    public void RT_setVal(Object v, boolean chk_chg) {
        UATag tag = this.getBelongTo();
//		if(chk_chg)
//		{
//			if(v==null)
//			{//
//				if(!uaVal.isValid())
//					return ;
//			}
//			else // if(uaVal.isValid() && v.equals(uaVal.getObjVal()))
//			{
//				tag.RT_setVal(v);
//				//uaVal.setValUpDT(System.currentTimeMillis());
//				return ;
//			}
//		}
//
//		if(v==null)
//		{
//			uaVal.setVal(false, null, System.currentTimeMillis());
//			tag.RT_setUAVal(uaVal);
//		}
//		else
//		{
//			uaVal.setVal(true, v, System.currentTimeMillis());
//			tag.RT_setValRaw(v);
//		}
        //tag.RT_setVal(v);
        //设置值，如果v=null表示错误，但可能会被内部抗干扰过滤
        tag.RT_setValRaw(v);
    }

    public void RT_setValErr() {
        RT_setValErr("");
    }

    /**
     * 强制设置错误
     */
    public void RT_setValErr(String errinfo) {
        UATag tag = this.getBelongTo();
        tag.RT_setValErr(errinfo, null);
    }

    public static interface IAddrDefSeg {
        String getRangeFrom();

        String getRangeTo();

        ValTP[] getValTPs();

        public boolean isWritable();

        public String getSample();


        public default String getRangeStr() {
            return getRangeFrom() + " - " + getRangeTo();
        }

        public default String getValTPsStr() {
            StringBuilder sb = new StringBuilder();
            boolean bfirst = true;
            for (ValTP vtp : this.getValTPs()) {
                if (bfirst) bfirst = false;
                else sb.append(",");
                sb.append(vtp.getStr());
            }
            return sb.toString();
        }

        public default String getReadWriteStr() {
            Lan lan = Lan.getLangInPk(DevAddr.class);
            if (isWritable())
                return lan.g("RW");
            else
                return lan.g("R");
        }
    }

    public static interface IAddrDef {
        public String getDefTypeForDoc();

        public List<IAddrDefSeg> getSegsForDoc();
    }

    /**
     * check addr result
     *
     * @author jason.zhu
     */
    public static class ChkRes {
        String addr = null;

        ValTP valTp = null;

        int chkVal; //-1 invalid addr  0-need modify 1-is ok,do not chg

        /**
         * chk prompt
         */
        String chkPrompt;

        public ChkRes(int chkv, String addr, ValTP vtp, String prompt) {
            this.addr = addr;
            this.valTp = vtp;
            this.chkVal = chkv;
            this.chkPrompt = prompt;
        }

        public String getAddr() {
            return addr;
        }

        public ValTP getValTP() {
            return this.valTp;
        }

        public int getChkVal() {
            return this.chkVal;
        }

        public boolean isChkOk() {
            return this.chkVal > 0;
        }

        public String getChkPrompt() {
            return chkPrompt;
        }

        public String toJsonStr() {
            String r = "{\"res\":" + this.chkVal;
            if (addr != null) {
                r += ",\"addr\":\"" + addr + "\"";
            }

            if (valTp != null) {
                r += ",\"vt\":" + valTp.getInt();
            }

            if (chkPrompt != null)
                r += ",\"prompt\":\"" + chkPrompt + "\"";

            r += "}";
//			if(chkres.getChkVal()<0)
//			{
//				throw new IllegalArgumentException(chkres.getChkFailedReson()) ;
//			}
//			else if(chkres.getChkVal()==0)
//			{
//				DevAddr mayda = chkres.getDevAddr() ;
//				throw new IllegalArgumentException("may be addr="+mayda.getAddr()+" valtp="+mayda.getValTP()) ;
//			}

            return r;
        }
    }
}
