

package cn.doraro.flexedge.driver.opc.opcua.client;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseResult;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;
import org.eclipse.milo.opcua.stack.core.util.ConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class BrowseExample implements ClientExample {
    private final Logger logger;

    public BrowseExample() {
        this.logger = LoggerFactory.getLogger((Class) this.getClass());
    }

    public static void main(final String[] args) throws Exception {
        final BrowseExample example = new BrowseExample();
        new ClientExampleRunner(example).run();
    }

    @Override
    public void run(final OpcUaClient client, final CompletableFuture<OpcUaClient> future) throws Exception {
        client.connect().get();
        this.browseNode("", client, Identifiers.RootFolder);
        future.complete(client);
    }

    private void browseNode(final String indent, final OpcUaClient client, final NodeId browseRoot) {
        final BrowseDescription browse = new BrowseDescription(browseRoot, BrowseDirection.Forward, Identifiers.References, Boolean.valueOf(true), Unsigned.uint(NodeClass.Object.getValue() | NodeClass.Variable.getValue()), Unsigned.uint(BrowseResultMask.All.getValue()));
        try {
            final BrowseResult browseResult = client.browse(browse).get();
            final List<ReferenceDescription> references = ConversionUtil.toList(browseResult.getReferences());
            for (final ReferenceDescription rd : references) {
                final QualifiedName qn = rd.getBrowseName();
                final String tn = rd.getTypeDefinition().getType().name();
                this.logger.info("{} Node={}", (Object) indent, (Object) (String.valueOf(qn.getName()) + " " + tn));
                rd.getNodeId().toNodeId(client.getNamespaceTable()).ifPresent((nodeId) -> {
                    this.browseNode(indent + "  ", client, nodeId);
                });            }
        } catch (final InterruptedException | ExecutionException e) {
            this.logger.error("Browsing nodeId={} failed: {}", new Object[]{browseRoot, e.getMessage(), e});
        }
    }
}
