// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.opc.opcua.server;

import java.security.PublicKey;
import java.util.Iterator;
import java.util.function.Function;
import java.util.Arrays;
import java.security.PrivateKey;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.security.cert.Certificate;
import java.security.Key;
import java.util.Set;
import com.google.common.collect.Sets;
import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import java.util.UUID;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import java.io.InputStream;
import java.security.KeyStore;
import java.io.File;
import org.slf4j.LoggerFactory;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import org.slf4j.Logger;
import java.util.regex.Pattern;

class KeyStoreLoader
{
    private static final Pattern IP_ADDR_PATTERN;
    private static final String SERVER_ALIAS = "server-ai";
    private static final char[] PASSWORD;
    private final Logger logger;
    private X509Certificate[] serverCerChain;
    private X509Certificate serverCer;
    private KeyPair serverKeyPair;
    
    static {
        IP_ADDR_PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        PASSWORD = "password".toCharArray();
    }
    
    KeyStoreLoader() {
        this.logger = LoggerFactory.getLogger((Class)this.getClass());
    }
    
    KeyStoreLoader load(final File baseDir) throws Exception {
        final KeyStore keystore = KeyStore.getInstance("PKCS12");
        final File server_keystore = baseDir.toPath().resolve("iottree-server.pfx").toFile();
        this.logger.info("Loading KeyStore at {}", (Object)server_keystore);
        if (!server_keystore.exists()) {
            keystore.load(null, KeyStoreLoader.PASSWORD);
            final KeyPair keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
            final String app_uri = "urn:iottree:opcua:server:" + UUID.randomUUID();
            final SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(keyPair).setCommonName("IOTTree OPCUA Server").setOrganization("iottree").setOrganizationalUnit("dev").setLocalityName("Beijing").setStateName("CA").setCountryCode("CN").setApplicationUri(app_uri);
            final Set<String> hostnames = (Set<String>)Sets.union((Set)Sets.newHashSet((Object[])new String[] { HostnameUtil.getHostname() }), HostnameUtil.getHostnames("0.0.0.0", false));
            for (final String hostname : hostnames) {
                if (KeyStoreLoader.IP_ADDR_PATTERN.matcher(hostname).matches()) {
                    builder.addIpAddress(hostname);
                }
                else {
                    builder.addDnsName(hostname);
                }
            }
            final X509Certificate certificate = builder.build();
            keystore.setKeyEntry("server-ai", keyPair.getPrivate(), KeyStoreLoader.PASSWORD, new X509Certificate[] { certificate });
            keystore.store(new FileOutputStream(server_keystore), KeyStoreLoader.PASSWORD);
        }
        else {
            keystore.load(new FileInputStream(server_keystore), KeyStoreLoader.PASSWORD);
        }
        final Key serverPrivateKey = keystore.getKey("server-ai", KeyStoreLoader.PASSWORD);
        if (serverPrivateKey instanceof PrivateKey) {
            this.serverCer = (X509Certificate)keystore.getCertificate("server-ai");
            this.serverCerChain = Arrays.stream(keystore.getCertificateChain("server-ai")).map((Function<? super Certificate, ?>)X509Certificate.class::cast).toArray(X509Certificate[]::new);
            final PublicKey serverPublicKey = this.serverCer.getPublicKey();
            this.serverKeyPair = new KeyPair(serverPublicKey, (PrivateKey)serverPrivateKey);
        }
        return this;
    }
    
    X509Certificate getServerCertificate() {
        return this.serverCer;
    }
    
    public X509Certificate[] getServerCertificateChain() {
        return this.serverCerChain;
    }
    
    KeyPair getServerKeyPair() {
        return this.serverKeyPair;
    }
}
