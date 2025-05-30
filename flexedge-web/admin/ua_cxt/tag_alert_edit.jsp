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
<%@ taglib uri="wb_tag" prefix="wbt" %>
<%!

%><%
    String lang = "en";
%>
<html>
<head>
    <title>Tag Alert Editor </title>
    <script src="/_js/jquery-1.12.0.min.js"></script>
    <link rel="stylesheet" type="text/css" href="/_js/layui/css/layui.css"/>
    <script src="/_js/dlg_layer.js"></script>
    <script src="/_js/layui/layui.all.js"></script>
    <script src="/_js/dlg_layer.js"></script>
    <jsp:include page="../head.jsp">
        <jsp:param value="true" name="simple"/>
    </jsp:include>
    <script>
        dlg.resize_to(650, 500);
    </script>
    <style type="text/css">
        .layui-form-label {
            width: 120px;
        }

        .prompt {
            border: 1px solid;
            margin: 10px;
        }
    </style>
</head>
<body>
<form class="layui-form" action="">
    <div class="layui-form-item" id="dt_tp">
        <label class="layui-form-label"><wbt:g>alert</wbt:g>/<wbt:g>evt</wbt:g> <wbt:g>type</wbt:g></label>
        <div class="layui-input-inline" style="width: 180px;">
            <select id="tp" name="tp" class="layui-input" placeholder="" lay-filter="tp">
                <%
                    boolean bfirst = true;
                    for (ValAlertTp tp : ValAlertTp.ALL) {
                        int tpv = tp.getTpVal();
                        String tpt = tp.getTitle();
                        String param1_t = tp.getParam1Title();
                        String param2_t = tp.getParam2Title();
                        String param3_t = tp.getParam3Title();

                        String trigger_cond = Convert.plainToHtml(tp.getTriggerCond(), false);
                        String release_cond = Convert.plainToHtml(tp.getReleaseCond(), false);

                        String chk = "";
                        if (bfirst) {
                            chk = "selected";
                            bfirst = false;
                        }

                %>
                <option value="<%=tpv%>" <%=chk %> param1_t="<%=param1_t%>" param2_t="<%=param2_t%>"
                        param3_t="<%=param3_t%>"
                        trigger_cond="<%=trigger_cond %>" release_cond="<%=release_cond %>"
                ><%=Convert.plainToHtml(tpt) %>
                </option>
                <%
                    }
                %>
            </select>
        </div>
        <div class="layui-form-mid"><wbt:g>lvl</wbt:g></div>
        <div class="layui-input-inline" style="width: 50px;">
            <select id="lvl" name="lvl" class="layui-input" placeholder="" lay-filter="lvl">
                <%
                    for (int k = 1; k <= 5; k++) {
                %>
                <option value="<%=k%>"><%=k %>
                </option>
                <%
                    }
                %>
            </select>
        </div>
        <div class="layui-form-mid"><wbt:g>enable</wbt:g></div>
        <div class="layui-input-inline" style="width: 100px;">
            <input type="checkbox" id="en" name="en" title="Enable or Not" lay-skin="switch">
        </div>
    </div>

    <div class="layui-card" id="card_scaling">
        <%--
          <div class="layui-form-item">
            <label class="layui-form-label">Alert Group</label>
                <div class="layui-input-inline" style="width: 150px;">
                <input type="number" id="group" name="group" class="layui-input" max="255" min="0" value="0"/>
              </div>
              <div class="layui-form-mid">Alert Level:</div>
              <div class="layui-input-inline" style="width: 150px;">
                <input type="number" id="lvl" name="lvl" class="layui-input" max="200" min="0" value="0"/>
              </div>
          </div>
           --%>
        <div class="layui-form-item">
            <label class="layui-form-label"><wbt:g>name</wbt:g></label>
            <div class="layui-input-inline" style="width: 250px;">
                <input type="text" id="name" name="name" class="layui-input" value=""/>
            </div>
        </div>
        <div class="layui-form-item" id="param1_c">
            <label class="layui-form-label" id="param1_t"><wbt:g>ref_val</wbt:g></label>
            <div class="layui-input-inline" style="width: 250px;" id="param1_d">
                <input type="text" id="param1" name="param1" value="0" class="layui-input"/>
            </div>
        </div>

        <div class="layui-form-item" id="param2_c">
            <label class="layui-form-label param_title" id="param2_t"><wbt:g>trigger_err</wbt:g></label>
            <div class="layui-input-inline" style="width: 250px;" id="param2_d">
                <input type="text" id="param2" name="param2" value="0" class="layui-input"/>
            </div>
        </div>

        <div class="layui-form-item" id="param3_c">
            <label class="layui-form-label param_title" id="param3_t"><wbt:g>relase_err</wbt:g></label>
            <div class="layui-input-inline" style="width: 250px;" id="param3_d">
                <input type="text" id="param3" name="param3" value="0" class="layui-input"/>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label param_title" "><wbt:g>prompt</wbt:g>
            <div class="layui-input-inline" style="width: 250px;">
                <input type="text" id="prompt" name="prompt" value="" class="layui-input"/>
            </div>
        </div>
    </div>
    <div id="tp_ppt" class="prompt">
    </div>
