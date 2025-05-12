package cn.doraro.flexedge.core.msgnet;

import java.util.List;
import java.util.Map;

public interface IMNCxtPk {
    public List<String> CXT_PK_getSubNames();

    /**
     * writable sub name
     *
     * @return
     */
    public List<String> CXT_PK_getSubNamesW();

    public List<MNCxtValTP> CXT_PK_getSubLimit(String subname);

    public Object CXT_PK_getSubVal(String subname);

    public boolean CXT_PK_setSubVal(String subname, Object subv, StringBuilder failedr);

    /**
     * trans to var to map
     * it will be used by Mustache {{ }} templete out
     *
     * @return
     */
    public Map<String, Object> CXT_PK_toMap();


}
