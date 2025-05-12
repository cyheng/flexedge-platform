// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.aromat.serial;

import cn.doraro.flexedge.core.dict.DataNode;
import cn.doraro.flexedge.core.util.Lan;

public abstract class AMMsgResp extends AMMsg {
    protected AMMsgReq msgReq;
    char respMark;
    String respCode;
    private String retTxt;

    public AMMsgResp(final AMMsgReq req) {
        this.msgReq = null;
        this.respMark = '\0';
        this.respCode = null;
        this.retTxt = null;
        this.msgReq = req;
    }

    public static String getRespCodeTitle(final String code) {
        final Lan lan = Lan.getLangInPk((Class) AMMsgResp.class);
        final DataNode dn = lan.gn("encode_" + code);
        if (dn == null) {
            return "";
        }
        return dn.getNameByLang("en");
    }

    public char getRespMark() {
        return this.respMark;
    }

    public boolean isRespErr() {
        return this.respMark == '!';
    }

    public boolean isRespNor() {
        return this.respMark == '$';
    }

    public String getRespCode() {
        return this.respCode;
    }

    public String getRetTxt() {
        return this.retTxt;
    }

    protected void parseFrom(final String str) throws Exception {
        this.retTxt = str;
        if ('@' != str.charAt(0)) {
            throw new IllegalArgumentException("no start @");
        }
        this.plcAddr = AMMsg.hex2byte(str.substring(1, 3));
        this.respMark = str.charAt(3);
        this.respCode = str.substring(4, 6);
        if (this.respMark == '!') {
            return;
        }
        if (this.respMark != '$') {
            throw new Exception("unknown resp mark=" + this.respMark);
        }
        final String hl_txt = str.substring(6);
        this.parseRespTxt(hl_txt);
    }

    protected abstract void parseRespTxt(final String p0) throws Exception;
}
