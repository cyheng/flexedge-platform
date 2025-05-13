

package cn.doraro.flexedge.driver.opc.opcua.server;

import cn.doraro.flexedge.core.Config;
import com.google.common.collect.Lists;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.identity.CompositeValidator;
import org.eclipse.milo.opcua.sdk.server.identity.IdentityValidator;
import org.eclipse.milo.opcua.sdk.server.identity.UsernameIdentityValidator;
import org.eclipse.milo.opcua.sdk.server.identity.X509IdentityValidator;
import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.UaRuntimeException;
import org.eclipse.milo.opcua.stack.core.security.*;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.eclipse.milo.opcua.stack.core.util.CertificateUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedHttpsCertificateBuilder;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;
import org.eclipse.milo.opcua.stack.server.security.DefaultServerCertificateValidator;
import org.eclipse.milo.opcua.stack.server.security.ServerCertificateValidator;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.KeyPair;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DrvServer {
    static final String PRODUCT_URI = "urn:iottree:server";
    private static final int TCP_BIND_PORT = 12686;
    private static final int HTTPS_BIND_PORT = 8443;

    static {
        Security.addProvider((Provider) new BouncyCastleProvider());
    }

    private final OpcUaServer server;
    private final PrjsNamespace prjNS;
    private DrvNamespace drvNamespace;

    public DrvServer() throws Exception {
        final String dirb = Config.getDataDirBase();
        if (dirb == null) {
            throw new RuntimeException("no DataDirBase found");
        }
        final File security_dir = new File(Config.getDataDirBase(), "security");
        if (!security_dir.exists() && !security_dir.mkdirs()) {
            throw new Exception("unable to create security dir: " + security_dir);
        }
        LoggerFactory.getLogger((Class) this.getClass()).info("security dir: {}", (Object) security_dir.getAbsolutePath());
        final KeyStoreLoader loader = new KeyStoreLoader().load(security_dir);
        final DefaultCertificateManager cer_mgr = new DefaultCertificateManager(loader.getServerKeyPair(), loader.getServerCertificateChain());
        final File pki_dir = security_dir.toPath().resolve("pki").toFile();
        final DefaultTrustListManager trust_list_mgr = new DefaultTrustListManager(pki_dir);
        LoggerFactory.getLogger((Class) this.getClass()).info("pki dir: {}", (Object) pki_dir.getAbsolutePath());
        final DefaultServerCertificateValidator cert_validator = new DefaultServerCertificateValidator((TrustListManager) trust_list_mgr);
        final KeyPair https_keypair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
        final SelfSignedHttpsCertificateBuilder https_cer_builder = new SelfSignedHttpsCertificateBuilder(https_keypair);
        https_cer_builder.setCommonName(HostnameUtil.getHostname());
        HostnameUtil.getHostnames("0.0.0.0").forEach(https_cer_builder::addDnsName);
        final X509Certificate https_cer = https_cer_builder.build();
        final UsernameIdentityValidator identity_validator = new UsernameIdentityValidator(true, authChallenge -> {
            final String username = authChallenge.getUsername();
            final String password = authChallenge.getPassword();
            final boolean userok = "user".equals(username) && "password1".equals(password);
            final boolean adminok = "admin".equals(username) && "password2".equals(password);
            return userok || adminok;
        });
        final X509IdentityValidator x509_id_validator = new X509IdentityValidator(c -> true);
        final X509Certificate certificate = (X509Certificate) cer_mgr.getCertificates().stream().findFirst().orElseThrow(() -> new UaRuntimeException(2156462080L, "no certificate found"));
        final String app_uri = CertificateUtil.getSanUri(certificate).orElseThrow(() -> new UaRuntimeException(2156462080L, "certificate is missing the application URI"));
        final Set<EndpointConfiguration> epconfigs = this.createEndpointConfigurations(certificate);
        final OpcUaServerConfig serverConfig = OpcUaServerConfig.builder().setApplicationUri(app_uri).setApplicationName(LocalizedText.english("IOTTree OPC UA Server")).setEndpoints((Set) epconfigs).setBuildInfo(new BuildInfo("urn:iottree:server", "iottree", "iottree opc ua server", OpcUaServer.SDK_VERSION, "", DateTime.now())).setCertificateManager((CertificateManager) cer_mgr).setTrustListManager((TrustListManager) trust_list_mgr).setCertificateValidator((ServerCertificateValidator) cert_validator).setHttpsKeyPair(https_keypair).setHttpsCertificate(https_cer).setIdentityValidator((IdentityValidator) new CompositeValidator(new IdentityValidator[]{(IdentityValidator) identity_validator, (IdentityValidator) x509_id_validator})).setProductUri("urn:iottree:server").build();
        this.server = new OpcUaServer(serverConfig);
        (this.prjNS = new PrjsNamespace(this.server)).startup();
    }

    private static EndpointConfiguration buildTcpEndpoint(final EndpointConfiguration.Builder base) {
        return base.copy().setTransportProfile(TransportProfile.TCP_UASC_UABINARY).setBindPort(12686).build();
    }

    private static EndpointConfiguration buildHttpsEndpoint(final EndpointConfiguration.Builder base) {
        return base.copy().setTransportProfile(TransportProfile.HTTPS_UABINARY).setBindPort(8443).build();
    }

    private Set<EndpointConfiguration> createEndpointConfigurations(final X509Certificate certificate) {
        final Set<EndpointConfiguration> epconfigs = new LinkedHashSet<EndpointConfiguration>();
        final List<String> bind_addrs = Lists.newArrayList();
        bind_addrs.add("0.0.0.0");
        final Set<String> hostnames = new LinkedHashSet<String>();
        hostnames.add(HostnameUtil.getHostname());
        hostnames.addAll(HostnameUtil.getHostnames("0.0.0.0"));
        for (final String bind_addr : bind_addrs) {
            for (final String hostname : hostnames) {
                final EndpointConfiguration.Builder builder = EndpointConfiguration.newBuilder().setBindAddress(bind_addr).setHostname(hostname).setCertificate(certificate).addTokenPolicies(new UserTokenPolicy[]{OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS, OpcUaServerConfig.USER_TOKEN_POLICY_USERNAME, OpcUaServerConfig.USER_TOKEN_POLICY_X509});
                final EndpointConfiguration.Builder noSecurityBuilder = builder.copy().setSecurityPolicy(SecurityPolicy.None).setSecurityMode(MessageSecurityMode.None);
                epconfigs.add(buildTcpEndpoint(noSecurityBuilder));
                epconfigs.add(buildHttpsEndpoint(noSecurityBuilder));
                epconfigs.add(buildTcpEndpoint(builder.copy().setSecurityPolicy(SecurityPolicy.Basic256Sha256).setSecurityMode(MessageSecurityMode.SignAndEncrypt)));
                epconfigs.add(buildHttpsEndpoint(builder.copy().setSecurityPolicy(SecurityPolicy.Basic256Sha256).setSecurityMode(MessageSecurityMode.Sign)));
                final EndpointConfiguration.Builder discoveryBuilder = builder.copy().setPath("/discovery").setSecurityPolicy(SecurityPolicy.None).setSecurityMode(MessageSecurityMode.None);
                epconfigs.add(buildTcpEndpoint(discoveryBuilder));
                epconfigs.add(buildHttpsEndpoint(discoveryBuilder));
            }
        }
        return epconfigs;
    }

    public OpcUaServer getServer() {
        return this.server;
    }

    public CompletableFuture<OpcUaServer> startup() {
        return this.server.startup();
    }

    public CompletableFuture<OpcUaServer> shutdown() {
        this.prjNS.shutdown();
        return this.server.shutdown();
    }
}
