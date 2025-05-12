// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.opc.opcua.server;

import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilterContext;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import java.util.List;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import cn.doraro.flexedge.driver.opc.opcua.server.types.CustomUnionType;
import org.eclipse.milo.opcua.stack.core.types.structured.StructureDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.StructureDefinition;
import org.eclipse.milo.opcua.stack.core.types.enumerated.StructureType;
import org.eclipse.milo.opcua.stack.core.types.structured.StructureField;
import cn.doraro.flexedge.driver.opc.opcua.server.types.CustomStructType;
import org.eclipse.milo.opcua.stack.core.types.structured.EnumDescription;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.types.structured.EnumDefinition;
import org.eclipse.milo.opcua.stack.core.types.structured.EnumField;
import cn.doraro.flexedge.driver.opc.opcua.server.types.CustomEnumType;
import org.eclipse.milo.opcua.sdk.server.nodes.UaObjectNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaObjectTypeNode;
import cn.doraro.flexedge.driver.opc.opcua.server.methods.GenerateEventMethod;
import org.eclipse.milo.opcua.sdk.server.api.methods.MethodInvocationHandler;
import cn.doraro.flexedge.driver.opc.opcua.server.methods.SqrtMethod;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.structured.Range;
import org.eclipse.milo.opcua.sdk.server.nodes.factories.NodeFactory;
import org.eclipse.milo.opcua.sdk.server.model.nodes.variables.AnalogItemTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilters;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilter;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.sdk.core.ValueRank;
import java.util.Set;
import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import java.lang.reflect.Array;
import org.eclipse.milo.opcua.sdk.server.model.nodes.objects.BaseEventTypeNode;
import org.eclipse.milo.opcua.sdk.server.model.nodes.objects.ServerTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.core.nodes.Node;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.Lifecycle;
import org.eclipse.milo.opcua.sdk.server.api.services.AttributeServices;
import org.slf4j.LoggerFactory;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.XmlElement;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import java.util.UUID;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.sdk.server.dtd.DataTypeDictionaryManager;
import java.util.Random;
import org.slf4j.Logger;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespaceWithLifecycle;

public class DrvNamespace extends ManagedNamespaceWithLifecycle
{
    public static final String NAMESPACE_URI = "urn:eclipse:milo:hello-world";
    private static final Object[][] STATIC_SCALAR_NODES;
    private static final Object[][] STATIC_ARRAY_NODES;
    private final Logger logger;
    private volatile Thread eventThread;
    private volatile boolean keepPostingEvents;
    private final Random random;
    private final DataTypeDictionaryManager dictionaryManager;
    private final SubscriptionModel subscriptionModel;
    
    static {
        STATIC_SCALAR_NODES = new Object[][] { { "Boolean", Identifiers.Boolean, new Variant((Object)false) }, { "Byte", Identifiers.Byte, new Variant((Object)Unsigned.ubyte(0)) }, { "SByte", Identifiers.SByte, new Variant((Object)0) }, { "Integer", Identifiers.Integer, new Variant((Object)32) }, { "Int16", Identifiers.Int16, new Variant((Object)16) }, { "Int32", Identifiers.Int32, new Variant((Object)32) }, { "Int64", Identifiers.Int64, new Variant((Object)64L) }, { "UInteger", Identifiers.UInteger, new Variant((Object)Unsigned.uint(32)) }, { "UInt16", Identifiers.UInt16, new Variant((Object)Unsigned.ushort(16)) }, { "UInt32", Identifiers.UInt32, new Variant((Object)Unsigned.uint(32)) }, { "UInt64", Identifiers.UInt64, new Variant((Object)Unsigned.ulong(64L)) }, { "Float", Identifiers.Float, new Variant((Object)3.14f) }, { "Double", Identifiers.Double, new Variant((Object)3.14) }, { "String", Identifiers.String, new Variant((Object)"string value") }, { "DateTime", Identifiers.DateTime, new Variant((Object)DateTime.now()) }, { "Guid", Identifiers.Guid, new Variant((Object)UUID.randomUUID()) }, { "ByteString", Identifiers.ByteString, new Variant((Object)new ByteString(new byte[] { 1, 2, 3, 4 })) }, { "XmlElement", Identifiers.XmlElement, new Variant((Object)new XmlElement("<a>hello</a>")) }, { "LocalizedText", Identifiers.LocalizedText, new Variant((Object)LocalizedText.english("localized text")) }, { "QualifiedName", Identifiers.QualifiedName, new Variant((Object)new QualifiedName(1234, "defg")) }, { "NodeId", Identifiers.NodeId, new Variant((Object)new NodeId(1234, "abcd")) }, { "Variant", Identifiers.BaseDataType, new Variant((Object)32) }, { "Duration", Identifiers.Duration, new Variant((Object)1.0) }, { "UtcTime", Identifiers.UtcTime, new Variant((Object)DateTime.now()) } };
        STATIC_ARRAY_NODES = new Object[][] { { "BooleanArray", Identifiers.Boolean, false }, { "ByteArray", Identifiers.Byte, Unsigned.ubyte(0) }, { "SByteArray", Identifiers.SByte, 0 }, { "Int16Array", Identifiers.Int16, 16 }, { "Int32Array", Identifiers.Int32, 32 }, { "Int64Array", Identifiers.Int64, 64L }, { "UInt16Array", Identifiers.UInt16, Unsigned.ushort(16) }, { "UInt32Array", Identifiers.UInt32, Unsigned.uint(32) }, { "UInt64Array", Identifiers.UInt64, Unsigned.ulong(64L) }, { "FloatArray", Identifiers.Float, 3.14f }, { "DoubleArray", Identifiers.Double, 3.14 }, { "StringArray", Identifiers.String, "string value" }, { "DateTimeArray", Identifiers.DateTime, DateTime.now() }, { "GuidArray", Identifiers.Guid, UUID.randomUUID() }, { "ByteStringArray", Identifiers.ByteString, new ByteString(new byte[] { 1, 2, 3, 4 }) }, { "XmlElementArray", Identifiers.XmlElement, new XmlElement("<a>hello</a>") }, { "LocalizedTextArray", Identifiers.LocalizedText, LocalizedText.english("localized text") }, { "QualifiedNameArray", Identifiers.QualifiedName, new QualifiedName(1234, "defg") }, { "NodeIdArray", Identifiers.NodeId, new NodeId(1234, "abcd") } };
    }
    
