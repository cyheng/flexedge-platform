<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="
	cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.dict.*,
                 cn.doraro.flexedge.core.store.*,
                 cn.doraro.flexedge.core.store.record.*,
                 cn.doraro.flexedge.core.basic.*,
                 cn.doraro.flexedge.core.util.web.*,
                 cn.doraro.flexedge.core.store.*,
                 java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*" %>
<%@ taglib uri="wb_tag" prefix="w" %>
<%
    if (!Convert.checkReqEmpty(request, out, "prjid"))
        return;

    String prjid = request.getParameter("prjid");
    String id = request.getParameter("id");

    if (id == null)
        id = "";
    String tp = RecProL1DValue.TP;
    String name = "";
    String title = "";
    String chked = "checked";
    String sor_name = "";
    String desc = "";
    RecProL1DValue.ByWay way = RecProL1DValue.ByWay.day;
    StoreManager storem = StoreManager.getInstance(prjid);
    RecManager recm = RecManager.getInstance(prjid);
    RecProL1DValue proDV = null;
    if (Convert.isNotNullEmpty(id)) {
        RecPro pro = recm.getRecProById(id);
        if (pro == null || !(pro instanceof RecProL1DValue)) {
            out.print("no RecProL1DValue found");
            return;
        }
        proDV = (RecProL1DValue) pro;
        name = proDV.getName();
        title = proDV.getTitle();
        if (!proDV.isEnable())
            chked = "";
        sor_name = proDV.getSorName();
        way = proDV.getWay();
        desc = "";//st.getDesc() ;
    }
%>
<html>
<head>
    <title></title>
    <jsp:include page="../head.jsp">
        <jsp:param value="true" name="simple"/>
    </jsp:include>
    <script>
        dlg.resize_to(700, 600);
    </script>
    <style>
    </style>
</head>
<body>
<form class="layui-form" action="">
    <div class="layui-form-item">
        <label class="layui-form-label"><w:g>name</w:g>:</label>
        <div class="layui-input-inline" style="width: 150px;">
            <input type="text" id="name" name="name" value="<%=name%>" autocomplete="off"
                   class="layui-input" <%=Convert.isNotNullEmpty(name)?"readonly":"" %>>
        </div>
        <div class="layui-form-mid"><w:g>title</w:g>:</div>
        <div class="layui-input-inline" style="width: 150px;">
            <input type="text" id="title" name="title" value="<%=title%>" autocomplete="off" class="layui-input">
        </div>
        <div class="layui-form-mid"><w:g>enable</w:g></div>
        <div class="layui-input-inline" style="width: 150px;">
            <input type="checkbox" id="enable" name="enable" <%=chked%> lay-skin="switch" lay-filter="enable"
                   class="layui-input">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label"><w:g>way</w:g></label>
        <div class="layui-input-inline" style="width: 150px;">
            <select id="way" lay-filter="way">
                <%

                    for (RecProL1DValue.ByWay w : RecProL1DValue.ByWay.values()) {

                        int v = w.getVal();
                        String t = w.getTitle();
                %>
                <option value="<%=v %>"><%=t %>
                </option>
                <%
                    }
                %>
            </select>
        </div>

        <div class="layui-form-mid"><w:g>data,sor</w:g>:</div>
        <div class="layui-input-inline" style="width: 150px;">
            <select id="sor_name" lay-filter="sor_name">
                <option value=""><w:g>inner_s</w:g></option>
                <%
                    List<Source> sors = storem.listSources();
                    for (Source sor : sors) {
                        if (!(sor instanceof SourceJDBC))
                            continue;

                        String sorn = sor.getName();
                        String sort = sor.getTitle();
                %>
                <option value="<%=sorn %>"><%=sort %>
                </option>
                <%
                    }
                %>
            </select>
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label"><w:g>desc</w:g>:</label>
        <div class="layui-input-block" style="width: 450px;">
            <textarea id="desc" name="desc" required lay-verify="required" placeholder="" class="layui-textarea"
                      rows="2"><%=desc%></textarea>
        </div>
    </div>
</form>
</body>
<script type="text/javascript">
    var id = "<%=id%>";
    var tp = "<%=tp%>";

    layui.use('form', function () {
        var form = layui.form;
        form.on("select(sor_name)", function (obj) {
            //let dbport = $("#db_port").val() ;
            if (!id) {
                let pdef = $("#drv_name").find("option:selected").attr("jdbc_port_def");
                $("#db_port").val(pdef);
                form.render();
            }

            update_ui();
        });

        $("#sor_name").val("<%=sor_name%>");
        $("#way").val("<%=way.getVal()%>");
        form.render();
    });

    function update_ui() {

    }

    update_ui();

    function win_close() {
        dlg.close(0);
    }

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
        var n = $('#name').val();
        if (n == null || n == '') {
            cb(false, '<w:g>pls,input,name</w:g>');
            return;
        }
        var tt = $('#title').val();
        if (tt == null || tt == '') {
            tt = n;
        }

        var desc = "";//document.getElementById('desc').value;
        if (desc == null)
            desc = '';

        var ben = $("#enable").prop("checked");

        let sor_name = $('#sor_name').val();
        if (!sor_name) {
            //cb(false,"Data source cannot be null") ;
            //return ;
            sor_name = "";
        }
        let way = get_input_val('way', 0, true);

        cb(true, {
            id: id, tp: tp, n: n, t: tt, en: ben, desc: desc, sor: sor_name,
            way: way
        });
        //var dbname=document.getElementById('db_name').value;

        //document.getElementById('form1').submit() ;
    }

</script>
</html>