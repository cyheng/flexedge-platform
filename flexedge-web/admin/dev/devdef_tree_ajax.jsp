<%@ page contentType="text/json;charset=UTF-8" isELIgnored="false" %>
<%@ page import="java.util.*,
                 java.io.*,
                 java.net.*,
                 java.util.*,
                 cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.util.*,
                 java.net.*" %>
<%!
    public static void renderTagGroup(Writer out, UANodeOCTagsGCxt tg) throws Exception {
        String path = tg.getNodePath();
        //out.write("{\"text\": \""+tg.getTitle()+"-"+tg.getName()+"\"") ;
        out.write("{\"text\": \"" + tg.getName() + "\",\"name\":\"" + tg.getName() + "\",\"title\":\"" + tg.getTitle() + "\"");
        out.write(",\"id\": \"" + tg.getId() + "\",\"type\":\"tagg\",\"path\":\""
                + path + "\"");
        out.write(",\"icon\":\"icon_tagg\",\"state\": {\"opened\": true}");
        out.write(",\"children\": [");
        List<UATagG> tgs = tg.getSubTagGs();
        if (tgs != null) {
            boolean bfirst = true;
            for (UATagG subtg : tgs) {
                if (bfirst)
                    bfirst = false;
                else
                    out.write(',');
                renderTagGroup(out, subtg);
            }
            renderHmis(bfirst, out, tg);
        }
        out.write("]}");
    }

    public static void renderTagGroupInDev(Writer out, UANodeOCTagsGCxt dev) throws Exception {
        List<UATagG> tgs = dev.getSubTagGs();
        if (tgs == null)
            return;
        boolean bfirst = true;
        for (UATagG tg : tgs) {
            if (bfirst)
                bfirst = false;
            else
                out.write(',');
            renderTagGroup(out, tg);

        }

        renderHmis(bfirst, out, dev);
    }


    public static void renderHmis(boolean bfirst, Writer out, UANodeOCTagsCxt tagcxt) throws Exception {
        List<UAHmi> hmis = tagcxt.getHmis();
        if (hmis == null || hmis.size() <= 0)
            return;

        for (UAHmi hmi : hmis) {

            if (bfirst) bfirst = false;
            else out.write(",");

            //out.write("{\"text\": \""+hmi.getTitle()+"-"+hmi.getName()+"\",\"title\":\""+hmi.getTitle()+"\"") ;
            out.write("{\"text\": \"" + hmi.getName() + "\",\"title\":\"" + hmi.getTitle() + "\"");
            out.write(",\"id\": \"" + hmi.getId() + "\",\"type\":\"hmi\" ,\"path\":\"" + hmi.getNodePath() + "\"");
            out.write(",\"icon\":\"fa fa-puzzle-piece fa-lg\",\"state\": {\"opened\": true}}");
        }
    }
%><%
    if (!Convert.checkReqEmpty(request, out, "libid", "catid", "devid"))
        return;
    String libid = request.getParameter("libid");
    String catid = request.getParameter("catid");
    String devid = request.getParameter("devid");

    DevDef dev = DevManager.getInstance().getDevDefById(libid, catid, devid);
    if (dev == null) {
        out.print("no dev found");
        return;
    }

%>[
{
"text":"<%=dev.getName() %>"
,"name":"<%=dev.getName()%>"
,"title":"<%=dev.getTitle()%>"
,"id":"<%=dev.getId() %>"
,"type":"dev"
,"path":"<%=dev.getNodePath()%>"
,"state": {"opened": true}
,"icon":"icon_dev"
,"children": [
<%
    renderTagGroupInDev(out, dev);
%>]
}
]
