<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ page
        import="java.util.*,
                java.io.*,
                java.util.*,
                cn.doraro.flexedge.core.*,
                cn.doraro.flexedge.core.util.*,
                cn.doraro.flexedge.core.comp.*,
                java.net.*" %>
<%
    if (!Convert.checkReqEmpty(request, out, "path"))
        return;
    //String op = request.getParameter("op");
    String node_path = request.getParameter("path");
    UANode n = UAUtil.findNodeByPath(node_path);
    if (n == null) {
        out.print("no node found");
    }

    if (!(n instanceof UANodeOCTags)) {
        out.print("not node oc tags");
        return;
    }


    UANodeOCTags ntags = (UANodeOCTags) n;
    List<UAHmi> hmis = null;
    if (n instanceof UANodeOCTagsCxt) {
        UANodeOCTagsCxt ntcxt = (UANodeOCTagsCxt) n;
        hmis = ntcxt.getHmis();
    }
%>