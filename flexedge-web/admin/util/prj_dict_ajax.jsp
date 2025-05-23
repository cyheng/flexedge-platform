<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,
                 java.io.*,
                 cn.doraro.flexedge.core.*,
                 org.json.*,
                 cn.doraro.flexedge.core.basic.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.dict.*,
                 cn.doraro.flexedge.core.util.xmldata.*,
                 cn.doraro.flexedge.core.util.web.*,
                 cn.doraro.flexedge.core.comp.*" %>
<%!

%><%
    if (!Convert.checkReqEmpty(request, out, "prjid", "op"))
        return;

    String op = request.getParameter("op");
    String prjid = request.getParameter("prjid");
    String classid = request.getParameter("cid");
    String nname = request.getParameter("name");

    UAPrj prj = UAManager.getInstance().getPrjById(prjid);
    if (prj == null) {
        out.print("no prj found");
        return;
    }

    PrjDataClass pdc = DictManager.getInstance().getPrjDataClassByPrjId(prjid);

    String name = request.getParameter("name");
    String title = request.getParameter("title");
    DataClass dc = null;
    if (Convert.isNotNullEmpty(classid)) {
        dc = pdc.getDataClassById(classid);
        if (dc == null) {
            out.print("no DataClass found");
            return;
        }
    }

    DataNode dn = null;
    switch (op) {
        case "edit_dc":
        case "add_dc":
            String bind_for = request.getParameter("bind_for");
            String bs = request.getParameter("bind_style");
            DataClass.BindStyle bind_sty = null;
            if (Convert.isNullOrEmpty(bs))
                bind_sty = DataClass.BindStyle.s;
            else
                bind_sty = DataClass.BindStyle.valueOf(bs);
            boolean benable = !"false".equals(request.getParameter("enable"));
            try {
                if (dc == null)
                    dc = pdc.addDataClass(name, title, benable, bind_for, bind_sty, null);
                else
                    pdc.updateDataClass(classid, name, title, benable, bind_for, bind_sty, null);

                out.print("succ");
            } catch (Exception e) {
                //e.printStackTrace();
                out.print(e.getMessage());
            }
            break;
        case "del_dc":
            if (!Convert.checkReqEmpty(request, out, "cid"))
                return;
            if (pdc.delDataClass(classid))
                out.print("succ");
            else
                out.print("del error");
            break;
        case "dc_imp_txt":
            if (!Convert.checkReqEmpty(request, out, "txt"))
                return;
            String txt = request.getParameter("txt");
            List<DataNode> newdns = pdc.impDataNodeByTxt(classid, txt);
            if (newdns.size() > 0)
                out.print("succ");
            else
                out.print("no DataNode imported");
            return;
        case "export":
            if (!Convert.checkReqEmpty(request, out, "taskid"))
                return;

            break;
        case "edit_dn":
            if (!Convert.checkReqEmpty(request, out, "name"))
                return;
        case "add_dn":
            if (!Convert.checkReqEmpty(request, out, "name"))
                return;
            try {
                DataNode newdn = pdc.addOrUpdateDataNode(classid, name, title);
                out.print("succ");
            } catch (Exception e) {
                e.printStackTrace();
                out.print(e.getMessage());
            }
            break;
        case "del_dn":
            if (!Convert.checkReqEmpty(request, out, "cid", "name"))
                return;
            dc.delDataNode(name);
            out.print("succ");
            return;
        case "list_dns":
            JSONArray jarr = new JSONArray();
            for (DataNode tmpdn : dc.getRootNodes()) {
                jarr.put(tmpdn.toSimpleJO());
            }
            out.print(jarr.toString());
            return;
        case "list_html":
            for (DataNode tmpdn : dc.getRootNodes()) {
%>
<tr id="<%=tmpdn.getName()%>">
    <td></td>
    <td><%=tmpdn.getName() %>
    </td>
    <td><%=tmpdn.getTitle() %>
    </td>
    <td><a href="javascript:add_or_edit_dn('<%=prjid %>','<%=dc.getClassId()%>','<%=tmpdn.getName()%>')"><i
            title="Edit Data Node" class="fa fa-pencil " aria-hidden="true"></i></a></td>
</tr>
<%
            }
            break;
    }
%>