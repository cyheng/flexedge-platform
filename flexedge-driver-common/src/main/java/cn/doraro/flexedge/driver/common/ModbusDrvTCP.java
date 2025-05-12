// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.ConnPt;
import cn.doraro.flexedge.core.DevDriver;
import cn.doraro.flexedge.core.conn.ConnPtCOM;
import cn.doraro.flexedge.driver.common.modbus.ModbusCmd;

import java.util.Arrays;
import java.util.List;

public class ModbusDrvTCP extends ModbusDrvRTU {
    @Override
    public String getName() {
        return "modbus_tcp";
    }

    @Override
    public String getTitle() {
        return "Modbus TCP";
    }

    @Override
    public DevDriver copyMe() {
        return new ModbusDrvTCP();
    }

    public List<Class<? extends ConnPt>> notsupportConnPtClass() {
        return (List<Class<? extends ConnPt>>) Arrays.asList(ConnPtCOM.class);
    }

    @Override
    protected boolean initDriver(final StringBuilder failedr) throws Exception {
        final boolean b = super.initDriver(failedr);
        if (!b) {
            return false;
        }
        for (final ModbusDevItem di : this.modbusDevItems) {
            di.setModbusProtocal(ModbusCmd.Protocol.tcp);
        }
        return true;
    }
}
