package cn.doraro.flexedge.core.util.xmldata;

public interface IXmlDataValidator {
    public XmlData toXmlData();

    public boolean fromXmlData(XmlData xd, StringBuilder failedr);
}
