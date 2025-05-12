// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.fins;

import cn.doraro.flexedge.core.UAVal;
import java.util.HashMap;

public class FinsMode
{
    protected HashMap<String, AreaCode> name2code_bit;
    protected HashMap<String, AreaCode> name2code_word;
    private static FinsMode_CS modelCSCJ;
    private static FinsMode_CS modelCJ2;
    private static FinsMode_CV modeCV;
    
    protected FinsMode() {
        this.name2code_bit = new HashMap<String, AreaCode>();
        this.name2code_word = new HashMap<String, AreaCode>();
    }
    
    protected void setAreaCode(final AreaCode ac) {
        if (ac.isBit()) {
            this.name2code_bit.put(ac.name, ac);
        }
        else {
            this.name2code_word.put(ac.name, ac);
        }
    }
    
    public AreaCode getAreaCodeBit(final String name) {
        return this.name2code_bit.get(name);
    }
    
    public AreaCode getAreaCodeWord(final String name) {
        return this.name2code_word.get(name);
    }
    
    public static FinsMode getMode_CS_CJ1() {
        return FinsMode.modelCSCJ;
    }
    
    public static FinsMode getMode_CJ2() {
        return FinsMode.modelCJ2;
    }
    
    public static FinsMode getMode_CV() {
        return FinsMode.modeCV;
    }
    
    static {
        FinsMode.modelCSCJ = new FinsMode_CS(false);
        FinsMode.modelCJ2 = new FinsMode_CS(true);
        FinsMode.modeCV = new FinsMode_CV();
    }
    
    public static class AreaCode
    {
        String name;
        String title;
        UAVal.ValTP dataTp;
        int code;
        
        public AreaCode(final String n, final String t, final UAVal.ValTP data_tp, final int code) {
            this.name = n;
            this.title = t;
            this.dataTp = data_tp;
            this.code = code;
        }
        
        public String getName() {
            return this.name;
        }
        
        public String getTitle() {
            return this.title;
        }
        
        public short getCode() {
            return (short)this.code;
        }
        
        public boolean isBit() {
            return this.dataTp == UAVal.ValTP.vt_bool;
        }
        
        public boolean isWord() {
            return this.dataTp == UAVal.ValTP.vt_int16 || this.dataTp == UAVal.ValTP.vt_uint16;
        }
    }
}
