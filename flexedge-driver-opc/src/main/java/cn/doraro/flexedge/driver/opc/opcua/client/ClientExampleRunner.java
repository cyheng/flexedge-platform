// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.opc.opcua.client;

import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import java.util.Optional;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import org.eclipse.milo.opcua.stack.core.Stack;
import java.nio.file.Path;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import java.util.function.Predicate;
import java.nio.file.LinkOption;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.Paths;
import org.slf4j.LoggerFactory;
import java.security.Provider;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;

public class ClientExampleRunner
{
    private final Logger logger;
    private final CompletableFuture<OpcUaClient> future;
    private final ClientExample clientExample;
    private final boolean serverRequired;
    
    static {
        Security.addProvider((Provider)new BouncyCastleProvider());
    }
    
    public ClientExampleRunner(final ClientExample clientExample) throws Exception {
        this(clientExample, true);
    }
    
    public ClientExampleRunner(final ClientExample clientExample, final boolean serverRequired) throws Exception {
        this.logger = LoggerFactory.getLogger((Class)this.getClass());
        this.future = new CompletableFuture<OpcUaClient>();
        this.clientExample = clientExample;
        this.serverRequired = serverRequired;
    }
    
    private OpcUaClient createClient() throws Exception {
        final Path securityTempDir = Paths.get(System.getProperty("java.io.tmpdir"), "security");
        Files.createDirectories(securityTempDir, (FileAttribute<?>[])new FileAttribute[0]);
        if (!Files.exists(securityTempDir, new LinkOption[0])) {
            throw new Exception("unable to create security dir: " + securityTempDir);
        }
        LoggerFactory.getLogger((Class)this.getClass()).info("security temp dir: {}", (Object)securityTempDir.toAbsolutePath());
        final KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);
        return OpcUaClient.create(this.clientExample.getEndpointUrl(), endpoints -> endpoints.stream().filter(this.clientExample.endpointFilter()).findFirst(), configBuilder -> configBuilder.setApplicationName(LocalizedText.english("eclipse milo opc-ua client")).setApplicationUri("urn:eclipse:milo:examples:client").setCertificate(keyStoreLoader.getClientCertificate()).setKeyPair(keyStoreLoader.getClientKeyPair()).setIdentityProvider(this.clientExample.getIdentityProvider()).setRequestTimeout(Unsigned.uint(5000)).build());
    }
    
    public void run() {
        try {
            final OpcUaClient client = this.createClient();
            this.future.whenCompleteAsync((c, ex) -> {
                if (ex != null) {
                    this.logger.error("Error running example: {}", (Object)ex.getMessage(), (Object)ex);
                }
                try {
                    opcUaClient.disconnect().get();
                    Stack.releaseSharedResources();
                }
                catch (final InterruptedException | ExecutionException e3) {
                    this.logger.error("Error disconnecting: {}", (Object)e3.getMessage(), (Object)e3);
                }
                try {
                    Thread.sleep(1000L);
                    System.exit(0);
                }
                catch (final InterruptedException e4) {
                    e4.printStackTrace();
                }
                return;
            });
            try {
                this.clientExample.run(client, this.future);
                this.future.get(15L, TimeUnit.SECONDS);
            }
            catch (final Throwable t) {
                this.logger.error("Error running client example: {}", (Object)t.getMessage(), (Object)t);
                this.future.completeExceptionally(t);
            }
        }
        catch (final Throwable t2) {
            this.logger.error("Error getting client: {}", (Object)t2.getMessage(), (Object)t2);
            this.future.completeExceptionally(t2);
            try {
                Thread.sleep(1000L);
                System.exit(0);
            }
            catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(999999999L);
        }
        catch (final InterruptedException e2) {
            e2.printStackTrace();
        }
    }
}
