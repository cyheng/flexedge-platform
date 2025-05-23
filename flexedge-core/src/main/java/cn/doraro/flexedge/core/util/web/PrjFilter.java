package cn.doraro.flexedge.core.util.web;

import cn.doraro.flexedge.core.*;
import cn.doraro.flexedge.core.plugin.PlugAuth;
import cn.doraro.flexedge.core.plugin.PlugManager;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import org.json.JSONObject;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;

public class PrjFilter implements Filter {
    private static final String METHOD_DELETE = "DELETE";
    private static final String METHOD_HEAD = "HEAD";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_TRACE = "TRACE";
    private static ILogger log = LoggerManager.getLogger(PrjFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    private boolean checkRestfulApiRight(HttpServletRequest req, UANode node, UAPrj prj) {

        PrjRestful restful = prj.getEnabledRestfulToken();
        if (restful == null)
            return true;

        try {
            return restful.checkRequest(req);
        } catch (Exception ee) {
            ee.printStackTrace();
            return false;
        }
    }

    private boolean isWriteTagCutoff(UAPrj prj) {
        return prj.getOrDefaultPropValueBool("prj_restful", "wtag_cutoff", false);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession();
        //this.getServletContext()..getRequestDispatcher(getServletInfo())
        response.setContentType("text/html;charset=UTF-8");
        String uri = req.getRequestURI();
        String qs = req.getQueryString();
        //System.out.println("uri="+uri +"  qs="+qs);
        if (uri.startsWith("/_ws")) {
            session.setAttribute("ClientIP", req.getRemoteAddr());
            chain.doFilter(request, response);
            return;
        }
        if (uri.startsWith("/flexedge"))
            uri = uri.substring(8);

        if (uri.endsWith(".jsp")) {
//			String u = uri ;
//			if(qs!=null)
//				u += "?"+qs ;
            //req.getRequestDispatcher(u).forward(req, resp);
            chain.doFilter(request, response);
            return;
        }

        if (uri.contentEquals("/")) {
            chain.doFilter(request, response);
            return;
        }

        if (uri.startsWith("/_res")) {//res_node_id="+resnodeid+"&name="+name
            List<String> ss = Convert.splitStrWith(uri.substring(5), "/");
            if (ss.size() <= 1)
                return;
            String tmpu = "/res.jsp?resnodeid=" + ss.get(0) + "&name=" + ss.get(1);
            if (qs != null)
                tmpu += "?" + qs;
            req.getRequestDispatcher(tmpu).forward(req, resp);
            return;
        }

        if (uri.startsWith("/_open")) {//res_node_id="+resnodeid+"&name="+name
            List<String> ss = Convert.splitStrWith(uri.substring(6), "/");
            if (ss.size() <= 1)
                return;
            String tmpu = "/open.jsp?resnodeid=" + ss.get(0) + "&name=" + ss.get(1);
            if (qs != null)
                tmpu += "?" + qs;
            req.getRequestDispatcher(tmpu).forward(req, resp);
            return;
        }

        UANode node = UAUtil.findNodeByPath(uri);
        if (node == null) {
            return;
        }

        //check right
        PlugAuth pa = PlugManager.getInstance().getPlugAuth();
        if (pa != null) {
            try {
                if (!pa.checkReadRight(node.getNodePath(), req)) {//no right
                    resp.getWriter().write(pa.getNoReadRightPrompt());
                    return;
                }
            } catch (Exception e) {
                //e.printStackTrace();
                PrintWriter w = resp.getWriter();
                w.write("check read right exception:");
                e.printStackTrace(w);
                //w.write(e.getMessage());
                return;
            }
        }

        if (node instanceof UAHmi) {
            //UAHmi hmi = (UAHmi)node ;
            if (qs == null)
                qs = "";
            else
                qs = "&" + qs;
            req.getRequestDispatcher("/hmi.jsp?path=" + uri + qs).forward(req, resp);
            return;
        }

        //restful api
        UANode topn = node.getTopNode();
        if (topn == null || !(topn instanceof UAPrj))
            return;

        UAPrj prj = (UAPrj) topn;

        if (!checkRestfulApiRight(req, node, prj)) {
            JSONObject jo = new JSONObject();
            jo.put("result", false);
            jo.put("err", "no right to access restful api");
            jo.write(resp.getWriter());
            return;
        }

        String op = req.getParameter("op");
        String tp = req.getParameter("tp");
        if (tp == null)
            tp = "";
        if (op == null)
            op = "";
        String method = req.getMethod();

        if (node instanceof UATag) {
            if (method.equals(METHOD_POST) || method.equals(METHOD_PUT)) {//tag write
                if (pa != null) {
                    try {
                        if (!pa.checkWriteRight(node.getNodePath(), req)) {//no right
                            JSONObject jo = new JSONObject();
                            jo.put("result", false);
                            jo.put("err", pa.getNoWriteRightPrompt());
                            jo.write(resp.getWriter()); //.write(pa.getNoWriteRightPrompt());

                            if (log.isTraceEnabled())
                                log.trace("PlugAuth [" + pa.getClass().getCanonicalName() + "] checkWriteRight Tag=[" + node.getNodePath() + "] failed");

                            return;
                        }
                    } catch (Exception e) {
                        //w.write("check write right exception:");
                        JSONObject jo = new JSONObject();
                        jo.put("result", false);
                        jo.put("err", "check write right exception:" + e.getMessage());
                        jo.write(resp.getWriter());

                        if (log.isDebugEnabled())
                            log.debug(e);
                        return;
                    }
                }

                StringBuilder failedr = new StringBuilder();
                boolean bres = doPut(req, resp, (UATag) node, prj, failedr);
                if (bres)
                    resp.getWriter().write("{\"result\":true}");
                else
                    resp.getWriter().write("{\"result\":false,\"err\":\"" + failedr.toString() + "\"}");
                return;
            }
        }


        switch (op) {
            case "cxt":
                req.getRequestDispatcher("/node_cxt.jsp?path=" + uri + "&tp=" + tp).forward(req, resp);
                break;
            case "list":
                req.getRequestDispatcher("/node_list.jsp?path=" + uri + "&tp=" + tp).forward(req, resp);
                break;
            case "ui":
                if (node instanceof UAPrj) {
                    prj = (UAPrj) node;
                    UAHmi tmphmi = prj.getHmiMain();
                    if (tmphmi == null) {
                        List<UAHmi> hmis = prj.getHmis();
                        if (hmis == null || hmis.size() <= 0)
                            return;
                        tmphmi = hmis.get(0);
                    }
                    req.getRequestDispatcher("/hmi.jsp?path=" + tmphmi.getNodePath()).forward(req, resp);
                    return;
                }
                break;
            case "nav":
                if (node instanceof UAPrj) {
                    req.getRequestDispatcher("/hmi_nav.jsp?path=" + node.getNodePath() + "&tp=" + tp).forward(req, resp);
                } else {
                    resp.getWriter().write("not prj node");
                }
                break;
            default:
                req.getRequestDispatcher("/node_cxt.jsp?path=" + uri + "&tp=" + tp).forward(req, resp);
                break;
        }

        return;

    }


    protected boolean doPut(HttpServletRequest req, HttpServletResponse resp, UATag tag, UAPrj prj, StringBuilder failedr) throws ServletException, IOException {
        //super.doPut(req, resp);
        //update restful api
        resp.setContentType("text/html;charset=UTF-8");
        String uri = req.getRequestURI();
        //String qs = req.getQueryString();

        if (uri.startsWith("/flexedge"))
            uri = uri.substring(8);

        UANode node = UAUtil.findNodeByPath(uri);
        if (node == null)
            return false;

        if (!(node instanceof UATag))
            return false;

        boolean cutoff = isWriteTagCutoff(prj);

        try {
            //String pv0 = req.getParameter("_pv") ;
            for (Enumeration<String> ens = req.getParameterNames(); ens.hasMoreElements(); ) {
                String pn = ens.nextElement();
                String pv = req.getParameter(pn);
                if (cutoff) {
                    log.warn("cut off write tag [" + tag.getNodePath() + "] with " + pn + "=" + pv);
                } else {
                    if (log.isDebugEnabled())
                        log.debug("write tag [" + tag.getNodePath() + "] with " + pn + "=" + pv);
                    tag.JS_set(pn, pv);
                }
            }

            return true;
        } catch (Exception ee) {
            if (log.isDebugEnabled())
                log.debug(ee);

            failedr.append(ee.getMessage());
            return false;
        }

    }

    @Override
    public void destroy() {

    }

}
