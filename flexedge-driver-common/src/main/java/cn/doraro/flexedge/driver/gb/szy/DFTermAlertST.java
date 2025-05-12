// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.gb.szy;

public class DFTermAlertST extends DataField {
    int alert;
    int term;

    public DFTermAlertST(final byte[] bs) {
        if (bs.length != 4) {
            throw new IllegalArgumentException("alert st bs len=4");
        }
        this.alert = (bs[1] & 0xFF);
        this.alert <<= 8;
        this.alert += (bs[0] & 0xFF);
        this.term = (bs[3] & 0xFF);
        this.term <<= 8;
        this.term += (bs[2] & 0xFF);
    }

    public int getAlertInf() {
        return this.alert;
    }

    public int getTermInf() {
        return this.term;
    }
}
