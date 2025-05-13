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
                 cn.doraro.flexedge.core.store.*,
                 cn.doraro.flexedge.core.msgnet.*,
                 cn.doraro.flexedge.core.msgnet.util.*,
                 cn.doraro.flexedge.ext.msg_net.*" %>
<%@ taglib uri="wb_tag" prefix="w" %>
<%

%>
<div class="layui-form-item">
    <label class="layui-form-label">Source:</label>
    <div class="layui-input-inline" style="width: 400px;">
        <select id="sor_name">
            <option value=""> ---</option>
            <%
                for (SourceJDBC sorj : StoreManager.listSourcesJDBC()) {
                    String u = sorj.getDBInf();
            %>
            <option value="<%=sorj.getName() %>">[<%=sorj.getTitle() %>] <%=u %>
            </option>
            <%
                }
            %>
        </select>
    </div>
</div>

<script>


    function on_after_pm_show(form) {
        //update_ui();
    }


    function get_pm_jo() {
        let sor_name = $('#sor_name').val();
        if (!sor_name) {
            return '<w:g>pls,select</w:g> Source';
        }

        let js_ob = {};
        js_ob.sor_name = sor_name;
        return js_ob;
    }

    function set_pm_jo(jo) {
        $('#sor_name').val(jo.sor_name || "");
    }

    function get_pm_size() {
        return {w: 600, h: 350};
    }

    //on_init_pm_ok() ;
</script>