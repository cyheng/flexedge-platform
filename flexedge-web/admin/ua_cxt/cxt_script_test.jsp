<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,
                 java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*,
                 cn.doraro.flexedge.*,
                 cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.cxt.*,
                 cn.doraro.flexedge.core.task.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.util.xmldata.*" %>
<%!

%><%
    if (!Convert.checkReqEmpty(request, out, "path"))
        return;
    String op = request.getParameter("op");
    if (op == null)
        op = "";
    String path = request.getParameter("path");
/*
String id = request.getParameter("id");
UARep rep = UAManager.getInstance().getRepById(repid) ;
if(rep==null)
{
	out.print("no rep found");
	return ;
}
*/

    UANode n = UAUtil.findNodeByPath(path);//.findNodeById(id) ;
    if (n == null) {
        out.print("no node found");
        return;
    }

    if (!(n instanceof UANodeOCTagsCxt)) {
        out.print("not node oc tags");
        return;
    }
    UANodeOCTagsCxt cxt = (UANodeOCTagsCxt) n;
    UAPrj prj = (UAPrj) cxt.getTopNode();
    String txt = request.getParameter("txt");
    if (Convert.isNullOrEmpty(txt)) {
        out.print("no txt input");
        return;
    }

//txt = URLDecoder.decode(txt,"UTF-8") ;

    long st = System.currentTimeMillis();
//Object obj = dnd.runCodeForTest(txt);
    UAContext cxtr = null;

    switch (op) {
        case "task":
            if (!Convert.checkReqEmpty(request, out, "taskid"))
                return;
            String taskid = request.getParameter("taskid");
            Task task = TaskManager.getInstance().getTask(prj.getId(), taskid);
            if (task == null) {
                out.print("no task found");
                return;
            }

            try {
                cxtr = task.getContext();
                String ret = cxtr.testScript(txt);
                //out.print(ret) ;
                ret = Convert.plainToJsStr(ret);
                long et = System.currentTimeMillis();
%>{res:"<%=ret %>",cost_ms:<%=(et - st)%>}
<%
        } catch (Exception e) {
            out.print("err:" + e.getMessage());
        }
        return;
    default:
        cxtr = cxt.RT_getContext();
        Object obj = null;
        try {
            //System.out.println("run js code") ;
            obj = cxtr.runCode(txt);
        } catch (Exception e) {
            out.print("err:" + e.getMessage());
            //e.printStackTrace();
            return;
        }

        String res_str = Convert.plainToJsStr("" + obj);
        long et = System.currentTimeMillis();
//System.out.println(sb.toString()) ;
%>{res:"<%=res_str %>",cost_ms:<%=(et - st)%>}
<%
            break;
    }
%>