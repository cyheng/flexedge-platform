

package cn.doraro.flexedge.driver.common.modbus.slave;

import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.xmldata.XmlVal;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public abstract class MSlave {
    private ArrayList<MSlaveDataProvider> bitPros;
    private ArrayList<MSlaveDataProvider> wordPros;

    public MSlave() {
        this.bitPros = new ArrayList<MSlaveDataProvider>();
        this.wordPros = new ArrayList<MSlaveDataProvider>();
    }

    void init(final Element ele) {
        for (final Element dele : Convert.getSubChildElement(ele, "data")) {
            final MSlaveDataProvider dp = this.createProvider(dele);
            if (dp != null) {
                if (dp.getDataType() == XmlVal.XmlValType.vt_bool) {
                    this.bitPros.add(dp);
                } else {
                    this.wordPros.add(dp);
                }
            }
        }
    }

    private MSlaveDataProvider createProvider(final Element ele) {
        try {
            final String procn = ele.getAttribute("provider");
            if (Convert.isNullOrEmpty(procn)) {
                return null;
            }
            final Class c = Class.forName(procn);
            final MSlaveDataProvider dp = c.newInstance();
            dp.init(ele);
            return dp;
        } catch (final Exception ee) {
            ee.printStackTrace();
            return null;
        }
    }

    public List<MSlaveDataProvider> getBitDataProviders() {
        return this.bitPros;
    }

    public List<MSlaveDataProvider> getWordDataProviders() {
        return this.wordPros;
    }

    public abstract void start();

    public abstract void stop();
}