    DrvNamespace(final OpcUaServer server) {
        super(server, "urn:eclipse:milo:hello-world");
        this.logger = LoggerFactory.getLogger((Class)this.getClass());
        this.keepPostingEvents = true;
        this.random = new Random();
        this.subscriptionModel = new SubscriptionModel(server, (AttributeServices)this);
        this.dictionaryManager = new DataTypeDictionaryManager(this.getNodeContext(), "urn:eclipse:milo:hello-world");
        this.getLifecycleManager().addLifecycle((Lifecycle)this.dictionaryManager);
        this.getLifecycleManager().addLifecycle((Lifecycle)this.subscriptionModel);
        this.getLifecycleManager().addStartupTask(this::createAndAddNodes);
        this.getLifecycleManager().addLifecycle((Lifecycle)new Lifecycle() {
            public void startup() {
                DrvNamespace.this.startBogusEventNotifier();
            }
            
            public void shutdown() {
                try {
                    DrvNamespace.access$1(DrvNamespace.this, false);
                    DrvNamespace.this.eventThread.interrupt();
                    DrvNamespace.this.eventThread.join();
                }
                catch (final InterruptedException ex) {}
            }
        });
    }
    
    private void createAndAddNodes() {
        final NodeId folderNodeId = this.newNodeId("HelloWorld");
        final UaFolderNode folderNode = new UaFolderNode(this.getNodeContext(), folderNodeId, this.newQualifiedName("HelloWorld"), LocalizedText.english("HelloWorld"));
        this.getNodeManager().addNode((Node)folderNode);
        folderNode.addReference(new Reference(folderNode.getNodeId(), Identifiers.Organizes, Identifiers.ObjectsFolder.expanded(), false));
        this.addVariableNodes(folderNode);
        this.addSqrtMethod(folderNode);
        this.addGenerateEventMethod(folderNode);
        try {
            this.registerCustomEnumType();
            this.addCustomEnumTypeVariable(folderNode);
        }
        catch (final Exception e) {
            this.logger.warn("Failed to register custom enum type", (Throwable)e);
        }
        try {
            this.registerCustomStructType();
            this.addCustomStructTypeVariable(folderNode);
        }
        catch (final Exception e) {
            this.logger.warn("Failed to register custom struct type", (Throwable)e);
        }
        try {
            this.registerCustomUnionType();
            this.addCustomUnionTypeVariable(folderNode);
        }
        catch (final Exception e) {
            this.logger.warn("Failed to register custom struct type", (Throwable)e);
        }
        this.addCustomObjectTypeAndInstance(folderNode);
    }
    
