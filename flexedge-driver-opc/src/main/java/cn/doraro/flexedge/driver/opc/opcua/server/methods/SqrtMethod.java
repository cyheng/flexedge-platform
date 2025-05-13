

package cn.doraro.flexedge.driver.opc.opcua.server.methods;

import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqrtMethod extends AbstractMethodInvocationHandler {
    public static final Argument X;
    public static final Argument X_SQRT;

    static {
        X = new Argument("x", Identifiers.Double, Integer.valueOf(-1), (UInteger[]) null, new LocalizedText("A value."));
        X_SQRT = new Argument("x_sqrt", Identifiers.Double, Integer.valueOf(-1), (UInteger[]) null, new LocalizedText("A value."));
    }

    private final Logger logger;

    public SqrtMethod(final UaMethodNode node) {
        super(node);
        this.logger = LoggerFactory.getLogger((Class) this.getClass());
    }

    public Argument[] getInputArguments() {
        return new Argument[]{SqrtMethod.X};
    }

    public Argument[] getOutputArguments() {
        return new Argument[]{SqrtMethod.X_SQRT};
    }

    protected Variant[] invoke(final AbstractMethodInvocationHandler.InvocationContext invocationContext, final Variant[] inputValues) {
        this.logger.debug("Invoking sqrt() method of objectId={}", (Object) invocationContext.getObjectId());
        final double x = (double) inputValues[0].getValue();
        final double xSqrt = Math.sqrt(x);
        return new Variant[]{new Variant((Object) xSqrt)};
    }
}
