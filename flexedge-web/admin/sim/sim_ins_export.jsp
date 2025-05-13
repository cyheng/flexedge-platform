<%@page import="cn.doraro.flexedge.core.util.web.WebRes" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="
	cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.comp.*,
                 cn.doraro.flexedge.core.sim.*,
                 java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*" %>
<%
    if (!Convert.checkReqEmpty(request, out, "insid"))
        return;

    String insid = request.getParameter("insid");
    SimManager simmgr = SimManager.getInstance();
    SimInstance ins = simmgr.getInstance(insid);
    if (ins == null) {
        out.print("no instance found");
        return;
    }

    simmgr.exportIns(response, insid);
%>