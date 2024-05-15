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

public class ListCodeGenerator extends FieldCodeGenerator {
    private CodeGenerationContext context;

    public ListCodeGenerator(CodeGenerationContext context) {
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

        Pattern pattern = Pattern.compile("^java\\.util\\.List<(.+)>$");
        Matcher matcher = pattern.matcher(typeName);
        if (matcher.matches()) {
            String innerType = matcher.group(1);
            sb.append(String.format(
                    "%sfor (%s element : %s) {\n",
                    getIndentString(indentation + 1),
                    innerType,
                    getter
            ));

            FieldCodeGenerator fieldCodeGenerator = getFieldCodeGenerator(context, field, innerType);
            if (fieldCodeGenerator != null) {
                fieldCodeGenerator.generateEncodeJavaCode(
                        sb,
                        field,
                        indentation + 2,
                        "element",
                        innerType
                );
            } else {
                context.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to encode list elements");
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
                "%sint listSize = le ? byteBuf.readIntLE() : byteBuf.readInt();\n",
                getIndentString(indentation + 1)
        ));

        Pattern pattern = Pattern.compile("^java\\.util\\.List<(.+)>$");
        Matcher matcher = pattern.matcher(typeName);
        if (matcher.matches()) {
            context.getImports().add("java.util.List");
            context.getImports().add("java.util.ArrayList");

            String innerType = matcher.group(1);
            sb.append(String.format(
                    "%sList<%s> decodedList = new ArrayList<>();\n",
                    getIndentString(indentation + 1),
                    innerType
            ));
            sb.append(String.format(
                    "%sfor (int i = 0; i < listSize; i++) {\n",
                    getIndentString(indentation + 1)
            ));

            FieldCodeGenerator fieldCodeGenerator = getFieldCodeGenerator(context, field, innerType);
            if (fieldCodeGenerator != null) {
                sb.append(String.format(
                        "%s%s element;\n",
                        getIndentString(indentation + 2),
                        innerType
                ));
                fieldCodeGenerator.generateDecodeJavaCode(
                        sb,
                        field,
                        indentation + 2,
                        "element",
                        innerType
                );
                sb.append(String.format(
                        "%sdecodedList.add(element);\n",
                        getIndentString(indentation + 2)
                ));
            } else {
                context.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to decode list elements");
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
                    "decodedList"
            );
            sb.append(getIndentString(indentation + 1));
            sb.append(setter);
            sb.append("\n");
        } else {
            context.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to determine element type for list");
        }

        sb.append(String.format(
                "%s}\n",
                getIndentString(indentation)
        ));
    }
}
