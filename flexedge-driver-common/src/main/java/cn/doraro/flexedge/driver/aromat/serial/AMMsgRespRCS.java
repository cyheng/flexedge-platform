// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.aromat.serial;

public class AMMsgRespRCS extends AMMsgResp {
    Boolean bOffOn;

    public AMMsgRespRCS(final AMMsgReq req) {
        super(req);
        this.bOffOn = null;
    }

    @Override
    protected void parseRespTxt(final String resp_txt) throws Exception {
        final char c = resp_txt.charAt(0);
        if (c == '1') {
            this.bOffOn = true;
        } else if (c == '0') {
            this.bOffOn = false;
        }
    }

    public Boolean getRespOffOn() {
        return this.bOffOn;
    }
}
