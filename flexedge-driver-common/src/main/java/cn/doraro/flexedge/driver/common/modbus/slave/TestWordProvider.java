// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus.slave;

import cn.doraro.flexedge.core.util.xmldata.XmlVal;

class TestWordProvider extends MSlaveDataProvider {
    @Override
    public XmlVal.XmlValType getDataType() {
        return XmlVal.XmlValType.vt_int16;
    }

    @Override
    protected SlaveData acquireData(final int idx, final int num) {
        final Int16Datas bds = new Int16Datas();
        bds.datas = new short[num];
        final boolean bv = false;
        for (int i = 0; i < num; ++i) {
            bds.datas[i] = (short) (System.currentTimeMillis() % 50000L);
        }
        return bds;
    }

    @Override
    protected boolean injectData(final SlaveData sd) {
        return false;
    }
}
