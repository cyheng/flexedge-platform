<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,
                 java.io.*,
                 cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.router.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.dict.*,
                 cn.doraro.flexedge.core.store.*,
                 cn.doraro.flexedge.core.comp.*" %>
<%!

%><%

%>
<%@ taglib uri="wb_tag" prefix="wbt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title></title>
    <jsp:include page="../head.jsp"></jsp:include>
    <style>
        .sel_item {
            width: 80%;
            margin: 20px;
            margin-left: 60px;
            align-content: center;
        }
    </style>
    <script type="text/javascript">
        dlg.resize_to(350, 400);
    </script>
</head>
<body>
<%
    for (RouterInnCollator ric : RouterInnCollator.listRICAll()) {
%>
<div class="sel_item">
    <button class="layui-btn " style="width:80%"
            onclick="go_to('<%=ric.getTp() %>','<%=ric.getTpTitle() %>')"><%=ric.getTpTitle() %>
    </button>
</div>
<%
    }
%>
<script>
    function go_to(tp, tt) {
        dlg.close({tp: tp, tt: tt});
    }
</script>
</body>
</html>
