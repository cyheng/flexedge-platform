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
                 cn.doraro.flexedge.core.msgnet.util.*,
                 cn.doraro.flexedge.ext.msg_net.*" %>
<%@ taglib uri="wb_tag" prefix="w" %>
<%

%>
<div class="layui-form-item">
    <label class="layui-form-label"><w:g>topic</w:g>:</label>
    <div class="layui-input-inline" style="width: 250px;">
        <input type="text" id="topic" name="topic" value="" autocomplete="off" class="layui-input">
    </div>

</div>
<div class="layui-form-item">
    <div class="layui-form-label"><w:g>desc</w:g>:</div>
    <div class="layui-input-inline" style="width: 350px;">
        <textarea type="text" id="d" name="d" style="height:60px;" autocomplete="off" class="layui-input"></textarea>
    </div>
</div>
<script>

    function on_after_pm_show(form) {

    }


    function get_pm_jo() {
        var topic = $('#topic').val();
        if (!topic) {
            return '<w:g>pls,input,topic</w:g>';
        }

        return {topic: topic};
    }

    function set_pm_jo(jo) {
        $('#topic').val(jo.topic || "");
    }

    function get_pm_size() {
        return {w: 500, h: 350};
    }

    //on_init_pm_ok() ;
</script>