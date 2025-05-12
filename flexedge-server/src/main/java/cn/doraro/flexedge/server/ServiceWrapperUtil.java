// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.server;

import java.io.FileOutputStream;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.FilenameFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

public class ServiceWrapperUtil
{
    public static String readFileTxt(final File f, final String encod) throws IOException {
        final byte[] bs = readFileBuf(f);
        if (encod == null || encod.equals("")) {
            return new String(bs);
        }
        return new String(bs, encod);
    }
    
    public static byte[] readFileBuf(final File f) throws IOException {
        if (f.length() > 10485760L) {
            throw new RuntimeException("file is too long");
        }
        Throwable t = null;
        try {
            final FileInputStream fis = new FileInputStream(f);
            try {
                final int size = fis.available();
                final byte[] buffer = new byte[size];
                fis.read(buffer);
                return buffer;
            }
            finally {
                if (fis != null) {
                    fis.close();
                }
            }
        }
        finally {
            if (t == null) {
                final Throwable exception;
                t = exception;
            }
            else {
                final Throwable exception;
                if (t != exception) {
                    t.addSuppressed(exception);
                }
            }
        }
    }
    
    private static String getJarCPInDir(final String prefix, final File dirf) {
        final String[] fns = dirf.list(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.toLowerCase().endsWith(".jar");
            }
        });
        if (fns == null || fns.length <= 0) {
            return "";
        }
        String ret = String.valueOf(prefix) + fns[0];
        for (int i = 1; i < fns.length; ++i) {
            ret = String.valueOf(ret) + ";" + prefix + fns[i];
        }
        return ret;
    }
    
    private static String getClasspathStr() {
        final File libdir = new File("./lib/");
        final File tlibdir = new File("./tomcat/lib/");
        String ret = getJarCPInDir("./lib/", libdir);
        ret = String.valueOf(ret) + ";" + getJarCPInDir("./tomcat/lib/", tlibdir);
        ret = String.valueOf(ret) + ";./tomcat/bin/bootstrap.jar;./tomcat/bin/tomcat-juli.jar";
        return ret;
    }
    
    private static void setupWrapperClasspath() throws IOException {
        final File conf_sor = new File("./wrapper.sor.conf");
        final File conff = new File("./wrapper.conf");
        final String txt = readFileTxt(conf_sor, "utf-8");
        final BufferedReader br = new BufferedReader(new StringReader(txt));
        Throwable t = null;
        try {
            final FileOutputStream fos = new FileOutputStream(conff);
            try {
                String ln = null;
                while ((ln = br.readLine()) != null) {
                    if (ln.startsWith("wrapper.java.command=")) {
                        File jdkdir = new File("./jdk/");
                        if (jdkdir.exists()) {
                            ln = "wrapper.java.command=.\\jdk\\bin\\java";
                        }
                        else {
                            jdkdir = new File("./jre/");
                            if (jdkdir.exists()) {
                                ln = "wrapper.java.command=.\\jre\\bin\\java";
                            }
                            else {
                                jdkdir = new File("./jre8_x86/");
                                if (jdkdir.exists()) {
                                    ln = "wrapper.java.command=.\\jre8_x86\\bin\\java";
                                }
                            }
                        }
                    }
                    if (ln.startsWith("wrapper.java.classpath.2=")) {
                        ln = "wrapper.java.classpath.2=" + getClasspathStr();
                    }
                    fos.write(ln.getBytes("UTF-8"));
                    fos.write("\r\n".getBytes());
                }
            }
            finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }
        finally {
            if (t == null) {
                final Throwable exception;
                t = exception;
            }
            else {
                final Throwable exception;
                if (t != exception) {
                    t.addSuppressed(exception);
                }
            }
        }
    }
    
    public static void main(final String[] args) throws IOException {
        setupWrapperClasspath();
        System.out.println("wrapper util setup ok");
    }
}
