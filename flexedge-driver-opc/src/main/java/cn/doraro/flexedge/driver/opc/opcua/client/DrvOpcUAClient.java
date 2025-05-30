

package cn.doraro.flexedge.driver.opc.opcua.client;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.security.Provider;
import java.security.Security;
import java.util.concurrent.ExecutionException;

public class DrvOpcUAClient {
    static {
        Security.addProvider((Provider) new BouncyCastleProvider());
    }

    private static void test1() {
    }

    public static void main(final String[] args) throws InterruptedException, ExecutionException {
    }

    private OpcUaClient createClient(final String opcurl) throws Exception {
        final Path securityTempDir = Paths.get(System.getProperty("java.io.tmpdir"), "security");
        Files.createDirectories(securityTempDir, (FileAttribute<?>[]) new FileAttribute[0]);
        if (!Files.exists(securityTempDir, new LinkOption[0])) {
            throw new Exception("unable to create security dir: " + securityTempDir);
        }
        final KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);
        return OpcUaClient.create(opcurl, endpoints -> endpoints.stream().filter(e -> 1).findFirst(), configBuilder -> configBuilder.setApplicationName(LocalizedText.english("eclipse milo opc-ua client")).setApplicationUri("urn:eclipse:milo:examples:client").setCertificate(keyStoreLoader.getClientCertificate()).setKeyPair(keyStoreLoader.getClientKeyPair()).setIdentityProvider((IdentityProvider) new AnonymousProvider()).setRequestTimeout(Unsigned.uint(5000)).build());
    }
}