    private void startBogusEventNotifier() {
        final UaNode serverNode = this.getServer().getAddressSpaceManager().getManagedNode(Identifiers.Server).orElse(null);
        if (serverNode instanceof ServerTypeNode) {
            ((ServerTypeNode)serverNode).setEventNotifier(Unsigned.ubyte(1));
            (this.eventThread = new Thread(() -> {
                while (this.keepPostingEvents) {
                    try {
                        final BaseEventTypeNode eventNode = this.getServer().getEventFactory().createEvent(this.newNodeId(UUID.randomUUID()), Identifiers.BaseEventType);
                        eventNode.setBrowseName(new QualifiedName(1, "foo"));
                        eventNode.setDisplayName(LocalizedText.english("foo"));
                        eventNode.setEventId(ByteString.of(new byte[] { 0, 1, 2, 3 }));
                        eventNode.setEventType(Identifiers.BaseEventType);
                        eventNode.setSourceNode(uaNode.getNodeId());
                        eventNode.setSourceName(uaNode.getDisplayName().getText());
                        eventNode.setTime(DateTime.now());
                        eventNode.setReceiveTime(DateTime.NULL_VALUE);
                        eventNode.setMessage(LocalizedText.english("event message!"));
                        eventNode.setSeverity(Unsigned.ushort(2));
                        this.getServer().getEventBus().post((Object)eventNode);
                        eventNode.delete();
                    }
                    catch (final Throwable e) {
                        this.logger.error("Error creating EventNode: {}", (Object)e.getMessage(), (Object)e);
                    }
                    try {
                        Thread.sleep(2000L);
                    }
                    catch (final InterruptedException ex) {}
                }
            }, "bogus-event-poster")).start();
        }
    }
    
    private void addVariableNodes(final UaFolderNode rootNode) {
        this.addArrayNodes(rootNode);
        this.addScalarNodes(rootNode);
        this.addAdminReadableNodes(rootNode);
        this.addAdminWritableNodes(rootNode);
        this.addDynamicNodes(rootNode);
        this.addDataAccessNodes(rootNode);
        this.addWriteOnlyNodes(rootNode);
    }
    
    private void addArrayNodes(final UaFolderNode rootNode) {
        final UaFolderNode arrayTypesFolder = new UaFolderNode(this.getNodeContext(), this.newNodeId("HelloWorld/ArrayTypes"), this.newQualifiedName("ArrayTypes"), LocalizedText.english("ArrayTypes"));
        this.getNodeManager().addNode((Node)arrayTypesFolder);
        rootNode.addOrganizes((UaNode)arrayTypesFolder);
        Object[][] static_ARRAY_NODES;
        for (int length = (static_ARRAY_NODES = DrvNamespace.STATIC_ARRAY_NODES).length, j = 0; j < length; ++j) {
            final Object[] os = static_ARRAY_NODES[j];
            final String name = (String)os[0];
            final NodeId typeId = (NodeId)os[1];
            final Object value = os[2];
            final Object array = Array.newInstance(value.getClass(), 5);
            for (int i = 0; i < 5; ++i) {
                Array.set(array, i, value);
            }
            final Variant variant = new Variant(array);
            UaVariableNode.build(this.getNodeContext(), builder -> {
                builder.setNodeId(this.newNodeId("HelloWorld/ArrayTypes/" + str));
                builder.setAccessLevel((Set)AccessLevel.READ_WRITE);
                builder.setUserAccessLevel((Set)AccessLevel.READ_WRITE);
                builder.setBrowseName(this.newQualifiedName(str));
                builder.setDisplayName(LocalizedText.english(str));
                builder.setDataType(dataType);
                builder.setTypeDefinition(Identifiers.BaseDataVariableType);
                builder.setValueRank(ValueRank.OneDimension.getValue());
                builder.setArrayDimensions(new UInteger[] { Unsigned.uint(0) });
                builder.setValue(new DataValue(variant2));
                builder.addAttributeFilter((AttributeFilter)new AttributeLoggingFilter(AttributeId.Value::equals));
                builder.addReference(new Reference(builder.getNodeId(), Identifiers.Organizes, uaFolderNode.getNodeId().expanded(), Reference.Direction.INVERSE));
                return builder.buildAndAdd();
            });
        }
    }
    
    private void addScalarNodes(final UaFolderNode rootNode) {
        final UaFolderNode scalarTypesFolder = new UaFolderNode(this.getNodeContext(), this.newNodeId("HelloWorld/ScalarTypes"), this.newQualifiedName("ScalarTypes"), LocalizedText.english("ScalarTypes"));
        this.getNodeManager().addNode((Node)scalarTypesFolder);
        rootNode.addOrganizes((UaNode)scalarTypesFolder);
        Object[][] static_SCALAR_NODES;
        for (int length = (static_SCALAR_NODES = DrvNamespace.STATIC_SCALAR_NODES).length, i = 0; i < length; ++i) {
            final Object[] os = static_SCALAR_NODES[i];
            final String name = (String)os[0];
            final NodeId typeId = (NodeId)os[1];
            final Variant variant = (Variant)os[2];
            final UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(this.getNodeContext()).setNodeId(this.newNodeId("HelloWorld/ScalarTypes/" + name)).setAccessLevel((Set)AccessLevel.READ_WRITE).setUserAccessLevel((Set)AccessLevel.READ_WRITE).setBrowseName(this.newQualifiedName(name)).setDisplayName(LocalizedText.english(name)).setDataType(typeId).setTypeDefinition(Identifiers.BaseDataVariableType).build();
            node.setValue(new DataValue(variant));
            node.getFilterChain().addLast((AttributeFilter)new AttributeLoggingFilter(AttributeId.Value::equals));
            this.getNodeManager().addNode((Node)node);
            scalarTypesFolder.addOrganizes((UaNode)node);
        }
    }
    
