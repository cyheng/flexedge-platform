package cn.doraro.flexedge.core.ws;

import javax.servlet.http.HttpSession;

public interface IWSRight {
    public boolean checkWSRight(HttpSession session);
}
