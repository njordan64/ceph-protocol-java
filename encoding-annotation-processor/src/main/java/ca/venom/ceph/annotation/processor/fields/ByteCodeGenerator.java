package ca.venom.ceph.annotation.processor.fields;

import ca.venom.ceph.annotation.processor.CodeGenerationContext;
import ca.venom.ceph.annotation.processor.EncodableField;

public class ByteCodeGenerator extends FieldCodeGenerator {
    private CodeGenerationContext context;

    public ByteCodeGenerator(CodeGenerationContext context) {
        this.context = context;
    }

    @Override
    public void generateEncodeJavaCode(StringBuilder sb,
                                       EncodableField field,
                                       int indentation,
                                       String variableName,
                                       String typeName) {
        String getter = getGetterName(field, typeName, variableName, field.getName());

        sb.append(String.format(
                "%sif (%s == null) {\n",
                getIndentString(indentation),
                getter
        ));
        sb.append(String.format(
                "%sbyteBuf.writeByte(0);\n",
                getIndentString(indentation + 1)
        ));
        sb.append(String.format(
                "%s} else {\n",
                getIndentString(indentation)
        ));
        sb.append(String.format(
                "%sbyteBuf.writeByte(%s);\n",
                getIndentString(indentation + 1),
                getter
        ));
        sb.append(String.format(
                "%s}\n",
                getIndentString(indentation)
        ));
    }

    @Override
    public void generateDecodeJavaCode(StringBuilder sb,
                                       EncodableField field,
                                       int indentation,
                                       String variableName,
                                       String typeName) {
        String setter = getSetter(
                field,
                typeName,
                variableName,
                field.getName(),
                "new Byte(byteBuf.readByte())");
        sb.append(getIndentString(indentation));
        sb.append(setter);
        sb.append("\n");
    }
}
