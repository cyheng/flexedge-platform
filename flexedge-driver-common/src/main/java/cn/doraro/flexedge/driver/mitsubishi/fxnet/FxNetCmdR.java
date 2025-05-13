

package cn.doraro.flexedge.driver.mitsubishi.fxnet;

import java.io.InputStream;
import java.io.OutputStream;

public class FxNetCmdR extends FxNetCmd {
    @Override
    public boolean doCmd(final InputStream inputs, final OutputStream outputs) throws Exception {
        return false;
    }
}
