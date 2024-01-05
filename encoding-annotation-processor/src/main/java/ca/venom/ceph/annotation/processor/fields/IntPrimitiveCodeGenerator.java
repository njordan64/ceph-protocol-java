package ca.venom.ceph.annotation.processor.fields;

import ca.venom.ceph.annotation.processor.CodeGenerationContext;
import ca.venom.ceph.annotation.processor.EncodableField;
import ca.venom.ceph.encoding.annotations.ByteOrderPreference;

public class IntPrimitiveCodeGenerator extends FieldCodeGenerator {
    private CodeGenerationContext context;

    public IntPrimitiveCodeGenerator(CodeGenerationContext context) {
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
                    "%sbyteBuf.writeIntLE(%s);\n",
                    getIndentString(indentation + 1),
                    getter
            ));
            sb.append(String.format(
                    "%s} else {\n",
                    getIndentString(indentation)
            ));
            sb.append(String.format(
                    "%sbyteBuf.writeInt(%s);\n",
                    getIndentString(indentation + 1),
                    getter
            ));
            sb.append(String.format(
                    "%s}\n",
                    getIndentString(indentation)
            ));
        } else {
            sb.append(String.format(
                    "%sbyteBuf.writeInt%s(%s);\n",
                    getIndentString(indentation),
                    (field.getByteOrderPreference() == ByteOrderPreference.LE) ? "LE" : "",
                    variableName,
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
                    "byteBuf.readIntLE()");
            sb.append(getIndentString(indentation));
            sb.append(setter);
            sb.append("\n");
        } else if (field.getByteOrderPreference() == ByteOrderPreference.BE) {
            String setter = getSetter(
                    field,
                    typeName,
                    variableName,
                    field.getName(),
                    "byteBuf.readInt()");
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
                    "byteBuf.readIntLE()");
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
                    "byteBuf.readInt()");
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
