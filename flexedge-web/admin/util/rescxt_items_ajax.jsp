<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,
                 java.io.*,
                 java.net.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.util.web.*,
                 cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.res.*,
                 cn.doraro.flexedge.core.util.xmldata.*" %>
<%!
%><%
    if (!Convert.checkReqEmpty(request, out, "res_lib_id"))
        return;
    String res_lib_id = request.getParameter("res_lib_id");
    String res_id = request.getParameter("res_id");

    ResDir rdir = ResManager.getInstance().getResDir(res_lib_id, res_id); //

//ResLib reslib = ResManager.getInstance().getResLibByLibId(res_lib_id) ;
    if (rdir == null) {
        out.print("no ResDir found");
        return;
    }


    List<ResItem> ris = rdir.listResItems();

    out.print("[");
    boolean bfirst = true;
    for (ResItem ri : ris) {
        if (bfirst) bfirst = false;
        else
            out.print(",");
        String name = ri.getName();
        out.print("{id:\"" + ri.getResId() + "\",name:\"" + name + "\"}");
    }
    out.print("]");%>