

package cn.doraro.flexedge.driver.opc.opcua.server.types;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.eclipse.milo.opcua.stack.core.UaSerializationException;
import org.eclipse.milo.opcua.stack.core.serialization.SerializationContext;
import org.eclipse.milo.opcua.stack.core.serialization.UaDecoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaEncoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaStructure;
import org.eclipse.milo.opcua.stack.core.serialization.codecs.GenericDataTypeCodec;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;

public class CustomStructType implements UaStructure {
    public static final ExpandedNodeId TYPE_ID;
    public static final ExpandedNodeId BINARY_ENCODING_ID;

    static {
        TYPE_ID = ExpandedNodeId.parse(String.format("nsu=%s;s=%s", "urn:eclipse:milo:hello-world", "DataType.CustomStructType"));
        BINARY_ENCODING_ID = ExpandedNodeId.parse(String.format("nsu=%s;s=%s", "urn:eclipse:milo:hello-world", "DataType.CustomStructType.BinaryEncoding"));
    }

    private final String foo;
    private final UInteger bar;
    private final boolean baz;

    public CustomStructType() {
        this(null, Unsigned.uint(0), false);
    }

    public CustomStructType(final String foo, final UInteger bar, final boolean baz) {
        this.foo = foo;
        this.bar = bar;
        this.baz = baz;
    }

    public String getFoo() {
        return this.foo;
    }

    public UInteger getBar() {
        return this.bar;
    }

    public boolean isBaz() {
        return this.baz;
    }

    public ExpandedNodeId getTypeId() {
        return CustomStructType.TYPE_ID;
    }

    public ExpandedNodeId getBinaryEncodingId() {
        return CustomStructType.BINARY_ENCODING_ID;
    }

    public ExpandedNodeId getXmlEncodingId() {
        return ExpandedNodeId.NULL_VALUE;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final CustomStructType that = (CustomStructType) o;
        return this.baz == that.baz && Objects.equal((Object) this.foo, (Object) that.foo) && Objects.equal((Object) this.bar, (Object) that.bar);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(new Object[]{this.foo, this.bar, this.baz});
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object) this).add("foo", (Object) this.foo).add("bar", (Object) this.bar).add("baz", this.baz).toString();
    }

    public static class Codec extends GenericDataTypeCodec<CustomStructType> {
        public Class<CustomStructType> getType() {
            return CustomStructType.class;
        }

        public CustomStructType decode(final SerializationContext context, final UaDecoder decoder) throws UaSerializationException {
            final String foo = decoder.readString("Foo");
            final UInteger bar = decoder.readUInt32("Bar");
            final boolean baz = decoder.readBoolean("Baz");
            return new CustomStructType(foo, bar, baz);
        }

        public void encode(final SerializationContext context, final UaEncoder encoder, final CustomStructType value) throws UaSerializationException {
            encoder.writeString("Foo", value.foo);
            encoder.writeUInt32("Bar", value.bar);
            encoder.writeBoolean("Baz", Boolean.valueOf(value.baz));
        }
    }
}
