package cn.doraro.flexedge.core.msgnet.util;

import cn.doraro.flexedge.core.util.Lan;

public enum RepeatTP {
    intv(1), intv_bt(2);

    private final int val;

    RepeatTP(int v) {
        val = v;
    }

    public static RepeatTP valOfInt(int i) {
        switch (i) {
//		case 0:
//			return none;
            case 1:
                return intv;
            case 2:
                return intv_bt;
            default:
                return null;
        }
    }

    public int getInt() {
        return val;
    }

    public String getTitle() {
        Lan lan = Lan.getLangInPk(RepeatTP.class);
        return lan.g("reptp_" + this.name());
    }

}
