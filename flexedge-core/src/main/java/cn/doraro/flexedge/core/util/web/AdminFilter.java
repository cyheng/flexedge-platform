package cn.doraro.flexedge.core.util.web;

import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.ILang;
import cn.doraro.flexedge.core.util.Lan;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author jason.zhu
 */
public class AdminFilter implements Filter, ILang {
    public static final String KEY_AUTH_SESSION_NAME = "access_auth_timelimit";
    static ILogger log = LoggerManager.getLogger(AdminFilter.class);
    static int runJspNum = 0;

    static {
        try {

        } catch (Exception eee) {
            eee.printStackTrace();
        }
    }

    public AdminFilter() {

    }

    public static int getRunJspNum() {
        return runJspNum;
    }

    public void init(FilterConfig config) throws ServletException {
        //Config.appConfigInitSucc = true;
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain fc) throws ServletException, IOException {
        String lan = request.getParameter("_lan_");//req url first
        if (Convert.isNullOrEmpty(lan)) {
            HttpServletRequest req = (HttpServletRequest) request;
            LoginUtil.SessionItem si = LoginUtil.getAdminLoginSession(req.getSession());
            if (si != null)
                lan = si.lan; // then session
        }

        try {
            if (Convert.isNotNullEmpty(lan))
                Lan.setLangInThread(lan);

            synchronized (AdminFilter.class) {
                runJspNum++;
            }

            doFilterInner(request, response, fc);
        } finally {
            if (Convert.isNotNullEmpty(lan))
                Lan.setLangInThread(null);

            synchronized (AdminFilter.class) {
                runJspNum--;
            }
        }
    }

    public void doFilterInner(ServletRequest request, ServletResponse response,
                              FilterChain fc) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String sp = req.getServletPath();
        if (sp != null && sp.startsWith("/login")) {
            fc.doFilter(req, resp);
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("sp=" + sp);
        }

        if (!LoginUtil.checkAdminLogin(req)) {
            if (sp.endsWith("_ajax.jsp")) {
                resp.getWriter().write(g("need_login"));
                return;
            }
            resp.sendRedirect("/admin/login/login.jsp");
            return;
        }

        req.setCharacterEncoding("UTF-8");

//		resp.setHeader( "Pragma", "no-cache" );
//		resp.setHeader( "Cache-Control", "no-cache" );
//		resp.setHeader( "Cache-Control", "no-store" );
//		resp.setDateHeader( "Expires", 0 );


        fc.doFilter(req, resp);
    }

    public void destroy() {

    }
}