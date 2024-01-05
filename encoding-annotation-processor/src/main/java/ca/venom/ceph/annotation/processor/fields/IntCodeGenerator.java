package ca.venom.ceph.annotation.processor.fields;

import ca.venom.ceph.annotation.processor.CodeGenerationContext;
import ca.venom.ceph.annotation.processor.EncodableField;
import ca.venom.ceph.encoding.annotations.ByteOrderPreference;

public class IntCodeGenerator extends FieldCodeGenerator {
    private CodeGenerationContext context;

    public IntCodeGenerator(CodeGenerationContext context) {
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
                    "%sbyteBuf.writeIntLE(%s == null ? 0 : %s);\n",
                    getIndentString(indentation + 1),
                    getter,
                    getter
            ));
            sb.append(String.format(
                    "%s} else {\n",
                    getIndentString(indentation)
            ));
            sb.append(String.format(
                    "%sbyteBuf.writeInt(%s == null ? 0 : %s);\n",
                    getIndentString(indentation + 1),
                    getter,
                    getter
            ));
            sb.append(String.format(
                    "%s}\n",
                    getIndentString(indentation)
            ));
        } else {
            sb.append(String.format(
                    "%sbyteBuf.writeInt%s(%s == null ? 0 : %s);\n",
                    getIndentString(indentation),
                    (field.getByteOrderPreference() == ByteOrderPreference.LE) ? "LE" : "",
                    getter,
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
                    "new Integer(byteBuf.readIntLE())");
            sb.append(getIndentString(indentation));
            sb.append(setter);
            sb.append("\n");
        } else if (field.getByteOrderPreference() == ByteOrderPreference.BE) {
            String setter = getSetter(
                    field,
                    typeName,
                    variableName,
                    field.getName(),
                    "new Integer(byteBuf.readInt())");
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
                    "new Integer(byteBuf.readIntLE())");
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
                    "new Integer(byteBuf.readInt())");
            sb.append(getIndentString(indentation + 1));
            sb.append(setter);
            sb.append("\n");
            sb.append(getIndentString(indentation));
            sb.append("}\n");
        }
    }
}
