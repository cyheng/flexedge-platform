<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,
                 java.io.*,
                 cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.task.*,
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
<div class="sel_item">
    <button class="layui-btn " style="width:80%" onclick="go_to('jdbc','JDBC')">JDBC(MySql,Sql Server..)</button>
</div>
<div class="sel_item">
    <button class="layui-btn " style="width:80%" onclick="go_to('influxdb','InfluxDB')">Influx DB</button>
</div>
<div class="sel_item">
    <button class="layui-btn layui-btn-disabled" style="width:80%" onclick0="go_to('iotdb','IoT DB')">IOT DB</button>
</div>
<script>
    function go_to(tp, tt) {
        //document.location.href="quote_edit.jsp?tp="+tp ;
        dlg.close({tp: tp, tt: tt});
    }
</script>
</body>
</html>
