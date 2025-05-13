

package cn.doraro.flexedge.driver.omron.fins;

import cn.doraro.flexedge.core.util.IBSOutput;

import java.io.IOException;
import java.io.InputStream;

public class FinsCmdResp extends FinsCmd {
    int errorCode;
    byte[] respVals;

    public FinsCmdResp(final FinsMode fins_mode) {
        super(fins_mode);
        this.errorCode = -1;
        this.respVals = null;
    }

    public static FinsCmdResp readFromStream(final InputStream inputs, final long timeout) throws IOException {
        return null;
    }

    @Override
    protected short getMRC() {
        return 1;
    }

    @Override
    protected short getSRC() {
        return 1;
    }

    @Override
    protected short getICF() {
        return 192;
    }

    @Override
    protected int getParamBytesNum() {
        return 0;
    }

    @Override
    protected void writeParam(final IBSOutput outputs) {
    }
}