    private void addWriteOnlyNodes(final UaFolderNode rootNode) {
        final UaFolderNode writeOnlyFolder = new UaFolderNode(this.getNodeContext(), this.newNodeId("HelloWorld/WriteOnly"), this.newQualifiedName("WriteOnly"), LocalizedText.english("WriteOnly"));
        this.getNodeManager().addNode((Node)writeOnlyFolder);
        rootNode.addOrganizes((UaNode)writeOnlyFolder);
        final String name = "String";
        final UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(this.getNodeContext()).setNodeId(this.newNodeId("HelloWorld/WriteOnly/" + name)).setAccessLevel((Set)AccessLevel.WRITE_ONLY).setUserAccessLevel((Set)AccessLevel.WRITE_ONLY).setBrowseName(this.newQualifiedName(name)).setDisplayName(LocalizedText.english(name)).setDataType(Identifiers.String).setTypeDefinition(Identifiers.BaseDataVariableType).build();
        node.setValue(new DataValue(new Variant((Object)"can't read this")));
        this.getNodeManager().addNode((Node)node);
        writeOnlyFolder.addOrganizes((UaNode)node);
    }
    
    private void addAdminReadableNodes(final UaFolderNode rootNode) {
        final UaFolderNode adminFolder = new UaFolderNode(this.getNodeContext(), this.newNodeId("HelloWorld/OnlyAdminCanRead"), this.newQualifiedName("OnlyAdminCanRead"), LocalizedText.english("OnlyAdminCanRead"));
        this.getNodeManager().addNode((Node)adminFolder);
        rootNode.addOrganizes((UaNode)adminFolder);
        final String name = "String";
        final UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(this.getNodeContext()).setNodeId(this.newNodeId("HelloWorld/OnlyAdminCanRead/" + name)).setAccessLevel((Set)AccessLevel.READ_WRITE).setBrowseName(this.newQualifiedName(name)).setDisplayName(LocalizedText.english(name)).setDataType(Identifiers.String).setTypeDefinition(Identifiers.BaseDataVariableType).build();
        node.setValue(new DataValue(new Variant((Object)"shh... don't tell the lusers")));
        node.getFilterChain().addLast((AttributeFilter)new RestrictedAccessFilter(identity -> {
            if ("admin".equals(identity)) {
                return AccessLevel.READ_WRITE;
            }
            else {
                return AccessLevel.NONE;
            }
        }));
        this.getNodeManager().addNode((Node)node);
        adminFolder.addOrganizes((UaNode)node);
    }
    
    private void addAdminWritableNodes(final UaFolderNode rootNode) {
        final UaFolderNode adminFolder = new UaFolderNode(this.getNodeContext(), this.newNodeId("HelloWorld/OnlyAdminCanWrite"), this.newQualifiedName("OnlyAdminCanWrite"), LocalizedText.english("OnlyAdminCanWrite"));
        this.getNodeManager().addNode((Node)adminFolder);
        rootNode.addOrganizes((UaNode)adminFolder);
        final String name = "String";
        final UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(this.getNodeContext()).setNodeId(this.newNodeId("HelloWorld/OnlyAdminCanWrite/" + name)).setAccessLevel((Set)AccessLevel.READ_WRITE).setBrowseName(this.newQualifiedName(name)).setDisplayName(LocalizedText.english(name)).setDataType(Identifiers.String).setTypeDefinition(Identifiers.BaseDataVariableType).build();
        node.setValue(new DataValue(new Variant((Object)"admin was here")));
        node.getFilterChain().addLast((AttributeFilter)new RestrictedAccessFilter(identity -> {
            if ("admin".equals(identity)) {
                return AccessLevel.READ_WRITE;
            }
            else {
                return AccessLevel.READ_ONLY;
            }
        }));
        this.getNodeManager().addNode((Node)node);
        adminFolder.addOrganizes((UaNode)node);
    }
    
