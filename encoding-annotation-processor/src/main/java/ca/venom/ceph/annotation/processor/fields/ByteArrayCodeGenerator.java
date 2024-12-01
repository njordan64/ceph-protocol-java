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
import ca.venom.ceph.encoding.annotations.ByteOrderPreference;

import javax.tools.Diagnostic;

public class ByteArrayCodeGenerator extends FieldCodeGenerator {
    private CodeGenerationContext context;

    public ByteArrayCodeGenerator(CodeGenerationContext context) {
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
                "%sif (%s != null) {\n",
                getIndentString(indentation),
                getter
        ));
        sb.append(String.format(
                "%sbyte[] bytesToWrite = %s;\n",
                getIndentString(indentation + 1),
                getter
        ));
        if (field.getByteOrderPreference() != ByteOrderPreference.NONE) {
            sb.append(String.format(
                    "%sboolean needToReverse = %sle;\n",
                    getIndentString(indentation + 1),
                    (field.getByteOrderPreference() == ByteOrderPreference.LE ? "!" : "")
            ));
            sb.append(String.format(
                    "%sif (needToReverse) {\n",
                    getIndentString(indentation + 1)
            ));
            sb.append(String.format(
                    "%sfor (int i = 0; i < bytesToWrite.length / 2; i++) {\n",
                    getIndentString(indentation + 2)
            ));
            sb.append(String.format(
                    "%sbyte b = bytesToWrite[i];\n",
                    getIndentString(indentation + 3)
            ));
            sb.append(String.format(
                    "%sbytesToWrite[i] = bytesToWrite[bytesToWrite.length - 1 - i];\n",
                    getIndentString(indentation + 3)
            ));
            sb.append(String.format(
                    "%sbytesToWrite[bytesToWrite.length - 1 - i] = b;\n",
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
        }

        if (field.isIncludeSize()) {
            if (field.getByteOrderPreference() == ByteOrderPreference.NONE) {
                sb.append(String.format(
                        "%sif (le) {\n",
                        getIndentString(indentation + 1)
                ));
                String functionName;
                switch (field.getSizeLength()) {
                    case 1 -> functionName = "Byte((byte) ";
                    case 2 -> functionName = "ShortLE((short) ";
                    default -> functionName = "IntLE(";
                }
                sb.append(String.format(
                        "%sbyteBuf.write%sbytesToWrite.length);\n",
                        getIndentString(indentation + 2),
                        functionName
                ));
                sb.append(String.format(
                        "%s} else {\n",
                        getIndentString(indentation + 1)
                ));
                switch (field.getSizeLength()) {
                    case 1 -> functionName = "Byte((byte) ";
                    case 2 -> functionName = "Short((short) ";
                    default -> functionName = "Int(";
                }
                sb.append(String.format(
                        "%sbyteBuf.write%sbytesToWrite.length);\n",
                        getIndentString(indentation + 2),
                        functionName
                ));
                sb.append(String.format(
                        "%s}\n",
                        getIndentString(indentation + 1)
                ));
            } else {
                String function;
                switch (field.getSizeLength()) {
                    case 1 -> function = "Byte((byte) ";
                    case 2 -> function = "Short" + ((field.getByteOrderPreference() == ByteOrderPreference.LE) ? "LE" : "");
                    default -> function = "Int" + ((field.getByteOrderPreference() == ByteOrderPreference.LE ? "LE" : ""));
                }
                sb.append(String.format(
                        "%sbyteBuf.write%sbytesToWrite.length);\n",
                        getIndentString(indentation + 2),
                        function
                ));
                sb.append(String.format(
                        "%s}\n",
                        getIndentString(indentation + 1)
                ));
            }
        }

        if (field.getSizeProperty() != null && !field.getSizeProperty().isEmpty()) {
            sb.append(String.format(
                    "%sif (%s.%s > bytesToWrite.length) {\n",
                    getIndentString(indentation + 1),
                    variableName,
                    field.getSizeProperty()
            ));
            sb.append(String.format(
                    "%sbyteBuf.writeBytes(bytesToWrite);\n",
                    getIndentString(indentation + 2)
            ));
            sb.append(String.format(
                    "%sbyteBuf.writeZero(%s.%s - bytesToWrite.length);\n",
                    getIndentString(indentation + 2),
                    variableName,
                    field.getSizeProperty()
            ));
            sb.append(String.format(
                    "%s} else if (%s.%s < bytesToWrite.length) {\n",
                    getIndentString(indentation + 1),
                    variableName,
                    field.getSizeProperty()
            ));
            sb.append(String.format(
                    "%sbyteBuf.writeBytes(bytesToWrite, 0, bytesToWrite.length - %s.%s);\n",
                    getIndentString(indentation + 2),
                    variableName,
                    field.getSizeProperty()
            ));
            sb.append(String.format(
                    "%s} else {\n",
                    getIndentString(indentation + 1)
            ));
            sb.append(String.format(
                    "%sbyteBuf.writeBytes(bytesToWrite);\n",
                    getIndentString(indentation + 2)
            ));
            sb.append(String.format(
                    "%s}",
                    getIndentString(indentation + 1)
            ));
        } else {
            sb.append(String.format(
                    "%sbyteBuf.writeBytes(bytesToWrite);\n",
                    getIndentString(indentation + 1)
            ));
        }

        if (field.isIncludeSize()) {
            sb.append(String.format(
                    "%s} else {\n",
                    getIndentString(indentation)
            ));
            sb.append(String.format(
                    "%sbyteBuf.writeZero(%d);\n",
                    getIndentString(indentation + 1),
                    field.getSizeLength()
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
        if (field.isIncludeSize()) {
            sb.append(String.format(
                    "%sint size;\n",
                    getIndentString(indentation + 1)
            ));
            sb.append(String.format(
                    "%sif (le) {\n",
                    getIndentString(indentation + 1)
            ));
            switch (field.getSizeLength()) {
                case 1 -> sb.append(String.format(
                        "%ssize = byteBuf.readByte();\n",
                        getIndentString(indentation + 2)
                ));
                case 2 -> sb.append(String.format(
                        "%ssize = byteBuf.readShortLE();\n",
                        getIndentString(indentation + 2)
                ));
                default -> sb.append(String.format(
                        "%ssize = byteBuf.readIntLE();\n",
                        getIndentString(indentation + 2)
                ));
            }
            sb.append(String.format(
                    "%s} else {\n",
                    getIndentString(indentation + 1)
            ));
            switch (field.getSizeLength()) {
                case 1 -> sb.append(String.format(
                        "%ssize = byteBuf.readByte();\n",
                        getIndentString(indentation + 2)
                ));
                case 2 -> sb.append(String.format(
                        "%ssize = byteBuf.readShort();\n",
                        getIndentString(indentation + 2)
                ));
                default -> sb.append(String.format(
                        "%ssize = byteBuf.readInt();\n",
                        getIndentString(indentation + 2)
                ));
            }
            sb.append(String.format(
                    "%s}\n",
                    getIndentString(indentation + 1)
            ));
        } else if (field.getEncodingSize() != null) {
            sb.append(String.format(
                    "%sint size = %d;\n",
                    getIndentString(indentation + 1),
                    field.getEncodingSize()
            ));
        } else if (field.getSizeProperty() != null && !field.getSizeProperty().isEmpty()) {
            sb.append(String.format(
                    "%sint size = %s.%s;\n",
                    getIndentString(indentation + 1),
                    variableName,
                    field.getSizeProperty()
            ));
        } else {
            context.getMessager().printMessage(Diagnostic.Kind.ERROR, "Byte array without included size or encoding size");
        }

        sb.append(String.format(
                "%sbyte[] bytes = new byte[size];\n",
                getIndentString(indentation + 1)
        ));
        sb.append(String.format(
                "%sbyteBuf.readBytes(bytes);\n",
                getIndentString(indentation + 1)
        ));

        if (field.getByteOrderPreference() != ByteOrderPreference.NONE) {
            sb.append(String.format(
                    "%sboolean needToReverse = %sle;\n",
                    getIndentString(indentation + 1),
                    (field.getByteOrderPreference() == ByteOrderPreference.LE) ? "!" : ""
            ));
            sb.append(String.format(
                    "%sif (needToReverse) {\n",
                    getIndentString(indentation + 1)
            ));
            sb.append(String.format(
                    "%sfor (int i = 0; i < size / 2; i++) {\n",
                    getIndentString(indentation + 2)
            ));
            sb.append(String.format(
                    "%sbyte b = bytes[i];\n",
                    getIndentString(indentation + 3)
            ));
            sb.append(String.format(
                    "%sbytes[i] = bytes[size - i - 1];\n",
                    getIndentString(indentation + 3)
            ));
            sb.append(String.format(
                    "%sbytes[size - i - 1] = b;\n",
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
        }

        String setter = getSetter(
                field,
                typeName,
                variableName,
                field.getName(),
                "bytes"
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
