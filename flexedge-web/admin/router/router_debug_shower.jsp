<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="org.json.*,
                 cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.node.*,
                 cn.doraro.flexedge.core.router.*,
                 cn.doraro.flexedge.ext.roa.*,
                 cn.doraro.flexedge.core.basic.*,
                 cn.doraro.flexedge.core.util.web.*,
                 java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*" %>
<%@ taglib uri="wb_tag" prefix="w" %>
<%
%>
<html>
<head>
    <title></title>
    <jsp:include page="../head.jsp"></jsp:include>
    <script>
        dlg.resize_to(600, 500);
    </script>
    <style>
        .conf {
            position: relative;
            width: 90%;
            height: 30px;
            border: 1px solid;
        }
    </style>
</head>
<body>
<form class="layui-form" action="">
    <div class="layui-form-item">
        <label class="layui-form-label"><w:g>date</w:g>:</label>
        <div class="layui-input-inline" style="width: 150px;">
            <input type="text" id="dt" name="dt" value="" autocomplete="off" class="layui-input">
        </div>
    </div>
    <div class="layui-form-item">
        <div class="layui-form-label"><w:g>data</w:g>:</div>
        <div class="layui-input-inline" style="width:450px;font-size:10px;">
            <textarea type="text" id="d" name="d" style="height:300px;" autocomplete="off"
                      class="layui-input"></textarea>
        </div>
    </div>
</form>
</body>
<script type="text/javascript">

    var pm = dlg.get_opener_opt("pm");
    if (pm) {
        $("#dt").val(new Date(pm.dt).format("yyyy-MM-dd hh:mm:ss"));

        $("#d").val(JSON.stringify(JSON.parse(pm.d), null, '\t'));
    }

    layui.use('form', function () {
        var form = layui.form;


        form.render();
    });

    function win_close() {
        dlg.close(0);
    }


</script>
</html>