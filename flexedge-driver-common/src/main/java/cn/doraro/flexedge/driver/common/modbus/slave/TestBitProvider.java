// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus.slave;

import cn.doraro.flexedge.core.util.xmldata.XmlVal;

class TestBitProvider extends MSlaveDataProvider
{
    @Override
    public XmlVal.XmlValType getDataType() {
        return XmlVal.XmlValType.vt_bool;
    }
    
    @Override
    protected SlaveData acquireData(final int idx, final int num) {
        final BoolDatas bds = new BoolDatas();
        bds.datas = new boolean[num];
        boolean bv = false;
        bv = (System.currentTimeMillis() % 2L == 0L);
        for (int i = 0; i < num; ++i) {
            bds.datas[i] = bv;
        }
        return bds;
    }
    
    @Override
    protected boolean injectData(final SlaveData sd) {
        return false;
    }
}
