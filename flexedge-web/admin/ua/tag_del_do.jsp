<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="
	cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.basic.*,
                 java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*" %>
<%
    if (!Convert.checkReqEmpty(request, out, "repid", "id"))
        return;
    String repid = request.getParameter("repid");
    String id = request.getParameter("id");

    UAManager uam = UAManager.getInstance();
    UAPrj dc = uam.getPrjById(repid);
    if (dc == null) {
        out.print("no rep found with id=" + repid);
        return;
    }

    UANode n = dc.findNodeById(id);
    if (n == null) {
        out.print("no node found");
        return;
    }
    if (!(n instanceof UATag)) {
        out.print("not tag node found");
        return;
    }

    boolean b = ((UATag) n).delFromParent();
    if (!b) {
        out.print("del err");
        return;
    } else {
        out.print("succ=" + id);
        return;
    }
%>