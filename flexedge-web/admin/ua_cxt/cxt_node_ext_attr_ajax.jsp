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
<%!

%><%
    if (!Convert.checkReqEmpty(request, out, "op", "path"))
        return;
    String op = request.getParameter("op");
    String path = request.getParameter("path");
    UANode n = UAUtil.findNodeByPath(path);
    if (n == null) {
        out.print("no node with path=" + path);
        return;
    }
    UANode topn = n.getTopNode();
    if (!(topn instanceof UAPrj)) {
        out.print("no prj found");
        return;
    }
    UAPrj prj = (UAPrj) topn;

    switch (op) {
        case "set_ext_attr":
            String jstr = request.getParameter("jstr");
            if (jstr == null)
                jstr = "";
            try {
                n.setExtAttrStr(jstr);
                prj.save();
                out.print("succ=" + n.getId());
            } catch (Exception e) {
                out.print(e.getMessage());
            }
            break;

    }

%>