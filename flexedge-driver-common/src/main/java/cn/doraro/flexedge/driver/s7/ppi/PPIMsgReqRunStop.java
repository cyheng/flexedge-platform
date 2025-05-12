// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.ppi;

public class PPIMsgReqRunStop extends PPIMsgReq
{
    @Override
    public short getFC() {
        return 0;
    }
    
    @Override
    public int getRetOffsetBytes() {
        return 0;
    }
    
    @Override
    protected short getStartD() {
        return 0;
    }
    
    @Override
    public byte[] toBytes() {
        return null;
    }
}
