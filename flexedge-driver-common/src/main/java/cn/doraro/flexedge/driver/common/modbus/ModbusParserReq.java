

package cn.doraro.flexedge.driver.common.modbus;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.List;

public abstract class ModbusParserReq extends ModbusParser {
    int[] limitDevIds;

    public ModbusParserReq() {
        this.limitDevIds = null;
    }

    public ModbusParser asLimitDevIds(final List<Integer> devids) {
        if (devids == null || devids.size() <= 0) {
            this.limitDevIds = null;
            return this;
        }
        final int s = devids.size();
        final int[] ids = new int[s];
        for (int i = 0; i < s; ++i) {
            ids[i] = devids.get(i);
        }
        this.limitDevIds = ids;
        return this;
    }

    public boolean checkLimitDevId(final int devid) {
        if (this.limitDevIds == null || this.limitDevIds.length <= 0) {
            return true;
        }
        for (final int did : this.limitDevIds) {
            if (did == devid) {
                return true;
            }
        }
        return false;
    }

    public abstract ModbusCmd parseReqCmdInLoop(final PushbackInputStream p0) throws IOException;
}
