package ca.venom.ceph.annotation.processor.fields;

import ca.venom.ceph.annotation.processor.CodeGenerationContext;
import ca.venom.ceph.annotation.processor.EncodableField;

import javax.tools.Diagnostic;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapCodeGenerator extends FieldCodeGenerator {
    private CodeGenerationContext context;

    public MapCodeGenerator(CodeGenerationContext context) {
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
                "%sbyteBuf.writeZero(4);\n",
                getIndentString(indentation + 1)
        ));
        sb.append(String.format(
                "%s} else {\n",
                getIndentString(indentation)
        ));
        sb.append(String.format(
                "%sif (le) {\n",
                getIndentString(indentation + 1)
        ));
        sb.append(String.format(
                "%sbyteBuf.writeIntLE(%s.size());\n",
                getIndentString(indentation + 2),
                getter
        ));
        sb.append(String.format(
                "%s} else {\n",
                getIndentString(indentation + 1)
        ));
        sb.append(String.format(
                "%sbyteBuf.writeInt(%s.size());\n",
                getIndentString(indentation + 2),
                getter
        ));
        sb.append(String.format(
                "%s}\n",
                getIndentString(indentation + 1)
        ));

        Pattern pattern = Pattern.compile("^java\\.util\\.Map<([^,]+)\s*,\s*(.+)>$");
        Matcher matcher = pattern.matcher(typeName);
        if (matcher.matches()) {
            context.getImports().add("java.util.Map");

            String keyType = matcher.group(1);
            String valueType = matcher.group(2);
            sb.append(String.format(
                    "%sfor (Map.Entry<%s, %s> item : %s.entrySet()) {\n",
                    getIndentString(indentation + 1),
                    keyType,
                    valueType,
                    getter
            ));
            sb.append(String.format(
                    "%s%s key = item.getKey();\n",
                    getIndentString(indentation + 2),
                    keyType
            ));
            sb.append(String.format(
                    "%s%s value = item.getValue();\n",
                    getIndentString(indentation + 2),
                    valueType
            ));

            FieldCodeGenerator keyCodeGenerator = getFieldCodeGenerator(context, field, keyType);
            FieldCodeGenerator valueCodeGenerator = getFieldCodeGenerator(context, field, valueType);
            if (keyCodeGenerator != null && valueCodeGenerator != null) {
                keyCodeGenerator.generateEncodeJavaCode(
                        sb,
                        field,
                        indentation + 2,
                        "key",
                        keyType
                );
                valueCodeGenerator.generateEncodeJavaCode(
                        sb,
                        field,
                        indentation + 2,
                        "value",
                        valueType
                );
            } else {
                context.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to encode key/value type");
            }

            sb.append(String.format(
                    "%s}\n",
                    getIndentString(indentation + 1)
            ));
        }

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
                "%sint mapSize = le ? byteBuf.readIntLE() : byteBuf.readInt();\n",
                getIndentString(indentation + 1)
        ));

        Pattern pattern = Pattern.compile("^java\\.util\\.Map<([^,]+)\\s*,\\s*(.*)>$");
        Matcher matcher = pattern.matcher(typeName);
        if (matcher.matches()) {
            context.getImports().add("java.util.Map");
            context.getImports().add("java.util.HashMap");

            String keyType = matcher.group(1);
            String valueType = matcher.group(2);
            sb.append(String.format(
                    "%sMap<%s, %s> decodedMap = new java.util.HashMap<>();\n",
                    getIndentString(indentation + 1),
                    keyType,
                    valueType
            ));
            sb.append(String.format(
                    "%sfor (int i = 0; i < mapSize; i++) {\n",
                    getIndentString(indentation + 1)
            ));

            FieldCodeGenerator keyCodeGenerator = getFieldCodeGenerator(context, field, keyType);
            FieldCodeGenerator valueCodeGenerator = getFieldCodeGenerator(context, field, valueType);
            if (keyCodeGenerator != null && valueCodeGenerator != null) {
                sb.append(String.format(
                        "%s%s key;\n",
                        getIndentString(indentation + 2),
                        keyType
                ));
                keyCodeGenerator.generateDecodeJavaCode(
                        sb,
                        field,
                        indentation + 2,
                        "key",
                        keyType
                );
                sb.append(String.format(
                        "%s%s value;\n",
                        getIndentString(indentation + 2),
                        valueType
                ));
                valueCodeGenerator.generateDecodeJavaCode(
                        sb,
                        field,
                        indentation + 2,
                        "value",
                        valueType
                );
                sb.append(String.format(
                        "%sdecodedMap.put(key, value);\n",
                        getIndentString(indentation + 2)
                ));
            } else {
                context.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to decode key/value type");
            }
            sb.append(String.format(
                    "%s}\n",
                    getIndentString(indentation + 1)
            ));
            String setter = getSetter(
                    field,
                    typeName,
                    variableName,
                    field.getName(),
                    "decodedMap"
            );
            sb.append(getIndentString(indentation + 1));
            sb.append(setter);
            sb.append("\n");
        } else {
            context.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to determine key/value types for map");
        }

        sb.append(String.format(
                "%s}\n",
                getIndentString(indentation)
        ));
    }
}
