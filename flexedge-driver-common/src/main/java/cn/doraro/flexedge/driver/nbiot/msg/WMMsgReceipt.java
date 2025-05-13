

package cn.doraro.flexedge.driver.nbiot.msg;

import java.util.ArrayList;

public class WMMsgReceipt extends WMMsgDT {
    public static final int TP_NOR = 0;
    public static final int TP_ALERT = 1;
    public static final int TP_TESTER = 2;
    int receiptTp;
    boolean bContinue;

    public WMMsgReceipt() {
        this.receiptTp = 0;
        this.bContinue = false;
        this.func = new byte[]{1, 1};
    }

    public void setReceiptTp(final int tp) {
        switch (tp) {
            case 0: {
                this.func = new byte[]{1, 1};
                break;
            }
            case 1: {
                this.func = new byte[]{7, 7};
                break;
            }
            case 2: {
                this.func = new byte[]{16, 16};
                break;
            }
            default: {
                throw new IllegalArgumentException("invalid receipt tp");
            }
        }
        this.receiptTp = tp;
    }

    public boolean isReceiptAlert() {
        return this.func != null && this.func[0] == 7 && this.func[1] == 7;
    }

    public boolean isContinue() {
        return this.bContinue;
    }

    public void setContinue(final boolean b) {
        this.bContinue = b;
    }

    @Override
    protected ArrayList<byte[]> getMsgBody() {
        final ArrayList<byte[]> bbs = super.getMsgBody();
        if (this.bContinue) {
            bbs.add(new byte[]{-86});
        } else {
            bbs.add(new byte[]{0});
        }
        return bbs;
    }

    @Override
    public String toString() {
        final String ret = super.toString();
        return "Receipt:" + ret;
    }
}
