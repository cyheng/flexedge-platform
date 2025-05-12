package cn.doraro.flexedge.core.util.filter;

import cn.doraro.flexedge.core.util.xmldata.IXmlDataable;
import cn.doraro.flexedge.core.util.xmldata.XmlData;


/**
 * @author jasonzhu
 */
public abstract class AbstractFilter implements IXmlDataable {
    public abstract String getFilterName();

    public abstract String getFilterTitle();

    /**
     * ����������-ʹ�÷�Χ����Ч������
     *
     * @return
     */
    public abstract String getFilterDesc();


    public void initFilter(XmlData xd) {

    }


    public abstract double filter(double d);


    public XmlData toXmlData() {
        XmlData xd = new XmlData();
        xd.setParamValue("_n", getFilterName());
        return xd;
    }

    public void fromXmlData(XmlData xd) {
        initFilter(xd);
    }
}
