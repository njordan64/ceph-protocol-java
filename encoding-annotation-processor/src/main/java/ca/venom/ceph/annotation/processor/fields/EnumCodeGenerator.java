package ca.venom.ceph.annotation.processor.fields;

import ca.venom.ceph.annotation.processor.CodeGenerationContext;
import ca.venom.ceph.annotation.processor.EncodableField;

public class EnumCodeGenerator extends FieldCodeGenerator {
    private CodeGenerationContext context;

    public EnumCodeGenerator(CodeGenerationContext context) {
        this.context = context;
    }

    @Override
    public void generateEncodeJavaCode(StringBuilder sb,
                                       EncodableField field,
                                       int indentation,
                                       String variableName,
                                       String typeName) {
        int encodingSize = 1;
        if (field.getEncodingSize() != null) {
            encodingSize = field.getEncodingSize();
        }
        String getter = getGetterName(field, typeName, variableName, field.getName());

        if (encodingSize == 1) {
            sb.append(String.format(
                    "%sbyteBuf.writeByte((byte) %s.getValueInt());\n",
                    getIndentString(indentation),
                    getter
            ));
        } else {
            sb.append(String.format(
                    "%sif (le) {\n",
                    getIndentString(indentation)
            ));
            sb.append(String.format(
                    "%sbyteBuf.write%s%s.getValueInt());\n",
                    getIndentString(indentation + 1),
                    (encodingSize == 2) ? "ShortLE((short) " : "IntLE(",
                    getter
            ));
            sb.append(String.format(
                    "%s} else {\n",
                    getIndentString(indentation)
            ));
            sb.append(String.format(
                    "%sbyteBuf.write%s%s.getValueInt());\n",
                    getIndentString(indentation + 1),
                    (encodingSize == 2) ? "Short((short) " : "Int(",
                    getter
            ));
            sb.append(String.format(
                    "%s}\n",
                    getIndentString(indentation)
            ));
        }
    }

    @Override
    public void generateDecodeJavaCode(StringBuilder sb,
                                       EncodableField field,
                                       int indentation,
                                       String variableName,
                                       String typeName) {
        int encodingSize = 1;
        if (field.getEncodingSize() != null) {
            encodingSize = field.getEncodingSize();
        }
        sb.append(String.format(
                "%s{\n",
                getIndentString(indentation)
        ));
        switch (encodingSize) {
            case 1 -> sb.append(String.format(
                    "%sint value = byteBuf.readByte();\n",
                    getIndentString(indentation + 1)
            ));
            case 2 -> sb.append(String.format(
                    "%sint value = (le) ? byteBuf.readShortLE() : byteBuf.readShort();\n",
                    getIndentString(indentation + 1)
            ));
            default -> sb.append(String.format(
                    "%sint value = (le) ? byteBuf.readIntLE() : byteBuf.readInt();\n",
                    getIndentString(indentation + 1)
            ));
        }
        String value = String.format(
                "%s.getFromValueInt(value)",
                typeName
        );
        String setter = getSetter(
                field,
                typeName,
                variableName,
                field.getName(),
                value
        );
        sb.append(getIndentString(indentation + 1));
        sb.append(setter);
        sb.append("\n");
        sb.append(String.format(
                "%s}\n",
                getIndentString(indentation)
        ));
    }
}
