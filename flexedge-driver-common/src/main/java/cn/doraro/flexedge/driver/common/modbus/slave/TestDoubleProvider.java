

package cn.doraro.flexedge.driver.common.modbus.slave;

import cn.doraro.flexedge.core.util.xmldata.XmlVal;

class TestDoubleProvider extends MSlaveDataProvider {
    @Override
    public XmlVal.XmlValType getDataType() {
        return XmlVal.XmlValType.vt_double;
    }

    @Override
    protected SlaveData acquireData(final int idx, final int num) {
        final DoubleDatas bds = new DoubleDatas();
        bds.datas = new double[num];
        final boolean bv = false;
        for (int i = 0; i < num; ++i) {
            bds.datas[i] = i + 0.1;
        }
        return bds;
    }

    @Override
    protected boolean injectData(final SlaveData sd) {
        return false;
    }
}
