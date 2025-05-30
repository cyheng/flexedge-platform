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
    if (!Convert.checkReqEmpty(request, out, "res_node_id"))
        return;
    String resnodeid = request.getParameter("res_node_id");
    ResDir dr = ResManager.getInstance().getResDir(resnodeid);

    if (dr == null) {
        out.print("no ResDir input");
        return;
    }

    String resname = request.getParameter("resname");
    if (Convert.isNullOrEmpty(resname)) {
        return;
    }
    ResItem ri = dr.getResItem(resname);
    if (ri == null) {
        return;
    }
    File rf = ri.getResFile();
    if (!rf.exists())
        return;
    try (FileInputStream fis = new FileInputStream(rf)) {
        WebRes.renderFile(response, ri.getFileName(), fis, true);
    }%>