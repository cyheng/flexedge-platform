

package cn.doraro.flexedge.driver.opc.opcua.server.methods;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.model.nodes.objects.BaseEventTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;

import java.util.UUID;

public class GenerateEventMethod extends AbstractMethodInvocationHandler {
    public static final Argument EVENT_TYPE_ID;

    static {
        EVENT_TYPE_ID = new Argument("EventTypeId", Identifiers.NodeId, Integer.valueOf(-1), (UInteger[]) null, new LocalizedText("NodeId of the TypeDefinition of the event to generate."));
    }

    private final OpcUaServer server;

    public GenerateEventMethod(final UaMethodNode methodNode) {
        super(methodNode);
        this.server = methodNode.getNodeContext().getServer();
    }

    public Argument[] getInputArguments() {
        return new Argument[]{GenerateEventMethod.EVENT_TYPE_ID};
    }

    public Argument[] getOutputArguments() {
        return new Argument[0];
    }

    protected Variant[] invoke(final AbstractMethodInvocationHandler.InvocationContext invocationContext, final Variant[] inputValues) throws UaException {
        final NodeId eventTypeId = (NodeId) inputValues[0].getValue();
        final BaseEventTypeNode eventNode = this.server.getEventFactory().createEvent(new NodeId(1, UUID.randomUUID()), eventTypeId);
        eventNode.setBrowseName(new QualifiedName(1, "foo"));
        eventNode.setDisplayName(LocalizedText.english("foo"));
        eventNode.setEventId(ByteString.of(new byte[]{0, 1, 2, 3}));
        eventNode.setEventType(Identifiers.BaseEventType);
        eventNode.setSourceNode(this.getNode().getNodeId());
        eventNode.setSourceName(this.getNode().getDisplayName().getText());
        eventNode.setTime(DateTime.now());
        eventNode.setReceiveTime(DateTime.NULL_VALUE);
        eventNode.setMessage(LocalizedText.english("event message!"));
        eventNode.setSeverity(Unsigned.ushort(2));
        this.server.getEventBus().post((Object) eventNode);
        eventNode.delete();
        return new Variant[0];
    }
}
