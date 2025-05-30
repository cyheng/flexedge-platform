<%@ page contentType="text/json;charset=UTF-8" isELIgnored="false" %>
<%@ page import="java.util.*,
                 java.io.*,
                 java.net.*,
                 java.util.*,
                 cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.util.*,
                 java.net.*" %>
<%!
    public static void renderTagGroup(Writer out, UATagG tg, boolean b_sel, boolean cont_only) throws Exception {
        out.write("{\"text\": \"" + tg.getName() + "\",\"a_attr\":{\"title\":\"" + Convert.plainToJsStr(tg.getTitle()) + "\"}");
        out.write(",\"id\": \"" + tg.getId() + "\",\"type\":\"tagg\" ,\"path\":\"" + tg.getNodePath() + "\"");
        out.write(",\"icon\":\"icon_tagg\",\"state\": {\"opened\": false}");
        out.write(",\"in_dev\":" + tg.isInDev());
        out.write(",\"ref_locked\":" + tg.isRefLocked());
        out.write(",\"state\": {\"opened\": " + (b_sel ? "true" : "false") + "}");
        out.write(",\"children\": [");
        List<UATagG> tgs = tg.getSubTagGs();
        boolean bfirst = true;
        if (tgs != null) {

            for (UATagG subtg : tgs) {
                if (bfirst)
                    bfirst = false;
                else
                    out.write(',');
                renderTagGroup(out, subtg, b_sel, cont_only);
            }
        }
        if (!cont_only)
            renderHmis(bfirst, out, tg);
        out.write("]}");
    }

    public static void renderTagGroupInCxt(boolean bfirst, Writer out, UANodeOCTagsGCxt dev, boolean b_sel, boolean cont_only) throws Exception {
        //DevDef dd = dev.getDevDef();
        List<UATagG> tgs = dev.getSubTagGs();
        //boolean bfirst = true ;
        if (tgs != null) {
            for (UATagG tg : tgs) {
                if (bfirst)
                    bfirst = false;
                else
                    out.write(',');
                renderTagGroup(out, tg, b_sel, cont_only);
            }
        }

        if (!cont_only)
            renderHmis(bfirst, out, dev);
    }

    public static void renderHmis(boolean bfirst, Writer out, UANodeOCTagsCxt tagcxt) throws Exception {
        List<UAHmi> hmis = tagcxt.getHmis();
        if (hmis == null || hmis.size() <= 0)
            return;

        for (UAHmi hmi : hmis) {

            if (bfirst) bfirst = false;
            else out.write(",");

            boolean ref = hmi.isRefedNode();
            out.write("{\"text\": \"" + hmi.getName() + "\",\"a_attr\":{\"title\":\"" + Convert.plainToJsStr(hmi.getTitle() + "[" + hmi.getId() + "]") + "\"},\"ref\":" + ref);
            out.write(",\"id\": \"" + hmi.getId() + "\",\"type\":\"hmi\" ,\"path\":\"" + hmi.getNodePath() + "\",\"main_ui\":" + hmi.isMainInPrj());
            out.write(",\"tp\":\"hmi\",\"icon\":\"icon_hmi\",\"state\": {\"opened\": true}}");
        }
    }
%><%
    if (!Convert.checkReqEmpty(request, out, "id"))
        return;
    String id = request.getParameter("id");
    boolean b_sel = "true".equals(request.getParameter("sel"));
    boolean cont_only = "true".equals(request.getParameter("cont_only"));
    UAPrj rep = UAManager.getInstance().getPrjById(id);
    if (rep == null) {
        out.print("no repository found!");
        return;
    }
