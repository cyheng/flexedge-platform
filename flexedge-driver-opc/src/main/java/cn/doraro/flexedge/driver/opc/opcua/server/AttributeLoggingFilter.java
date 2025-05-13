

package cn.doraro.flexedge.driver.opc.opcua.server;

import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilter;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilterContext;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public class AttributeLoggingFilter implements AttributeFilter {
    private final Logger logger;
    private final Predicate<AttributeId> attributePredicate;

    public AttributeLoggingFilter() {
        this(attributeId -> true);
    }

    public AttributeLoggingFilter(final Predicate<AttributeId> attributePredicate) {
        this.logger = LoggerFactory.getLogger((Class) this.getClass());
        this.attributePredicate = attributePredicate;
    }

    public Object getAttribute(final AttributeFilterContext.GetAttributeContext ctx, final AttributeId attributeId) {
        final Object value = ctx.getAttribute(attributeId);
        if (this.attributePredicate.test(attributeId) && ctx.getSession().isPresent()) {
            this.logger.info("get nodeId={} attributeId={} value={}", new Object[]{ctx.getNode().getNodeId(), attributeId, value});
        }
        return value;
    }

    public void setAttribute(final AttributeFilterContext.SetAttributeContext ctx, final AttributeId attributeId, final Object value) {
        if (this.attributePredicate.test(attributeId) && ctx.getSession().isPresent()) {
            this.logger.info("set nodeId={} attributeId={} value={}", new Object[]{ctx.getNode().getNodeId(), attributeId, value});
        }
        ctx.setAttribute(attributeId, value);
    }
}