    private void addDynamicNodes(final UaFolderNode rootNode) {
        final UaFolderNode dynamicFolder = new UaFolderNode(this.getNodeContext(), this.newNodeId("HelloWorld/Dynamic"), this.newQualifiedName("Dynamic"), LocalizedText.english("Dynamic"));
        this.getNodeManager().addNode((Node)dynamicFolder);
        rootNode.addOrganizes((UaNode)dynamicFolder);
        String name = "Boolean";
        NodeId typeId = Identifiers.Boolean;
        Variant variant = new Variant((Object)false);
        UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(this.getNodeContext()).setNodeId(this.newNodeId("HelloWorld/Dynamic/" + name)).setAccessLevel((Set)AccessLevel.READ_WRITE).setBrowseName(this.newQualifiedName(name)).setDisplayName(LocalizedText.english(name)).setDataType(typeId).setTypeDefinition(Identifiers.BaseDataVariableType).build();
        node.setValue(new DataValue(variant));
        node.getFilterChain().addLast(new AttributeFilter[] { (AttributeFilter)new AttributeLoggingFilter(), AttributeFilters.getValue(ctx -> {
                new DataValue(new Variant((Object)this.random.nextBoolean()));
                return;
            }) });
        this.getNodeManager().addNode((Node)node);
        dynamicFolder.addOrganizes((UaNode)node);
        name = "Int32";
        typeId = Identifiers.Int32;
        variant = new Variant((Object)0);
        node = new UaVariableNode.UaVariableNodeBuilder(this.getNodeContext()).setNodeId(this.newNodeId("HelloWorld/Dynamic/" + name)).setAccessLevel((Set)AccessLevel.READ_WRITE).setBrowseName(this.newQualifiedName(name)).setDisplayName(LocalizedText.english(name)).setDataType(typeId).setTypeDefinition(Identifiers.BaseDataVariableType).build();
        node.setValue(new DataValue(variant));
        node.getFilterChain().addLast(new AttributeFilter[] { (AttributeFilter)new AttributeLoggingFilter(), AttributeFilters.getValue(ctx -> {
                new DataValue(new Variant((Object)this.random.nextInt()));
                return;
            }) });
        this.getNodeManager().addNode((Node)node);
        dynamicFolder.addOrganizes((UaNode)node);
        name = "Double";
        typeId = Identifiers.Double;
        variant = new Variant((Object)0.0);
        node = new UaVariableNode.UaVariableNodeBuilder(this.getNodeContext()).setNodeId(this.newNodeId("HelloWorld/Dynamic/" + name)).setAccessLevel((Set)AccessLevel.READ_WRITE).setBrowseName(this.newQualifiedName(name)).setDisplayName(LocalizedText.english(name)).setDataType(typeId).setTypeDefinition(Identifiers.BaseDataVariableType).build();
        node.setValue(new DataValue(variant));
        node.getFilterChain().addLast(new AttributeFilter[] { (AttributeFilter)new AttributeLoggingFilter(), AttributeFilters.getValue(ctx -> {
                new DataValue(new Variant((Object)this.random.nextDouble()));
                return;
            }) });
        this.getNodeManager().addNode((Node)node);
        dynamicFolder.addOrganizes((UaNode)node);
    }
    
    private void addDataAccessNodes(final UaFolderNode rootNode) {
        final UaFolderNode dataAccessFolder = new UaFolderNode(this.getNodeContext(), this.newNodeId("HelloWorld/DataAccess"), this.newQualifiedName("DataAccess"), LocalizedText.english("DataAccess"));
        this.getNodeManager().addNode((Node)dataAccessFolder);
        rootNode.addOrganizes((UaNode)dataAccessFolder);
        try {
            final AnalogItemTypeNode node = (AnalogItemTypeNode)this.getNodeFactory().createNode(this.newNodeId("HelloWorld/DataAccess/AnalogValue"), Identifiers.AnalogItemType, (NodeFactory.InstantiationCallback)new NodeFactory.InstantiationCallback() {
                public boolean includeOptionalNode(final NodeId typeDefinitionId, final QualifiedName browseName) {
                    return true;
                }
            });
            node.setBrowseName(this.newQualifiedName("AnalogValue"));
            node.setDisplayName(LocalizedText.english("AnalogValue"));
            node.setDataType(Identifiers.Double);
            node.setValue(new DataValue(new Variant((Object)3.14)));
            node.setEURange(new Range(Double.valueOf(0.0), Double.valueOf(100.0)));
            this.getNodeManager().addNode((Node)node);
            dataAccessFolder.addOrganizes((UaNode)node);
        }
        catch (final UaException e) {
            this.logger.error("Error creating AnalogItemType instance: {}", (Object)e.getMessage(), (Object)e);
        }
    }
    
