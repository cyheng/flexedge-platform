package cn.doraro.flexedge.core.conn.html;


public class XPathExt {

}

class XPathAttr extends XPathExt {
    String attrName = null;

    public XPathAttr(String pn) {
        this.attrName = pn;
    }

    public String getAttrName() {
        return attrName;
    }
}

