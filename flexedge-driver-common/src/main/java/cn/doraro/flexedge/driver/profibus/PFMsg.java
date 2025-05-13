

package cn.doraro.flexedge.driver.profibus;

public class PFMsg {
    public static final short PK_ACK = 229;
    public static final short SD1 = 16;
    public static final short SD2 = 104;
    public static final short SD3 = 162;
    public static final short SD4 = 220;
    short sd;
    short da;
    short sa;

    public short getStartD() {
        return this.sd;
    }

    public boolean isTokenMsg() {
        return this.sd == 220;
    }

    public short getSorAddr() {
        return this.sa;
    }

    public short getDestAddr() {
        return this.da;
    }
}
