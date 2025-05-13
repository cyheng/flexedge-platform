<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="
	cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.util.*,
                 java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*,
                 org.json.*,
                 cn.doraro.flexedge.core.*" %>
<%
    if (!Convert.checkReqEmpty(request, out, "id"))
        return;
    String repid = request.getParameter("id");

    UAPrj rep = UAManager.getInstance().getPrjById(repid);
    if (rep == null) {
        out.print("no rep found");
        return;
    }

    JSONObject jobj = rep.toOCDynJSON(-1);
    out.print(jobj.toString(2));
%>