    private void addSqrtMethod(final UaFolderNode folderNode) {
        final UaMethodNode methodNode = UaMethodNode.builder(this.getNodeContext()).setNodeId(this.newNodeId("HelloWorld/sqrt(x)")).setBrowseName(this.newQualifiedName("sqrt(x)")).setDisplayName(new LocalizedText((String)null, "sqrt(x)")).setDescription(LocalizedText.english("Returns the correctly rounded positive square root of a double value.")).build();
        final SqrtMethod sqrtMethod = new SqrtMethod(methodNode);
        methodNode.setInputArguments(sqrtMethod.getInputArguments());
        methodNode.setOutputArguments(sqrtMethod.getOutputArguments());
        methodNode.setInvocationHandler((MethodInvocationHandler)sqrtMethod);
        this.getNodeManager().addNode((Node)methodNode);
        methodNode.addReference(new Reference(methodNode.getNodeId(), Identifiers.HasComponent, folderNode.getNodeId().expanded(), false));
    }
    
    private void addGenerateEventMethod(final UaFolderNode folderNode) {
        final UaMethodNode methodNode = UaMethodNode.builder(this.getNodeContext()).setNodeId(this.newNodeId("HelloWorld/generateEvent(eventTypeId)")).setBrowseName(this.newQualifiedName("generateEvent(eventTypeId)")).setDisplayName(new LocalizedText((String)null, "generateEvent(eventTypeId)")).setDescription(LocalizedText.english("Generate an Event with the TypeDefinition indicated by eventTypeId.")).build();
        final GenerateEventMethod generateEventMethod = new GenerateEventMethod(methodNode);
        methodNode.setInputArguments(generateEventMethod.getInputArguments());
        methodNode.setOutputArguments(generateEventMethod.getOutputArguments());
        methodNode.setInvocationHandler((MethodInvocationHandler)generateEventMethod);
        this.getNodeManager().addNode((Node)methodNode);
        methodNode.addReference(new Reference(methodNode.getNodeId(), Identifiers.HasComponent, folderNode.getNodeId().expanded(), false));
    }
    
    private void addCustomObjectTypeAndInstance(final UaFolderNode rootFolder) {
        final UaObjectTypeNode objectTypeNode = UaObjectTypeNode.builder(this.getNodeContext()).setNodeId(this.newNodeId("ObjectTypes/MyObjectType")).setBrowseName(this.newQualifiedName("MyObjectType")).setDisplayName(LocalizedText.english("MyObjectType")).setIsAbstract(false).build();
        final UaVariableNode foo = UaVariableNode.builder(this.getNodeContext()).setNodeId(this.newNodeId("ObjectTypes/MyObjectType.Foo")).setAccessLevel((Set)AccessLevel.READ_WRITE).setBrowseName(this.newQualifiedName("Foo")).setDisplayName(LocalizedText.english("Foo")).setDataType(Identifiers.Int16).setTypeDefinition(Identifiers.BaseDataVariableType).build();
        foo.addReference(new Reference(foo.getNodeId(), Identifiers.HasModellingRule, Identifiers.ModellingRule_Mandatory.expanded(), true));
        foo.setValue(new DataValue(new Variant((Object)0)));
        objectTypeNode.addComponent((UaNode)foo);
        final UaVariableNode bar = UaVariableNode.builder(this.getNodeContext()).setNodeId(this.newNodeId("ObjectTypes/MyObjectType.Bar")).setAccessLevel((Set)AccessLevel.READ_WRITE).setBrowseName(this.newQualifiedName("Bar")).setDisplayName(LocalizedText.english("Bar")).setDataType(Identifiers.String).setTypeDefinition(Identifiers.BaseDataVariableType).build();
        bar.addReference(new Reference(bar.getNodeId(), Identifiers.HasModellingRule, Identifiers.ModellingRule_Mandatory.expanded(), true));
        bar.setValue(new DataValue(new Variant((Object)"bar")));
        objectTypeNode.addComponent((UaNode)bar);
        this.getServer().getObjectTypeManager().registerObjectType(objectTypeNode.getNodeId(), (Class)UaObjectNode.class, UaObjectNode::new);
        objectTypeNode.addReference(new Reference(objectTypeNode.getNodeId(), Identifiers.HasSubtype, Identifiers.BaseObjectType.expanded(), false));
        this.getNodeManager().addNode((Node)objectTypeNode);
        this.getNodeManager().addNode((Node)foo);
        this.getNodeManager().addNode((Node)bar);
        try {
            final UaObjectNode myObject = (UaObjectNode)this.getNodeFactory().createNode(this.newNodeId("HelloWorld/MyObject"), objectTypeNode.getNodeId());
            myObject.setBrowseName(this.newQualifiedName("MyObject"));
            myObject.setDisplayName(LocalizedText.english("MyObject"));
            rootFolder.addOrganizes((UaNode)myObject);
            myObject.addReference(new Reference(myObject.getNodeId(), Identifiers.Organizes, rootFolder.getNodeId().expanded(), false));
        }
        catch (final UaException e) {
            this.logger.error("Error creating MyObjectType instance: {}", (Object)e.getMessage(), (Object)e);
        }
    }
    
