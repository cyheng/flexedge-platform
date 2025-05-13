<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="
	cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.conn.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.basic.*,
                 java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*" %>
<%
    if (!Convert.checkReqEmpty(request, out, "prjid"))
        return;
    String prjid = request.getParameter("prjid");
    String cpid = request.getParameter("cpid");
    if (cpid == null)
        cpid = "";
%>
<jsp:include page="cp_edit_tcp_server.jsp">
    <jsp:param value="<%=prjid %>" name="prjid"/>
    <jsp:param value="<%=cpid %>" name="cpid"/>
</jsp:include>