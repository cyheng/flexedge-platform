<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,
                 java.io.*,
                 cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.task.*,
                 cn.doraro.flexedge.core.basic.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.dict.*,
                 org.json.*,
                 cn.doraro.flexedge.core.station.*,
                 cn.doraro.flexedge.core.util.xmldata.*,
                 cn.doraro.flexedge.core.util.web.*,
                 cn.doraro.flexedge.core.comp.*" %>
<%!

%><%
    if (!Convert.checkReqEmpty(request, out, "op"))
        return;

    String op = request.getParameter("op");
    String id = request.getParameter("id");
    String tt = request.getParameter("tt");
    String key = request.getParameter("key");

    switch (op) {
        case "set_pstation":
            try {
                if (!Convert.checkReqEmpty(request, out, "id"))
                    return;

                PlatInsManager.getInstance().setStation(id, tt, key);

                out.print("succ");
            } catch (Exception e) {
                //e.printStackTrace();
                out.print(e.getMessage());
            }
            break;
        case "list_pstation":
            List<PStation> pss = PlatInsManager.getInstance().listStations();
            JSONArray jarr = new JSONArray();
            for (PStation ps : pss) {
                JSONObject jo = ps.toJO();
                jarr.put(jo);
            }
            jarr.write(out);
            break;
        case "del_pstation":
            if (!Convert.checkReqEmpty(request, out, "id"))
                return;
            if (PlatInsManager.getInstance().delStation(id) != null) {
                out.print("succ");
                return;
            }
            out.print("delete pstation failed");
            break;
        default:
            out.print("unknown op=" + op);
            return;
    }

%>