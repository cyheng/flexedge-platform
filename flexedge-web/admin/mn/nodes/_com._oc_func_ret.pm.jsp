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
    String container_id = request.getParameter("container_id");
    String netid = request.getParameter("netid");
    String itemid = request.getParameter("itemid");

    MNManager mnm = MNManager.getInstanceByContainerId(container_id);
    MNNet net = mnm.getNetById(netid);
    if (net == null) {
        out.print("no net found");
        return;
    }

    OutCallRet ocfr = (OutCallRet) net.getNodeById(itemid);
    if (ocfr == null) {
        out.print("no node item found");
        return;
    }

    List<OutCallFunc> ocfs = ocfr.findAllPrevCallNodes();
%>
<div class="layui-form-item">
    <label class="layui-form-label">Function:</label>
    <div class="layui-input-inline" style="width:150px;">
        <select id="func">
            <option value=""> ---</option>
            <%
                for (OutCallFunc ocf : ocfs) {
                    String fn = ocf.getFuncName();
            %>
            <option value="<%=fn %>"><%=fn %>
            </option>
            <%
                }
            %>
        </select>
    </div>
</div>
<script>

    function get_pm_jo() {
        let func = $("#func").val();
        return {func: func};
    }

    function set_pm_jo(jo) {
        $("#func").val(jo.func || "");
    }

    function get_pm_size() {
        return {w: 600, h: 450};
    }

</script>