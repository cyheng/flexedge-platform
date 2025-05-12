// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.aromat.serial;

public class AMMsgReqRCS extends AMMsgReqRC
{
    char contactC;
    int contanctN;
    
    public AMMsgReqRCS asContactCode(final char cc, final int num) {
        if (num > 9999) {
            throw new IllegalArgumentException("num is too big");
        }
        this.contactC = cc;
        this.contanctN = num;
        return this;
    }
    
    @Override
    protected void packContent(final StringBuilder sb) {
        sb.append('S');
        sb.append(this.contactC);
        sb.append(AMMsg.byte_to_bcd4(this.contanctN));
    }
    
    @Override
    protected AMMsgResp newRespInstance() {
        return new AMMsgRespRCS(this);
    }
}
