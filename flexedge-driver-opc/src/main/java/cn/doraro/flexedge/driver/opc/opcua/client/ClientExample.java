// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.opc.opcua.client;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface ClientExample {
    default String getEndpointUrl() {
        return "opc.tcp://localhost:49320";
    }

    default Predicate<EndpointDescription> endpointFilter() {
        return e -> true;
    }

    default SecurityPolicy getSecurityPolicy() {
        return SecurityPolicy.None;
    }

    default IdentityProvider getIdentityProvider() {
        return (IdentityProvider) new UsernameProvider("u1", "123456");
    }

    void run(final OpcUaClient p0, final CompletableFuture<OpcUaClient> p1) throws Exception;
}