%>[

{
"text":"<%=rep.getName() %>"
,"id":"<%=rep.getId() %>","a_attr":{"title":"<%=Convert.plainToJsStr(rep.getTitle())%>"}
,"type":"prj"
,"path":"<%=rep.getNodePath()%>"
,"icon": "icon_prj"
,"state": {"opened": true}
,"children": [

<%
    boolean bf1 = true;
    for (UACh ch : rep.getChs()) {
        if (bf1)
            bf1 = false;
        else
            out.print(",");
        String drvfit = "";
        DevDriver drv = ch.getDriver();
        String drvt = "none";
        if (drv != null)
            drvt = drv.getTitle();
        boolean hasdrv = ch.hasDriver();
        if (hasdrv) {
            if (!ch.isDriverFit())
                drvfit = "<span class=tn_warn title='" + drvt + " is not fit'>drv?</span>";
            else
                drvfit = "<span class=tn_ok title='" + drvt + "'>drv</span>";
        }

        if (b_sel)
            drvfit = "";

        boolean b_connpt_to_dev = false;
        if (drv != null) {
            b_connpt_to_dev = drv.isConnPtToDev();
        }

        String sub_devids = "";
        boolean bsubf = true;
        for (UADev dev : ch.getDevs()) {
            if (bsubf) bsubf = false;
            else
                sub_devids += ",";
            sub_devids += dev.getId();
        }
%>
{
"text":"<img id='ch_<%=ch.getId()%>' src='/admin/inc/sm_icon_ch.png'
can_connpt_bind='<%=b_connpt_to_dev ? false : true%>' dev_ids='<%=sub_devids%>' /><%if (hasdrv) {%><i
id='ch_run_<%=ch.getId()%>' class='fa fa-cog fa-lg'></i><%}%>&nbsp;<span title='<%=ch.getTitle()%>'><%=ch.getName() %>
</span><%=drvfit%>"
,"id":"<%=ch.getId() %>","a_attr":{"title":"<%=Convert.plainToJsStr(ch.getTitle())%>"}
,"type":"ch"
,"path":"<%=ch.getNodePath()%>"
,"state": {"opened": <%=(b_sel ? "true" : "false")%>}
,"icon":"icon_ch_hidden"
,"children": [
<%
    boolean bf2 = true;
    for (UADev dev : ch.getDevs()) {
        if (bf2)
            bf2 = false;
        else
            out.print(",");
        String devok = "";
        DevDef devdef = dev.getDevDef();

        String model_n = "";
        String model_t = "";
        DevDriver.Model dm = dev.getDrvDevModel();
        if (dm != null) {
            model_n = "(" + dm.getName() + ")";
            model_t = "(" + dm.getTitle() + ")";
        }

        String deft = "";
        if (devdef == null) {
            devok = "";//"<span class=tn_warn title='"+dev.getTitle()+" is not def'>dev?</span>" ;
        } else {
            deft = devdef.getTitle();
        }
        if (Convert.isNotNullEmpty(deft))
            deft = "[" + deft + "]";
        boolean ref_locked = dev.isRefLocked();
%>
{
"text":"<img id='dev_<%=ch.getId()%>-<%=dev.getId()%>' src='/admin/inc/sm_icon_dev.png' style='width:18px;height:18px'
can_connpt_bind='<%=b_connpt_to_dev ? true : false%>' /><span title='<%=dev.getTitle()%><%=model_t%>
'><%=dev.getName() %><%=model_n%></span><%=deft%> <%=devok%>"
,"id":"<%=dev.getId() %>","a_attr":{"title":"<%=Convert.plainToJsStr(dev.getTitle())%>"}
,"type":"dev","ref_locked":<%=ref_locked%>
,"path":"<%=dev.getNodePath()%>"
,"state": {"opened": <%=(b_sel ? "true" : "false")%>}
,"icon":"icon_dev_hidden"
,"children": [
<%
    renderTagGroupInCxt(true, out, dev, b_sel, cont_only);
%>
]
}
<%
    }
    renderTagGroupInCxt(bf2, out, ch, b_sel, cont_only);
    //renderHmis(bf2,out,ch) ;
%>
]
}
<%
    }
    if (!cont_only)
        renderHmis(bf1, out, rep);
%>

]}

]
