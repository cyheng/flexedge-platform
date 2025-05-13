

package cn.doraro.flexedge.driver.opc.opcua.server;

import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.server.Session;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilter;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilterContext;
import org.eclipse.milo.opcua.stack.core.AttributeId;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class RestrictedAccessFilter implements AttributeFilter {
    private static final Set<AccessLevel> INTERNAL_ACCESS;

    static {
        INTERNAL_ACCESS = (Set) AccessLevel.READ_WRITE;
    }

    private final Function<Object, Set<AccessLevel>> accessLevelsFn;

    public RestrictedAccessFilter(final Function<Object, Set<AccessLevel>> accessLevelsFn) {
        this.accessLevelsFn = accessLevelsFn;
    }

    public Object getAttribute(final AttributeFilterContext.GetAttributeContext ctx, final AttributeId attributeId) {
        if (attributeId == AttributeId.UserAccessLevel) {
            final Optional<Object> identity = ctx.getSession().map(Session::getIdentityObject);
            final Set<AccessLevel> accessLevels = identity.map((Function<? super Object, ? extends Set<AccessLevel>>) this.accessLevelsFn).orElse(RestrictedAccessFilter.INTERNAL_ACCESS);
            return AccessLevel.toValue((Set) accessLevels);
        }
        return ctx.getAttribute(attributeId);
    }
}
