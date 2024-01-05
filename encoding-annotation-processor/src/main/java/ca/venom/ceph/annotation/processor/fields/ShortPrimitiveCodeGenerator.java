package ca.venom.ceph.annotation.processor.fields;

import ca.venom.ceph.annotation.processor.CodeGenerationContext;
import ca.venom.ceph.annotation.processor.EncodableField;
import ca.venom.ceph.encoding.annotations.ByteOrderPreference;

public class ShortPrimitiveCodeGenerator extends FieldCodeGenerator {
    private CodeGenerationContext context;

    public ShortPrimitiveCodeGenerator(CodeGenerationContext context) {
        this.context = context;
    }

    @Override
    public void generateEncodeJavaCode(StringBuilder sb,
                                       EncodableField field,
                                       int indentation,
                                       String variableName,
                                       String typeName) {
        String getter = getGetterName(field, typeName, variableName, field.getName());

        switch (field.getByteOrderPreference()) {
            case NONE -> {
                sb.append(String.format(
                        "%sif (le) {\n",
                        getIndentString(indentation)
                ));
                sb.append(String.format(
                        "%sbyteBuf.writeShortLE(%s);\n",
                        getIndentString(indentation + 1),
                        getter
                ));
                sb.append(String.format(
                        "%s} else {\n",
                        getIndentString(indentation)
                ));
                sb.append(String.format(
                        "%sbyteBuf.writeShort(%s);\n",
                        getIndentString(indentation + 1),
                        getter
                ));
                sb.append(String.format(
                        "%s}\n",
                        getIndentString(indentation)
                ));
            }
            case LE -> {
                sb.append(String.format(
                        "%sbyteBuf.writeShortLE(%s);\n",
                        getIndentString(indentation),
                        getter
                ));
            }
            case BE -> {
                sb.append(String.format(
                        "%sbyteBuf.writeShort(%s);\n",
                        getIndentString(indentation),
                        getter
                ));
            }
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
                    "byteBuf.readShortLE()");
            sb.append(getIndentString(indentation));
            sb.append(setter);
            sb.append("\n");
        } else if (field.getByteOrderPreference() == ByteOrderPreference.BE) {
            String setter = getSetter(
                    field,
                    typeName,
                    variableName,
                    field.getName(),
                    "byteBuf.readShort()");
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
                    "byteBuf.readShortLE()");
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
                    "byteBuf.readShort()");
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
