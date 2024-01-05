package ca.venom.ceph.annotation.processor.fields;

import ca.venom.ceph.annotation.processor.CodeGenerationContext;
import ca.venom.ceph.annotation.processor.EncodableField;

public class StringCodeGenerator extends FieldCodeGenerator {
    private CodeGenerationContext context;

    public StringCodeGenerator(CodeGenerationContext context) {
        this.context = context;
    }

    @Override
    public void generateEncodeJavaCode(StringBuilder sb,
                                       EncodableField field,
                                       int indentation,
                                       String variableName,
                                       String typeName) {
        context.getImports().add("java.nio.charset.StandardCharsets");

        String getter = getGetterName(field, typeName, variableName, field.getName());

        sb.append(String.format(
                "%sif (%s == null) {\n",
                getIndentString(indentation),
                getter
        ));
        sb.append(String.format(
                "%sbyteBuf.writeZero(4);\n",
                getIndentString(indentation + 1)
        ));
        sb.append(String.format(
                "%s} else {\n",
                getIndentString(indentation)
        ));
        sb.append(String.format(
                "%sbyte[] strBytes = %s.getBytes(StandardCharsets.UTF_8);\n",
                getIndentString(indentation + 1),
                getter
        ));
        sb.append(String.format(
                "%sif (le) {\n",
                getIndentString(indentation + 1)
        ));
        sb.append(String.format(
                "%sbyteBuf.writeIntLE(strBytes.length);\n",
                getIndentString(indentation + 2)
        ));
        sb.append(String.format(
                "%s} else {\n",
                getIndentString(indentation + 1)
        ));
        sb.append(String.format(
                "%sbyteBuf.writeInt(strBytes.length);\n",
                getIndentString(indentation + 2)
        ));
        sb.append(String.format(
                "%s}\n",
                getIndentString(indentation + 1)
        ));
        sb.append(String.format(
                "%sbyteBuf.writeBytes(strBytes);\n",
                getIndentString(indentation + 1)
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
        sb.append(String.format(
                "%s{\n",
                getIndentString(indentation)
        ));
        sb.append(String.format(
                "%sint size = le ? byteBuf.readIntLE() : byteBuf.readInt();\n",
                getIndentString(indentation + 1)
        ));
        sb.append(String.format(
                "%sif (size == 0) {\n",
                getIndentString(indentation + 1)
        ));
        String setter = getSetter(
                field,
                typeName,
                variableName,
                field.getName(),
                "null"
        );
        sb.append(getIndentString(indentation + 2));
        sb.append(setter);
        sb.append("\n");
        sb.append(String.format(
                "%s} else {\n",
                getIndentString(indentation + 1)
        ));

        sb.append(String.format(
                "%sbyte[] bytes = new byte[size];\n",
                getIndentString(indentation + 2)
        ));
        sb.append(String.format(
                "%sbyteBuf.readBytes(bytes);\n",
                getIndentString(indentation + 2)
        ));
        setter = getSetter(
                field,
                typeName,
                variableName,
                field.getName(),
                "new String(bytes, StandardCharsets.UTF_8)"
        );
        sb.append(getIndentString(indentation + 2));
        sb.append(setter);
        sb.append("\n");
        sb.append(String.format(
                "%s}\n",
                getIndentString(indentation + 1)
        ));
        sb.append(String.format(
                "%s}\n",
                getIndentString(indentation)
        ));
    }
}
