// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.aromat.serial;

import cn.doraro.flexedge.core.util.Lan;

public abstract class AMMsgReqRC extends AMMsgReq
{
    public static final char CONTACT_CODE_X = 'X';
    public static final char CONTACT_CODE_Y = 'Y';
    public static final char CONTACT_CODE_R = 'R';
    public static final char CONTACT_CODE_L = 'L';
    public static final char CONTACT_CODE_T = 'T';
    public static final char CONTACT_CODE_C = 'C';
    
    public static String getContactCodeTitle(final char c) {
        final Lan lan = Lan.getLangInPk((Class)AMMsgReqRC.class);
        return lan.g("contact_" + c);
    }
    
    @Override
    public String getCmdCode() {
        return "RC";
    }
}
