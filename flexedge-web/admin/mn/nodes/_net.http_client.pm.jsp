<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,
                 java.io.*,
                 org.json.*,
                 cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.basic.*,
                 cn.doraro.flexedge.core.task.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.dict.*,
                 cn.doraro.flexedge.core.comp.*,
                 cn.doraro.flexedge.core.msgnet.*,
                 cn.doraro.flexedge.core.msgnet.nodes.*,
                 cn.doraro.flexedge.core.msgnet.util.*" %>
<%@ taglib uri="wb_tag" prefix="w" %>
<%

%>

<div class="layui-form-item">
    <label class="layui-form-label"><select id="method" class="layui-input" lay-filter="method"
                                            style="width:80px;border-right: 0px;">
        <%
            for (NM_HttpClient.Method m : NM_HttpClient.Method.values()) {
        %>
        <option value="<%=m.name()%>"><%=m.name() %>
        </option>
        <%
            }
        %>
    </select></label>
    <div class="layui-form-mid" style="width:30px;">
        Url
    </div>
    <div class="layui-input-inline" style="width:80px;">
        <select id="url_sty" class="layui-input" lay-filter="url_sty" style="width:80px;border-right: 0px;">
            <%
                for (MNCxtValSty vs : MNCxtValSty.FOR_STR_LIST) {
            %>
            <option value="<%=vs.name()%>"><%=vs.getTitle() %>.</option>
            <%
                }
            %>
        </select>
    </div>
    <div class="layui-input-inline" style="width:350px;">
        <input type="text" id="url_subn" class="layui-input" style="border-left: 0px;left:2px;"/>
    </div>
</div>
<div class="layui-form-item">
    <label class="layui-form-label">Heads:</label>
    <div class="layui-input-inline" style="width:350px;">

    </div>
</div>
<div class="layui-form-item">
    <label class="layui-form-label">Body:</label>
    <div class="layui-input-inline" style="width:150px;">

    </div>
</div>
<div class="layui-form-item">
    <label class="layui-form-label">Response:</label>
    <div class="layui-input-inline" style="width:150px;">
        <select id=resp_fmt class="layui-input" lay-filter="resp_fmt" style="width:80px;border-right: 0px;">
            <%
                for (NM_HttpClient.RespFmt rf : NM_HttpClient.RespFmt.values()) {
            %>
            <option value="<%=rf.name()%>"><%=rf.name() %>
            </option>
            <%
                }
            %>
        </select>
    </div>
</div>
<script>

    function on_after_pm_show(form) {

        update_bt();
    }

    function update_bt() {

    }

    function get_pm_jo() {
        let method = $("#method").val();
        let url_sty = $("#url_sty").val();
        let url_subn = $("#url_subn").val();
        let resp_fmt = $("#resp_fmt").val();
        return {method: method, url_sty: url_sty, url_subn: url_subn, resp_fmt: resp_fmt};
    }

    function set_pm_jo(jo) {
        if (jo.method)
            $("#method").val(jo.method);
        if (jo.url_sty)
            $("#url_sty").val(jo.url_sty);
        if (jo.url_subn)
            $("#url_subn").val(jo.url_subn);
        if (jo.resp_fmt)
            $("#resp_fmt").val(jo.resp_fmt);
    }

    function get_pm_size() {
        return {w: 700, h: 450};
    }

    //on_init_pm_ok() ;
</script>