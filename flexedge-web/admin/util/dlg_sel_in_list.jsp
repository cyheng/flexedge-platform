<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="
	cn.doraro.flexedge.core.*,
				 cn.doraro.flexedge.core.conn.*,
				 cn.doraro.flexedge.core.util.*,
				 cn.doraro.flexedge.core.basic.*,
				 java.io.*,
				 java.util.*,
				 java.net.*,
				 java.util.*" %>
<%
    String txt_title = request.getParameter("txt_title");
    if (Convert.isNullOrEmpty(txt_title))
        txt_title = "Text";
    String opener_list_id = request.getParameter("opener_list_id");
    if (Convert.isNullOrEmpty(opener_list_id))
        opener_list_id = "";

    //String opener_seled_id = request.getParameter("opener_seled_id") ;
    //if(Convert.isNullOrEmpty(opener_seled_id))
    //	opener_seled_id ="" ;

    String v = request.getParameter("v");
    if (Convert.isNullOrEmpty(v))
        v = "";
    boolean multi = "true".equalsIgnoreCase(request.getParameter("multi"));
%>
<html>
<head>
    <title></title>
    <jsp:include page="../head.jsp"></jsp:include>
</head>
<script type="text/javascript">
    dlg.resize_to(400, 500);
</script>
<body>
<table id="item_list" border="0" class="list_table" cellspacing="0" cellpadding="0" border="0">

</table>
</body>
<script type="text/javascript">
    var form = null;
    var opener_list_id = "<%=opener_list_id%>";

    var ow = dlg.get_opener_w();

    var list_ob = null;
    var seled_ids = []
    if (opener_list_id) {
        list_ob = ow[opener_list_id];
    }


    function get_select() {
        var ret = [];
        $('input:checkbox:checked').each(function (i) {
            ret.push($(this).attr("value"));
        });
        return ret;
    }


    function show_list() {
        //console.log(list_ob);
        var htmlstr = "";
        if (list_ob) {
            for (var i = 0; i < list_ob.length; i++) {
                var id = list_ob[i].id;
                var tt = list_ob[i].title;
                var seled = list_ob[i].sel;

                htmlstr += "<tr class=\"list_tr\" id=\"" + id + "\" >"
                    + "<td nowrap=\"nowrap\">&nbsp;<input id=\"sel_mid_" + id + "\" type=\"checkbox\" " + (seled ? "checked=\"checked\"" : "") + " value=\"" + id + "\"/></td>"
                    + "<td width=\"10\">&nbsp;</td>"
                    + "<td width=\"95%\"  onclick=\"sel_item(\'" + id + "\')\">" + tt + "</td>"
                    + "</tr>"
                    + "<tr><td height=\"1\" colspan=\"8\" bgcolor=\"#EAE9E1\"></td></tr>";
            }
        }

        document.getElementById("item_list").innerHTML = htmlstr;
    }

    show_list();

    function sel_item(id) {
        var chked = $("#sel_mid_" + id).prop("checked");
        $("#sel_mid_" + id).prop("checked", !chked)
    }
</script>
</html>