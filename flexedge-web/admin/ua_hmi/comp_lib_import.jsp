<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="
	cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.comp.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.basic.*,
                 java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*" %>
<%@ taglib uri="wb_tag" prefix="wbt" %>
<%
    if (!Convert.checkReqEmpty(request, out, "tmpfn"))
        return;
    String tmpfn = request.getParameter("tmpfn");
    File tmpf = new File(Config.getDataTmpDir(), tmpfn);
    if (!tmpf.exists()) {
        out.print("no upload file found");
        return;
    }

    CompManager devmgr = CompManager.getInstance();
    HashMap<String, String> pms = devmgr.parseCompLibZipFileMeta(tmpf);
    if (pms == null) {
        out.print("no invalid HMI Comp file");
        return;
    }

    String tp = pms.get("tp");
    String libid = pms.get("libid");
    String libtitle = pms.get("libtitle");

    if (!"complib".equals(tp) || Convert.isNullOrEmpty(libid)) {
        out.print("invlid import file!");
        tmpf.delete();
        return;
    }
//	DevDriver dd = devmgr.getDriver(drvname) ;
//	if(dd==null)
//	{
//		out.print("no Driver found") ;
//		return ;
//	}

    CompLib complib = devmgr.getCompLibById(libid);

%>
<html>
<head>
    <title>HMI Comp Lib importer</title>
    <jsp:include page="../head.jsp"></jsp:include>
    <style type="text/css">
        .imp_item {
            position: relative;
            width: 90%;
            margin: 10px;

            height: 85px;
        }
    </style>
    <script>
        dlg.resize_to(600, 400);
    </script>
</head>
<body>
<%
    String bgcolor = "#8dd35f";
    String prompt = "new_lib_imp";
    boolean b_can_imp = true;
    if (complib != null) {
        bgcolor = "#ff8080";
        prompt = "lib_al_existed";
    }

%>

<div class="imp_item" style="background-color: <%=bgcolor%>">
    <table style="height:100%;width:100%">
        <tr>
            <td colspan="3" align="center"><h3><wbt:g><%=prompt %>
            </wbt:g></h3></td>
        </tr>
        <tr>
            <td valign="middle">

            </td>
            <td>
                <wbt:g>title</wbt:g>:<input type="text" id="inputt" value="<%=libtitle %>"/>
            </td>
            <td></td>
        </tr>
        <tr>
            <td colspan="3" align="center">
                <input type="radio" id="" name="radio_op" value="ignore"><wbt:g>do_not_imp</wbt:g>
                <%
                    if (complib != null) {
                %>
                <input type="radio" id="" name="radio_op" value="replace"><wbt:g>replace</wbt:g>
                <%
                    }

                %><input type="radio" id="" name="radio_op" value="new" checked="checked"><wbt:g>create_new</wbt:g>
            </td>
        </tr>
        <tr>
    </table>

</div>
</body>
<script type="text/javascript">
    var tmpfn = "<%=tmpfn%>";
    var libid = "<%=libid%>";

    function win_close() {
        dlg.close(0);
    }


    function do_submit(cb) {
        var pms = {tmpfn: tmpfn};

        var op = $("input[name='radio_op']:checked").val();
        pms["op"] = op;
        pms["tmpfn"] = tmpfn;
        pms["libid"] = libid;
        var tt = $("#inputt").val();
        if (tt == null || tt == "") {
            dlg.mdg("<wbt:g>pls,input,title</wbt:g>");
            return;
        }
        pms["libtitle"] = tt;

        //console.log(pms);

        send_ajax('comp_lib_imp_ajax.jsp', pms, function (bsucc, ret) {
            if (!bsucc || ret.indexOf('succ') < 0) {
                cb(false, ret);
                return;
            }
            cb(true, ret);
        }, false);

        //document.getElementById('form1').submit() ;
    }
</script>
</html>