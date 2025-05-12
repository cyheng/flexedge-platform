// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus;

import java.util.HashMap;

public interface ModbusRunListener {
    void onModbusReadData(final ModbusCmd p0, final Object[] p1) throws Exception;

    void onModbusReadChanged(final ModbusCmd p0, final HashMap<Integer, Object> p1);

    void onModbusReadFailed(final ModbusCmd p0);

    void onModbusCmdRunError() throws Exception;
}