    private void registerCustomEnumType() throws Exception {
        final NodeId dataTypeId = CustomEnumType.TYPE_ID.toNodeIdOrThrow(this.getServer().getNamespaceTable());
        this.dictionaryManager.registerEnumCodec(new CustomEnumType.Codec().asBinaryCodec(), "CustomEnumType", dataTypeId);
        final EnumField[] fields = { new EnumField(Long.valueOf(0L), LocalizedText.english("Field0"), LocalizedText.NULL_VALUE, "Field0"), new EnumField(Long.valueOf(1L), LocalizedText.english("Field1"), LocalizedText.NULL_VALUE, "Field1"), new EnumField(Long.valueOf(2L), LocalizedText.english("Field2"), LocalizedText.NULL_VALUE, "Field2") };
        final EnumDefinition definition = new EnumDefinition(fields);
        final EnumDescription description = new EnumDescription(dataTypeId, new QualifiedName(this.getNamespaceIndex(), "CustomEnumType"), definition, Unsigned.ubyte(BuiltinDataType.Int32.getTypeId()));
        this.dictionaryManager.registerEnumDescription(description);
    }
    
    private void registerCustomStructType() throws Exception {
        final NodeId dataTypeId = CustomStructType.TYPE_ID.toNodeIdOrThrow(this.getServer().getNamespaceTable());
        final NodeId binaryEncodingId = CustomStructType.BINARY_ENCODING_ID.toNodeIdOrThrow(this.getServer().getNamespaceTable());
        this.dictionaryManager.registerStructureCodec(new CustomStructType.Codec().asBinaryCodec(), "CustomStructType", dataTypeId, binaryEncodingId);
        final StructureField[] fields = { new StructureField("foo", LocalizedText.NULL_VALUE, Identifiers.String, Integer.valueOf(-1), (UInteger[])null, this.getServer().getConfig().getLimits().getMaxStringLength(), Boolean.valueOf(false)), new StructureField("bar", LocalizedText.NULL_VALUE, Identifiers.UInt32, Integer.valueOf(-1), (UInteger[])null, Unsigned.uint(0), Boolean.valueOf(false)), new StructureField("baz", LocalizedText.NULL_VALUE, Identifiers.Boolean, Integer.valueOf(-1), (UInteger[])null, Unsigned.uint(0), Boolean.valueOf(false)) };
        final StructureDefinition definition = new StructureDefinition(binaryEncodingId, Identifiers.Structure, StructureType.Structure, fields);
        final StructureDescription description = new StructureDescription(dataTypeId, new QualifiedName(this.getNamespaceIndex(), "CustomStructType"), definition);
        this.dictionaryManager.registerStructureDescription(description, binaryEncodingId);
    }
    
    private void registerCustomUnionType() throws Exception {
        final NodeId dataTypeId = CustomUnionType.TYPE_ID.toNodeIdOrThrow(this.getServer().getNamespaceTable());
        final NodeId binaryEncodingId = CustomUnionType.BINARY_ENCODING_ID.toNodeIdOrThrow(this.getServer().getNamespaceTable());
        this.dictionaryManager.registerUnionCodec(new CustomUnionType.Codec().asBinaryCodec(), "CustomUnionType", dataTypeId, binaryEncodingId);
        final StructureField[] fields = { new StructureField("foo", LocalizedText.NULL_VALUE, Identifiers.UInt32, Integer.valueOf(-1), (UInteger[])null, this.getServer().getConfig().getLimits().getMaxStringLength(), Boolean.valueOf(false)), new StructureField("bar", LocalizedText.NULL_VALUE, Identifiers.String, Integer.valueOf(-1), (UInteger[])null, Unsigned.uint(0), Boolean.valueOf(false)) };
        final StructureDefinition definition = new StructureDefinition(binaryEncodingId, Identifiers.Structure, StructureType.Union, fields);
        final StructureDescription description = new StructureDescription(dataTypeId, new QualifiedName(this.getNamespaceIndex(), "CustomUnionType"), definition);
        this.dictionaryManager.registerStructureDescription(description, binaryEncodingId);
    }
    
