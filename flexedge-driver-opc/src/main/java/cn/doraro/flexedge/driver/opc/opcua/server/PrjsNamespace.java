

package cn.doraro.flexedge.driver.opc.opcua.server;

import cn.doraro.flexedge.core.*;
import cn.doraro.flexedge.driver.opc.opcua.UaHelper;
import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.core.nodes.Node;
import org.eclipse.milo.opcua.sdk.server.Lifecycle;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespaceWithLifecycle;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.api.services.AttributeServices;
import org.eclipse.milo.opcua.sdk.server.dtd.DataTypeDictionaryManager;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilter;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilters;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;

import java.util.List;
import java.util.Set;

public class PrjsNamespace extends ManagedNamespaceWithLifecycle {
    public static final String NAMESPACE_URI = "urn:iottree:server";
    private final DataTypeDictionaryManager dictionaryManager;
    private final SubscriptionModel subscriptionModel;

    public PrjsNamespace(final OpcUaServer server) {
        super(server, "urn:iottree:server");
        this.subscriptionModel = new SubscriptionModel(server, (AttributeServices) this);
        this.dictionaryManager = new DataTypeDictionaryManager(this.getNodeContext(), "urn:iottree:server");
        this.getLifecycleManager().addLifecycle((Lifecycle) this.dictionaryManager);
        this.getLifecycleManager().addLifecycle((Lifecycle) this.subscriptionModel);
        this.getLifecycleManager().addStartupTask(this::createAndAddNodes);
        this.getLifecycleManager().addLifecycle((Lifecycle) new Lifecycle() {
            public void startup() {
            }

            public void shutdown() {
            }
        });
    }

    private void createAndAddNodes() {
        for (final UAPrj p : UAManager.getInstance().listPrjs()) {
            this.addPrjNodes(p);
        }
    }

    private void addPrjNodes(final UAPrj prj) {
        final NodeId nodeid = this.newNodeId(prj.getName());
        final UaFolderNode pnode = new UaFolderNode(this.getNodeContext(), nodeid, this.newQualifiedName(prj.getName()), LocalizedText.english(prj.getTitle()));
        this.getNodeManager().addNode(pnode);
        pnode.addReference(new Reference(pnode.getNodeId(), Identifiers.Organizes, Identifiers.ObjectsFolder.expanded(), false));
        for (final UACh ch : prj.getChs()) {
            this.addChNodes(ch, pnode);
        }
        this.addTagsNodes((UANodeOCTagsCxt) prj, pnode);
    }

    private void addTagGNodes(final UANodeOCTagsGCxt tagscxt, final UaFolderNode pnode) {
        final List<UATagG> taggs = tagscxt.getSubTagGs();
        if (taggs == null) {
            return;
        }
        for (final UATagG tagg : taggs) {
            final NodeId tggnid = this.newNodeId(tagg.getNodePathCxt());
            final UaFolderNode tggn = new UaFolderNode(this.getNodeContext(), tggnid, this.newQualifiedName(tagg.getName()), LocalizedText.english(tagg.getTitle()));
            this.getNodeManager().addNode( tggn);
            pnode.addOrganizes((UaNode) tggn);
            this.addTagGNodes((UANodeOCTagsGCxt) tagg, tggn);
            this.addTagsNodes((UANodeOCTagsCxt) tagg, tggn);
        }
    }

    private void addTagsNodes(final UANodeOCTagsCxt tagscxt, final UaFolderNode pnode) {
        final List<UATag> tags = tagscxt.listTags();
        final String ppath = tagscxt.getNodePathCxt("/");
        for (final UATag tag : tags) {
            final String name = tag.getName();
            final NodeId tpid = UaHelper.transValTp2UaTp(tag.getValTp());
            if (tpid == null) {
                continue;
            }
            final UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(this.getNodeContext()).setNodeId(this.newNodeId(String.valueOf(ppath) + "/" + name)).setAccessLevel((Set) AccessLevel.READ_WRITE).setUserAccessLevel((Set) AccessLevel.READ_WRITE).setBrowseName(this.newQualifiedName(name)).setDisplayName(LocalizedText.english(name)).setDataType(tpid).setTypeDefinition(Identifiers.BaseDataVariableType).build();
            node.getFilterChain().addLast((AttributeFilter) new AttributeLoggingFilter(AttributeId.Value::equals));
            node.getFilterChain().addLast(new AttributeFilter[]{(AttributeFilter) new AttributeLoggingFilter(), AttributeFilters.getValue(ctx -> {
                final UAVal uav = uaTag.RT_getVal();
                if (uav.isValid()) {
                    final Variant v = new Variant(uav.getObjVal());
                    return new DataValue(v);
                } else {
                    return new DataValue(StatusCode.BAD);
                }
            })});
            this.getNodeManager().addNode( node);
            pnode.addOrganizes((UaNode) node);
        }
    }

    private void addChNodes(final UACh ch, final UaFolderNode prjn) {
        final NodeId chnid = this.newNodeId(ch.getNodePathCxt());
        final UaFolderNode chn = new UaFolderNode(this.getNodeContext(), chnid, this.newQualifiedName(ch.getName()), LocalizedText.english(ch.getTitle()));
        this.getNodeManager().addNode(  chn);
        prjn.addOrganizes((UaNode) chn);
        final List<UADev> devs = ch.getDevs();
        if (devs != null) {
            for (final UADev dev : devs) {
                final NodeId devnid = this.newNodeId(dev.getNodePathCxt());
                final UaFolderNode devn = new UaFolderNode(this.getNodeContext(), devnid, this.newQualifiedName(dev.getName()), LocalizedText.english(dev.getTitle()));
                this.getNodeManager().addNode(  devn);
                chn.addOrganizes((UaNode) devn);
                this.addTagGNodes((UANodeOCTagsGCxt) dev, devn);
                this.addTagsNodes((UANodeOCTagsCxt) dev, devn);
            }
        }
        this.addTagGNodes((UANodeOCTagsGCxt) ch, chn);
        this.addTagsNodes((UANodeOCTagsCxt) ch, chn);
    }

    public void onDataItemsCreated(final List<DataItem> dataItems) {
        this.subscriptionModel.onDataItemsCreated((List) dataItems);
    }

    public void onDataItemsModified(final List<DataItem> dataItems) {
        this.subscriptionModel.onDataItemsModified((List) dataItems);
    }

    public void onDataItemsDeleted(final List<DataItem> dataItems) {
        this.subscriptionModel.onDataItemsDeleted((List) dataItems);
    }

    public void onMonitoringModeChanged(final List<MonitoredItem> monitoredItems) {
        this.subscriptionModel.onMonitoringModeChanged((List) monitoredItems);
    }
}
