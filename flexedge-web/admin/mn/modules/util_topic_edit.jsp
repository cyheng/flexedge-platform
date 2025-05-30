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
    String newid = IdCreator.newSeqId();
%>
<html>
<head>
    <title></title>
    <jsp:include page="../../head.jsp"></jsp:include>
    <script>
        dlg.resize_to(550, 300);
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
        <label class="layui-form-label"><w:g>title</w:g>:</label>
        <div class="layui-input-inline" style="width: 250px;">
            <input type="text" id="t" name="t" value="" autocomplete="off" class="layui-input">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label"><w:g>topic</w:g>:</label>
        <div class="layui-input-inline" style="width: 250px;">
            <input type="text" id="topic" name="topic" value="" autocomplete="off" class="layui-input">
        </div>

    </div>
    <div class="layui-form-item">
        <div class="layui-form-label"><w:g>desc</w:g>:</div>
        <div class="layui-input-inline" style="width: 350px;">
            <textarea type="text" id="d" name="d" style="height:60px;" autocomplete="off"
                      class="layui-input"></textarea>
        </div>
    </div>
</form>
</body>
<script type="text/javascript">

    var id = "<%=newid%>";
    var pm = dlg.get_opener_opt("pm");
    if (pm && pm.id) {
        id = pm.id;
    }

    layui.use('form', function () {
        var form = layui.form;

        update_ui();

        form.render();
    });

    function update_ui() {
        //console.log(pm) ;
        if (!pm)
            return;
        //$("#n").val(pm.n) ;
        $("#t").val(pm.t);
        $("#d").val(pm.d);
        $("#topic").val(pm.topic);

    }

    function win_close() {
        dlg.close(0);
    }

    function do_submit(cb) {
        let t = $("#t").val();
        if (!t) t = "";

        let topic = $("#topic").val();
        if (!topic) {
            cb(false, '<w:g>pls,input,topic</w:g>');
            return;
        }
        let d = $("#d").val();
        cb(true, {id: id, t: t, topic: topic, d: d});
    }

</script>
</html>