    private void addCustomEnumTypeVariable(final UaFolderNode rootFolder) throws Exception {
        final NodeId dataTypeId = CustomEnumType.TYPE_ID.toNodeIdOrThrow(this.getServer().getNamespaceTable());
        final UaVariableNode customEnumTypeVariable = UaVariableNode.builder(this.getNodeContext()).setNodeId(this.newNodeId("HelloWorld/CustomEnumTypeVariable")).setAccessLevel((Set)AccessLevel.READ_WRITE).setUserAccessLevel((Set)AccessLevel.READ_WRITE).setBrowseName(this.newQualifiedName("CustomEnumTypeVariable")).setDisplayName(LocalizedText.english("CustomEnumTypeVariable")).setDataType(dataTypeId).setTypeDefinition(Identifiers.BaseDataVariableType).build();
        customEnumTypeVariable.setValue(new DataValue(new Variant((Object)CustomEnumType.Field1)));
        this.getNodeManager().addNode((Node)customEnumTypeVariable);
        customEnumTypeVariable.addReference(new Reference(customEnumTypeVariable.getNodeId(), Identifiers.Organizes, rootFolder.getNodeId().expanded(), false));
    }
    
    private void addCustomStructTypeVariable(final UaFolderNode rootFolder) throws Exception {
        final NodeId dataTypeId = CustomStructType.TYPE_ID.toNodeIdOrThrow(this.getServer().getNamespaceTable());
        final NodeId binaryEncodingId = CustomStructType.BINARY_ENCODING_ID.toNodeIdOrThrow(this.getServer().getNamespaceTable());
        final UaVariableNode customStructTypeVariable = UaVariableNode.builder(this.getNodeContext()).setNodeId(this.newNodeId("HelloWorld/CustomStructTypeVariable")).setAccessLevel((Set)AccessLevel.READ_WRITE).setUserAccessLevel((Set)AccessLevel.READ_WRITE).setBrowseName(this.newQualifiedName("CustomStructTypeVariable")).setDisplayName(LocalizedText.english("CustomStructTypeVariable")).setDataType(dataTypeId).setTypeDefinition(Identifiers.BaseDataVariableType).build();
        final CustomStructType value = new CustomStructType("foo", Unsigned.uint(42), true);
        final ExtensionObject xo = ExtensionObject.encodeDefaultBinary(this.getServer().getSerializationContext(), (Object)value, binaryEncodingId);
        customStructTypeVariable.setValue(new DataValue(new Variant((Object)xo)));
        this.getNodeManager().addNode((Node)customStructTypeVariable);
        customStructTypeVariable.addReference(new Reference(customStructTypeVariable.getNodeId(), Identifiers.Organizes, rootFolder.getNodeId().expanded(), false));
    }
    
    private void addCustomUnionTypeVariable(final UaFolderNode rootFolder) throws Exception {
        final NodeId dataTypeId = CustomUnionType.TYPE_ID.toNodeIdOrThrow(this.getServer().getNamespaceTable());
        final NodeId binaryEncodingId = CustomUnionType.BINARY_ENCODING_ID.toNodeIdOrThrow(this.getServer().getNamespaceTable());
        final UaVariableNode customUnionTypeVariable = UaVariableNode.builder(this.getNodeContext()).setNodeId(this.newNodeId("HelloWorld/CustomUnionTypeVariable")).setAccessLevel((Set)AccessLevel.READ_WRITE).setUserAccessLevel((Set)AccessLevel.READ_WRITE).setBrowseName(this.newQualifiedName("CustomUnionTypeVariable")).setDisplayName(LocalizedText.english("CustomUnionTypeVariable")).setDataType(dataTypeId).setTypeDefinition(Identifiers.BaseDataVariableType).build();
        final CustomUnionType value = CustomUnionType.ofBar("hello");
        final ExtensionObject xo = ExtensionObject.encodeDefaultBinary(this.getServer().getSerializationContext(), (Object)value, binaryEncodingId);
        customUnionTypeVariable.setValue(new DataValue(new Variant((Object)xo)));
        this.getNodeManager().addNode((Node)customUnionTypeVariable);
        customUnionTypeVariable.addReference(new Reference(customUnionTypeVariable.getNodeId(), Identifiers.Organizes, rootFolder.getNodeId().expanded(), false));
    }
    
    public void onDataItemsCreated(final List<DataItem> dataItems) {
        this.subscriptionModel.onDataItemsCreated((List)dataItems);
    }
    
    public void onDataItemsModified(final List<DataItem> dataItems) {
        this.subscriptionModel.onDataItemsModified((List)dataItems);
    }
    
    public void onDataItemsDeleted(final List<DataItem> dataItems) {
        this.subscriptionModel.onDataItemsDeleted((List)dataItems);
    }
    
    public void onMonitoringModeChanged(final List<MonitoredItem> monitoredItems) {
        this.subscriptionModel.onMonitoringModeChanged((List)monitoredItems);
    }
    
    static /* synthetic */ void access$1(final DrvNamespace drvNamespace, final boolean keepPostingEvents) {
        drvNamespace.keepPostingEvents = keepPostingEvents;
    }
}
