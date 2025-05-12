package cn.doraro.flexedge.core.basic;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

public interface IConnEndPoint extends Closeable {
    public InputStream getInputStream();

    public OutputStream getOutputStream();

}
