// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.fins;

import cn.doraro.flexedge.core.util.ILang;

public class FinsEndCode implements ILang
{
    int mainCode;
    boolean networkRelayErr;
    int subCode;
    boolean fatalCPUUnitErr;
    boolean nonfatalCPUUnitError;
    
    public FinsEndCode(final short main, final short sub) {
        this.networkRelayErr = false;
        this.fatalCPUUnitErr = false;
        this.nonfatalCPUUnitError = false;
        this.mainCode = (main & 0x7F);
        this.networkRelayErr = ((main & 0x80) > 0);
        this.subCode = (sub & 0x3F);
        this.fatalCPUUnitErr = ((sub & 0x80) > 0);
        this.nonfatalCPUUnitError = ((sub & 0x40) > 0);
    }
    
    public boolean isNormal() {
        return this.mainCode == 0 && this.subCode == 0;
    }
    
    public int getMainCode() {
        return this.mainCode;
    }
    
    public boolean isNetworkRelayErr() {
        return this.networkRelayErr;
    }
    
    public int getSubCode() {
        return this.subCode;
    }
    
    public boolean isFatalCPUUnitErr() {
        return this.fatalCPUUnitErr;
    }
    
    public boolean isNonfatalCPUUnitErr() {
        return this.nonfatalCPUUnitError;
    }
    
    public String getMainTitle() {
        return this.g("endc_" + this.mainCode);
    }
    
    public String getSubTitle() {
        return this.g("endc_" + this.mainCode + "_" + this.subCode);
    }
    
    public String getCheckPoint() {
        return this.g("endc_" + this.mainCode + "_" + this.subCode, "chkpt", "");
    }
    
    public String getProbableCause() {
        return this.g("endc_" + this.mainCode + "_" + this.subCode, "cause", "");
    }
    
    public String getCorrection() {
        return this.g("endc_" + this.mainCode + "_" + this.subCode, "correction", "");
    }
    
    public String getErrorInf() {
        return this.mainCode + " " + this.subCode;
    }
}
