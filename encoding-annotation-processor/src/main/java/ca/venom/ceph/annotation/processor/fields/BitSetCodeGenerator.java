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

public class BitSetCodeGenerator extends FieldCodeGenerator {
    private CodeGenerationContext context;

    public BitSetCodeGenerator(CodeGenerationContext context) {
        this.context = context;
    }

    @Override
    public void generateEncodeJavaCode(StringBuilder sb,
                                       EncodableField field,
                                       int indentation,
                                       String variableName,
                                       String typeName) {
        context.getImports().add("java.util.BitSet");

        String getter = getGetterName(field, typeName, variableName, field.getName());
        int encodingSize = 1;
        if (field.getEncodingSize() != null) {
            encodingSize = field.getEncodingSize();
        }

        sb.append(String.format(
                "%sif (%s == null) {\n",
                getIndentString(indentation),
                getter
        ));
        sb.append(String.format(
                "%sbyteBuf.writeZero(%d);\n",
                getIndentString(indentation + 1),
                encodingSize
        ));
        sb.append(String.format(
                "%s} else {\n",
                getIndentString(indentation)
        ));
        sb.append(String.format(
                "%sbyte[] bytes = new byte[%d];\n",
                getIndentString(indentation + 1),
                encodingSize
        ));
        sb.append(String.format(
                "%sbyte[] bitsetBytes = %s.toByteArray();\n",
                getIndentString(indentation + 1),
                getter
        ));
        sb.append(String.format(
                "%sif (le) {\n",
                getIndentString(indentation + 1)
        ));
        sb.append(String.format(
                "%sSystem.arraycopy(bitsetBytes, 0, bytes, 0, Math.min(%d, bitsetBytes.length));\n",
                getIndentString(indentation + 2),
                encodingSize
        ));
        sb.append(String.format(
                "%s} else {\n",
                getIndentString(indentation + 1)
        ));
        sb.append(String.format(
                "%sfor (int i = 0; i < Math.min(%d, bitsetBytes.length); i++) {\n",
                getIndentString(indentation + 2),
                encodingSize / 2
        ));
        sb.append(String.format(
                "%sbytes[%d - i - 1] = bitsetBytes[i];\n",
                getIndentString(indentation + 3),
                encodingSize
        ));
        sb.append(String.format(
                "%s}\n",
                getIndentString(indentation + 2)
        ));
        sb.append(String.format(
                "%s}\n",
                getIndentString(indentation + 1)
        ));
        sb.append(String.format(
                "%sbyteBuf.writeBytes(bytes);\n",
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
        if (field.getEncodingSize() != null) {
            context.getImports().add("java.util.BitSet");

            sb.append(String.format(
                    "%s{\n",
                    getIndentString(indentation)
            ));
            sb.append(String.format(
                    "%sbyte[] bytes = new byte[%d];\n",
                    getIndentString(indentation + 1),
                    field.getEncodingSize()
            ));
            sb.append(String.format(
                    "%sbyteBuf.readBytes(bytes);\n",
                    getIndentString(indentation + 1)
            ));
            sb.append(String.format(
                    "%sif (!le) {\n",
                    getIndentString(indentation + 1)
            ));
            sb.append(String.format(
                    "%sfor (int i = 0; i < %d / 2; i++) {\n",
                    getIndentString(indentation + 2),
                    field.getEncodingSize()
            ));
            sb.append(String.format(
                    "%sbyte b = bytes[%d - i - 1];\n",
                    getIndentString(indentation + 3),
                    field.getEncodingSize()
            ));
            sb.append(String.format(
                    "%sbytes[%d - i - 1] = bytes[i];\n",
                    getIndentString(indentation + 3),
                    field.getEncodingSize()
            ));
            sb.append(String.format(
                    "%sbytes[i] = b;\n",
                    getIndentString(indentation + 3)
            ));
            sb.append(String.format(
                    "%s}\n",
                    getIndentString(indentation + 2)
            ));
            sb.append(String.format(
                    "%s}\n",
                    getIndentString(indentation + 1)
            ));

            String setter = getSetter(
                    field,
                    typeName,
                    variableName,
                    field.getName(),
                    "BitSet.valueOf(bytes)"
            );
            sb.append(getIndentString(indentation + 1));
            sb.append(setter);
            sb.append("\n");

            sb.append(String.format(
                    "%s}\n",
                    getIndentString(indentation)
            ));
        } else {
            context.getMessager().printMessage(Diagnostic.Kind.ERROR, "BitSet missing encoding size");
        }
    }
}
