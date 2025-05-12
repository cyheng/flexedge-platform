// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink;

import cn.doraro.flexedge.core.dict.DataNode;
import cn.doraro.flexedge.core.util.Lan;

public abstract class HLMsgResp extends HLMsg
{
    protected HLMsgReq hlMsgReq;
    String endCode;
    private String retTxt;
    String headCode;
    
    public HLMsgResp(final HLMsgReq req) {
        this.hlMsgReq = null;
        this.endCode = null;
        this.retTxt = null;
        this.headCode = null;
        this.hlMsgReq = req;
    }
    
    public String getHLEndCode() {
        return this.endCode;
    }
    
    @Override
    public String getHeadCode() {
        return this.headCode;
    }
    
    public String getRetTxt() {
        return this.retTxt;
    }
    
    protected void parseFrom(final String str) throws Exception {
        this.retTxt = str;
        if ('@' != str.charAt(0)) {
            throw new IllegalArgumentException("no start @");
        }
        this.plcUnit = HLMsg.bcd2_to_byte(str.charAt(1), str.charAt(2));
        this.headCode = str.substring(3, 5);
        this.endCode = str.substring(5, 7);
        final String hl_txt = str.substring(7);
        this.parseHLTxt(hl_txt);
    }
    
    protected abstract void parseHLTxt(final String p0) throws Exception;
    
    public static String getEndCodeTitle(final String end_code) {
        final Lan lan = Lan.getLangInPk((Class)HLMsgResp.class);
        final DataNode dn = lan.gn("encode_" + end_code);
        if (dn == null) {
            return "";
        }
        return dn.getNameByLang("en");
    }
}
