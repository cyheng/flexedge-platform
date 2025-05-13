package cn.doraro.flexedge.pro;

public interface IProLic extends IPro {
    String getProUID();

    boolean setSignedStr(String var1);

    String getSignedStr();

    boolean installSignedStr(String var1) throws Exception;

    boolean isSignedOk();

    boolean isPermitted();

    long getTmpLicenseTimeout();

    String getNotPermittedReson();
}
