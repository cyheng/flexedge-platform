

package cn.doraro.flexedge.driver.common.modbus;

import java.io.IOException;
import java.io.InputStream;

public abstract class ModbusParser {
    protected static void readFill(final InputStream ins, final byte[] bs, final int offset, final int len) throws IOException {
        int rlen = 0;
        int r = 0;
        while ((r = ins.read(bs, rlen + offset, len - rlen)) >= 0) {
            rlen += r;
            if (rlen == len) {
                return;
            }
        }
        if (r < 0) {
            throw new IOException("end of stream");
        }
    }

    protected short checkFC(final short v) {
        switch (v) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6: {
                return v;
            }
            default: {
                return -1;
            }
        }
    }
}
