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
    /*
    String prjid = request.getParameter("prjid");
    String netid = request.getParameter("netid") ;
    String itemid = request.getParameter("itemid") ;

    UAPrj prj = UAManager.getInstance().getPrjById(prjid) ;
    if(prj==null)
    {
        out.print("no prj found") ;
        return ;
    }
    MNManager mnm= MNManager.getInstance(prj) ;
    MNNet net = mnm.getNetById(netid) ;
    if(net==null)
    {
        out.print("no net found") ;
        return ;
    }
    MNBase item =net.getItemById(itemid) ;
    if(item==null)
    {
        out.print("no item found") ;
        return ;
    }

    String prj_path = prj.getNodePath() ;
    MNMsg msg = null;
    if(item instanceof MNNode)
        msg = ((MNNode)item).RT_getLastMsgIn() ;
    if(msg==null)
        msg = new MNMsg() ;
    JSONObject pld = msg.getPayloadJO(null) ;
    LinkedHashMap<String,Object> name2v = new LinkedHashMap<>() ;
    if(pld!=null)
    {
        for(String n:pld.keySet())
        {
            Object obj = pld.get(n) ;
            name2v.put(n,obj) ;
        }
    }*/
%>
<div class="layui-form-item">
    <label class="layui-form-label">Batch Buf Len:</label>
    <div class="layui-input-inline" style="width: 250px;">
        <input type="text" id="batch_w_buflen" name="batch_w_buflen" value="100" autocomplete="off" class="layui-input">
    </div>

</div>

<script>


    function on_after_pm_show(form) {

    }


    function get_pm_jo() {
        let batch_w_buflen = get_input_val('batch_w_buflen', true, 10);

        return {batch_w_buflen: batch_w_buflen};
    }

    function set_pm_jo(jo) {
        $('#batch_w_buflen').val(jo.batch_w_buflen || 100);
    }

    function get_pm_size() {
        return {w: 500, h: 350};
    }

    //on_init_pm_ok() ;
</script>