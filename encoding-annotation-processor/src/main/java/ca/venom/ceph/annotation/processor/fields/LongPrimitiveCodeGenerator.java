package ca.venom.ceph.annotation.processor.fields;

import ca.venom.ceph.annotation.processor.CodeGenerationContext;
import ca.venom.ceph.annotation.processor.EncodableField;
import ca.venom.ceph.encoding.annotations.ByteOrderPreference;

public class LongPrimitiveCodeGenerator extends FieldCodeGenerator {
    private CodeGenerationContext context;

    public LongPrimitiveCodeGenerator(CodeGenerationContext context) {
        this.context = context;
    }

    @Override
    public void generateEncodeJavaCode(StringBuilder sb,
                                       EncodableField field,
                                       int indentation,
                                       String variableName,
                                       String typeName) {
        String getter = getGetterName(field, typeName, variableName, field.getName());

        if (field.getByteOrderPreference() == ByteOrderPreference.NONE) {
            sb.append(String.format(
                    "%sif (le) {\n",
                    getIndentString(indentation)
            ));
            sb.append(String.format(
                    "%sbyteBuf.writeLongLE(%s);\n",
                    getIndentString(indentation + 1),
                    getter
            ));
            sb.append(String.format(
                    "%s} else {\n",
                    getIndentString(indentation)
            ));
            sb.append(String.format(
                    "%sbyteBuf.writeLong(%s);\n",
                    getIndentString(indentation + 1),
                    getter
            ));
            sb.append(String.format(
                    "%s}\n",
                    getIndentString(indentation)
            ));
        } else {
            sb.append(String.format(
                    "%sbyteBuf.writeLong%s(%s);\n",
                    getIndentString(indentation),
                    (field.getByteOrderPreference() == ByteOrderPreference.LE) ? "LE" : "",
                    getter
            ));
        }
    }

    @Override
    public void generateDecodeJavaCode(StringBuilder sb,
                                       EncodableField field,
                                       int indentation,
                                       String variableName,
                                       String typeName) {
        if (field.getByteOrderPreference() == ByteOrderPreference.LE) {
            String setter = getSetter(
                    field,
                    typeName,
                    variableName,
                    field.getName(),
                    "byteBuf.readLongLE()");
            sb.append(getIndentString(indentation));
            sb.append(setter);
            sb.append("\n");
        } else if (field.getByteOrderPreference() == ByteOrderPreference.BE) {
            String setter = getSetter(
                    field,
                    typeName,
                    variableName,
                    field.getName(),
                    "byteBuf.readLong()");
            sb.append(getIndentString(indentation));
            sb.append(setter);
            sb.append("\n");
        } else {
            sb.append(String.format(
                    "%sif (le) {\n",
                    getIndentString(indentation)
            ));
            String setter = getSetter(
                    field,
                    typeName,
                    variableName,
                    field.getName(),
                    "byteBuf.readLongLE()");
            sb.append(getIndentString(indentation + 1));
            sb.append(setter);
            sb.append("\n");

            sb.append(String.format(
                    "%s} else {\n",
                    getIndentString(indentation)
            ));
            setter = getSetter(
                    field,
                    typeName,
                    variableName,
                    field.getName(),
                    "byteBuf.readLong()");
            sb.append(getIndentString(indentation + 1));
            sb.append(setter);
            sb.append("\n");
            sb.append(String.format(
                    "%s}\n",
                    getIndentString(indentation)
            ));
        }
    }
}
