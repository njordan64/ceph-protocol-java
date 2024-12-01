/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
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
            int itemNum = context.incrementMapNesting();
            String itemName = "item" + itemNum;
            sb.append(String.format(
                    "%sfor (Map.Entry<%s, %s> %s : %s.entrySet()) {\n",
                    getIndentString(indentation + 1),
                    keyType,
                    valueType,
                    itemName,
                    getter
            ));
            String keyName = "key" + itemNum;
            sb.append(String.format(
                    "%s%s %s = %s.getKey();\n",
                    getIndentString(indentation + 2),
                    keyType,
                    keyName,
                    itemName
            ));
            String valueName = "value" + itemNum;
            sb.append(String.format(
                    "%s%s %s = %s.getValue();\n",
                    getIndentString(indentation + 2),
                    valueType,
                    valueName,
                    itemName
            ));

            FieldCodeGenerator keyCodeGenerator = getFieldCodeGenerator(context, field, keyType);
            FieldCodeGenerator valueCodeGenerator = getFieldCodeGenerator(context, field, valueType);
            if (keyCodeGenerator != null && valueCodeGenerator != null) {
                keyCodeGenerator.generateEncodeJavaCode(
                        sb,
                        field,
                        indentation + 2,
                        keyName,
                        keyType
                );
                valueCodeGenerator.generateEncodeJavaCode(
                        sb,
                        field,
                        indentation + 2,
                        valueName,
                        valueType
                );
            } else {
                context.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to encode key/value type");
            }

            sb.append(String.format(
                    "%s}\n",
                    getIndentString(indentation + 1)
            ));
            context.decrementMapNesting();
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
        int itemNum = context.incrementMapNesting();
        String mapSizeName = "mapSize" + itemNum;
        sb.append(String.format(
                "%sint %s = le ? byteBuf.readIntLE() : byteBuf.readInt();\n",
                getIndentString(indentation + 1),
                mapSizeName
        ));

        Pattern pattern = Pattern.compile("^java\\.util\\.Map<([^,]+)\\s*,\\s*(.*)>$");
        Matcher matcher = pattern.matcher(typeName);
        if (matcher.matches()) {
            context.getImports().add("java.util.Map");
            context.getImports().add("java.util.HashMap");

            String keyType = matcher.group(1);
            String valueType = matcher.group(2);
            String mapName = "decodedMap" + itemNum;
            sb.append(String.format(
                    "%sMap<%s, %s> %s = new java.util.HashMap<>();\n",
                    getIndentString(indentation + 1),
                    keyType,
                    valueType,
                    mapName
            ));
            String iteratorName = "i" + itemNum;
            sb.append(String.format(
                    "%sfor (int %s = 0; %s < %s; %s++) {\n",
                    getIndentString(indentation + 1),
                    iteratorName,
                    iteratorName,
                    mapSizeName,
                    iteratorName
            ));

            FieldCodeGenerator keyCodeGenerator = getFieldCodeGenerator(context, field, keyType);
            FieldCodeGenerator valueCodeGenerator = getFieldCodeGenerator(context, field, valueType);
            if (keyCodeGenerator != null && valueCodeGenerator != null) {
                String keyName = "key" + itemNum;
                sb.append(String.format(
                        "%s%s %s;\n",
                        getIndentString(indentation + 2),
                        keyType,
                        keyName
                ));
                keyCodeGenerator.generateDecodeJavaCode(
                        sb,
                        field,
                        indentation + 2,
                        keyName,
                        keyType
                );
                String valueName = "value" + itemNum;
                sb.append(String.format(
                        "%s%s %s;\n",
                        getIndentString(indentation + 2),
                        valueType,
                        valueName
                ));
                valueCodeGenerator.generateDecodeJavaCode(
                        sb,
                        field,
                        indentation + 2,
                        valueName,
                        valueType
                );
                sb.append(String.format(
                        "%s%s.put(%s, %s);\n",
                        getIndentString(indentation + 2),
                        mapName,
                        keyName,
                        valueName
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
                    mapName
            );
            sb.append(getIndentString(indentation + 1));
            sb.append(setter);
            sb.append("\n");
        } else {
            context.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to determine key/value types for map");
        }

        context.decrementMapNesting();

        sb.append(String.format(
                "%s}\n",
                getIndentString(indentation)
        ));
    }
}