</form>
</body>
<script type="text/javascript">
    var form;
    var ow = dlg.get_opener_w();
    var alertdd = dlg.get_opener_opt("dd");
    var alertid = null;
    var node_path = ow.node_path;
    if (alertdd == null || alertdd == undefined)
        alertdd = {tp: 1, group: 0, lvl: 3, param1: "0", en: true};
    else
        alertid = alertdd.id;
    var path = node_path;

    layui.use('form', function () {
        form = layui.form;

        form.on('select(tp)', function (data) {
            //console.log(data);　
            //var n = data.value;

            update_ui();
        });
        if (alertdd) {
            if (alertdd.en != false)
                $("#en").prop("checked", "checked");
            $("#tp").val(alertdd.tp);
            $("#lvl").val(alertdd.lvl);
            $("#name").val(alertdd.name);
            $("#param1").val(alertdd.param1);
            $("#param2").val(alertdd.param2);
            $("#param3").val(alertdd.param3);
            $("#prompt").val(alertdd.prompt);
        }

        form.render();
    });

    function get_val(n, defv) {
        var v = alertdd[n];
        if (v)
            return v;
        return "" + defv;
    }

    function update_ui() {
        let opt = $('#tp option:selected');
        let pt1 = opt.attr("param1_t");
        let pt2 = opt.attr("param2_t");
        let pt3 = opt.attr("param3_t");
        $("#param1_d").css("display", pt1 ? "" : "none");
        $("#param1_t").html(pt1);
        $("#param2_d").css("display", pt2 ? "" : "none");
        $("#param2_t").html(pt2);
        $("#param3_d").css("display", pt3 ? "" : "none");
        $("#param3_t").html(pt3);


        let tmps = "";
        let trigger_cond = opt.attr("trigger_cond");
        let release_cond = opt.attr("release_cond");
        tmps += "<span style='color:red;'><wbt:g>trigger_cond</wbt:g>:</span>" + convertHTML(trigger_cond) + "<br>";
        tmps += "<span style='color:green;'><wbt:g>release_cond</wbt:g>:</span>" + convertHTML(release_cond);
        $("#tp_ppt").html(tmps);

        form.render();
    }

    function convertHTML(str) {
        var characters = [/&/g, /</g, />/g, /\"/g, /\'/g];
        var entities = ["&amp;", "&lt;", "&gt;", "&quot;", "&apos;"];
        for (var i = 0; i < characters.length; i++) {
            str = str.replace(characters[i], entities[i]);
        }

        return str;
    }

    update_ui();

    function get_input_val(id, defv, bnum) {
        var n = $('#' + id).val();
        if (n == null || n == '') {
            return defv;
        }
        if (bnum)
            return parseInt(n);
        return n;
    }

    function do_submit(cb) {
        var vt = $("#tp").val();
        let ret = {};
        if (alertid)
            ret.id = alertid;
        ret.en = $("#en").prop("checked");
        ret.tp = parseInt($("#tp").val());
        ret.tpt = $("#tp option:selected").text();

        ret.name = $("#name").val();
        if (ret.name && !chk_name(ret.name)) {
            cb(false, "name must a-z A-z first 0-9 first and followed a-z A-z _");
            return;
        }
        ret.lvl = get_input_val("lvl", 3, true);
        ret.param1 = $("#param1").val();
        ret.param2 = $("#param2").val();
        ret.param3 = $("#param3").val();
        ret.prompt = $("#prompt").val();
        cb(true, ret);
        return;
    }

</script>
</html>