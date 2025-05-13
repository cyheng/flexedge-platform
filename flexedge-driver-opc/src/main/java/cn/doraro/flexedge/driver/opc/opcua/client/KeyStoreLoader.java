

package cn.doraro.flexedge.driver.opc.opcua.client;

import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.regex.Pattern;

public class KeyStoreLoader {
    private static final Pattern IP_ADDR_PATTERN;
    private static final String CLIENT_ALIAS = "client-ai";
    private static final char[] PASSWORD;

    static {
        IP_ADDR_PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        PASSWORD = "password".toCharArray();
    }

    private final Logger logger;
    private X509Certificate clientCertificate;
    private KeyPair clientKeyPair;

    public KeyStoreLoader() {
        this.logger = LoggerFactory.getLogger((Class) this.getClass());
    }

    KeyStoreLoader load(final Path baseDir) throws Exception {
        final KeyStore keyStore = KeyStore.getInstance("PKCS12");
        final Path serverKeyStore = baseDir.resolve("iottree-client.pfx");
        this.logger.info("Loading KeyStore at {}", (Object) serverKeyStore);
        if (!Files.exists(serverKeyStore, new LinkOption[0])) {
            keyStore.load(null, KeyStoreLoader.PASSWORD);
            final KeyPair keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
            final SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(keyPair).setCommonName("IOTTree OPCUA Client").setOrganization("iottree").setOrganizationalUnit("dev").setLocalityName("Beijing").setStateName("CA").setCountryCode("CN").setApplicationUri("urn:eclipse:milo:examples:client").addDnsName("localhost").addIpAddress("127.0.0.1");
            for (final String hostname : HostnameUtil.getHostnames("0.0.0.0")) {
                if (KeyStoreLoader.IP_ADDR_PATTERN.matcher(hostname).matches()) {
                    builder.addIpAddress(hostname);
                } else {
                    builder.addDnsName(hostname);
                }
            }
            final X509Certificate certificate = builder.build();
            keyStore.setKeyEntry("client-ai", keyPair.getPrivate(), KeyStoreLoader.PASSWORD, new X509Certificate[]{certificate});
            Throwable t = null;
            try {
                final OutputStream out = Files.newOutputStream(serverKeyStore, new OpenOption[0]);
                try {
                    keyStore.store(out, KeyStoreLoader.PASSWORD);
                } finally {
                    if (out != null) {
                        out.close();
                    }
                }
            } finally {
                if (t == null) {
                    final Throwable exception;
                    t = exception;
                } else {
                    final Throwable exception;
                    if (t != exception) {
                        t.addSuppressed(exception);
                    }
                }
            }
        }
        Throwable t2 = null;
        try {
            final InputStream in = Files.newInputStream(serverKeyStore, new OpenOption[0]);
            try {
                keyStore.load(in, KeyStoreLoader.PASSWORD);
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        } finally {
            if (t2 == null) {
                final Throwable exception2;
                t2 = exception2;
            } else {
                final Throwable exception2;
                if (t2 != exception2) {
                    t2.addSuppressed(exception2);
                }
            }
        }
        final Key serverPrivateKey = keyStore.getKey("client-ai", KeyStoreLoader.PASSWORD);
        if (serverPrivateKey instanceof PrivateKey) {
            this.clientCertificate = (X509Certificate) keyStore.getCertificate("client-ai");
            final PublicKey serverPublicKey = this.clientCertificate.getPublicKey();
            this.clientKeyPair = new KeyPair(serverPublicKey, (PrivateKey) serverPrivateKey);
        }
        return this;
    }

    X509Certificate getClientCertificate() {
        return this.clientCertificate;
    }

    KeyPair getClientKeyPair() {
        return this.clientKeyPair;
    }
}
