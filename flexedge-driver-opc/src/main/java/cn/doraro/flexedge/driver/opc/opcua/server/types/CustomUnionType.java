

package cn.doraro.flexedge.driver.opc.opcua.server.types;

import org.eclipse.milo.opcua.stack.core.UaSerializationException;
import org.eclipse.milo.opcua.stack.core.serialization.SerializationContext;
import org.eclipse.milo.opcua.stack.core.serialization.UaDecoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaEncoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaStructure;
import org.eclipse.milo.opcua.stack.core.serialization.codecs.GenericDataTypeCodec;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;
import org.eclipse.milo.opcua.stack.core.types.structured.Union;

public class CustomUnionType extends Union implements UaStructure {
    public static final ExpandedNodeId TYPE_ID;
    public static final ExpandedNodeId BINARY_ENCODING_ID;

    static {
        TYPE_ID = ExpandedNodeId.parse(String.format("nsu=%s;s=%s", "urn:eclipse:milo:hello-world", "DataType.CustomUnionType"));
        BINARY_ENCODING_ID = ExpandedNodeId.parse(String.format("nsu=%s;s=%s", "urn:eclipse:milo:hello-world", "DataType.CustomUnionType.BinaryEncoding"));
    }

    private final Type type;
    private final Object value;

    private CustomUnionType(final Type type, final Object value) {
        this.type = type;
        this.value = value;
    }

    public static CustomUnionType ofNull() {
        return new CustomUnionType(Type.Null, null);
    }

    public static CustomUnionType ofFoo(final UInteger value) {
        return new CustomUnionType(Type.Foo, value);
    }

    public static CustomUnionType ofBar(final String value) {
        return new CustomUnionType(Type.Bar, value);
    }

    public ExpandedNodeId getTypeId() {
        return CustomUnionType.TYPE_ID;
    }

    public ExpandedNodeId getBinaryEncodingId() {
        return CustomUnionType.BINARY_ENCODING_ID;
    }

    public ExpandedNodeId getXmlEncodingId() {
        return ExpandedNodeId.NULL_VALUE;
    }

    public UInteger asFoo() {
        return (UInteger) this.value;
    }

    public String asBar() {
        return (String) this.value;
    }

    public boolean isNull() {
        return this.type == Type.Null;
    }

    public boolean isFoo() {
        return this.type == Type.Foo;
    }

    public boolean isBar() {
        return this.type == Type.Bar;
    }

    enum Type {
        Null("Null", 0),
        Foo("Foo", 1),
        Bar("Bar", 2);

        private Type(final String name, final int ordinal) {
        }
    }

    public static class Codec extends GenericDataTypeCodec<CustomUnionType> {
        public Class<CustomUnionType> getType() {
            return CustomUnionType.class;
        }

        public CustomUnionType decode(final SerializationContext context, final UaDecoder decoder) {
            final UInteger switchValue = decoder.readUInt32("SwitchValue");
            switch (switchValue.intValue()) {
                case 0: {
                    return CustomUnionType.ofNull();
                }
                case 1: {
                    final UInteger foo = decoder.readUInt32("foo");
                    return CustomUnionType.ofFoo(foo);
                }
                case 2: {
                    final String bar = decoder.readString("bar");
                    return CustomUnionType.ofBar(bar);
                }
                default: {
                    throw new UaSerializationException(2147942400L, "unknown field in Union CustomUnionType: " + switchValue);
                }
            }
        }

        public void encode(final SerializationContext context, final UaEncoder encoder, final CustomUnionType value) {
            encoder.writeUInt32("SwitchValue", Unsigned.uint(value.type.ordinal()));
            switch (value.type) {
                case Null: {
                    break;
                }
                case Foo: {
                    encoder.writeUInt32("foo", value.asFoo());
                    break;
                }
                case Bar: {
                    encoder.writeString("bar", value.asBar());
                    break;
                }
                default: {
                    throw new IllegalArgumentException("unhandled type: " + value.type);
                }
            }
        }
    }
}
