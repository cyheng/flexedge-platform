package cn.doraro.flexedge.pro;

import java.lang.reflect.Method;

public class ProInter implements IProLic {
    Object proOb = null;
    Class<?> proCl = null;

    public ProInter(Object ob) {
        this.proOb = ob;
        this.proCl = ob.getClass();
    }

    public String getProName() {
        try {
            Method m = this.proCl.getDeclaredMethod("getProName");
            return (String)m.invoke(this.proOb);
        } catch (Exception var2) {
            return null;
        }
    }

    public String getProUID() {
        try {
            Method m = this.proCl.getDeclaredMethod("getProUID");
            return (String)m.invoke(this.proOb);
        } catch (Exception var2) {
            return null;
        }
    }

    public boolean setSignedStr(String signed_str) {
        try {
            Method m = this.proCl.getDeclaredMethod("setSignedStr", String.class);
            return (Boolean)m.invoke(this.proOb, signed_str);
        } catch (Exception var3) {
            return false;
        }
    }

    public String getSignedStr() {
        try {
            Method m = this.proCl.getDeclaredMethod("getSignedStr");
            return (String)m.invoke(this.proOb);
        } catch (Exception var2) {
            return null;
        }
    }

    public boolean installSignedStr(String signed_str) throws Exception {
        try {
            Method m = this.proCl.getDeclaredMethod("installSignedStr", String.class);
            return (Boolean)m.invoke(this.proOb, signed_str);
        } catch (Exception var3) {
            return false;
        }
    }

    public boolean isSignedOk() {
        try {
            Method m = this.proCl.getDeclaredMethod("isSignedOk");
            return (Boolean)m.invoke(this.proOb);
        } catch (Exception var2) {
            return false;
        }
    }

    public boolean isPermitted() {
        try {
            Method m = this.proCl.getDeclaredMethod("isPermitted");
            return (Boolean)m.invoke(this.proOb);
        } catch (Exception var2) {
            var2.printStackTrace();
            return false;
        }
    }

    public long getTmpLicenseTimeout() {
        try {
            Method m = this.proCl.getDeclaredMethod("getTmpLicenseTimeout");
            return (Long)m.invoke(this.proOb);
        } catch (Exception var2) {
            return -1L;
        }
    }

    public String getNotPermittedReson() {
        try {
            Method m = this.proCl.getDeclaredMethod("getNotPermittedReson");
            return (String)m.invoke(this.proOb);
        } catch (Exception var2) {
            return var2.getMessage();
        }
    }
}
