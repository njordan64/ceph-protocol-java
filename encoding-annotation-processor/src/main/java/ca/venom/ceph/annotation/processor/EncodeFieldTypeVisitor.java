/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.annotation.processor;

import ca.venom.ceph.annotation.processor.parser.ParsedField;
import ca.venom.ceph.encoding.annotations.ByteOrderPreference;

import java.util.ArrayList;
import java.util.List;

public class EncodeFieldTypeVisitor implements FieldTypeVisitor<List<CodeLine>, EncodeCodeGenContext> {
    private FieldTypeToStringVisitor typeParamVisitor = new FieldTypeToStringVisitor();

    @Override
    public List<CodeLine> visitDeclaredType(ParsedField.DeclaredFieldType fieldType,
                                            ParsedField field,
                                            EncodeCodeGenContext context) {
        ClassNameSplitter fullClassName = new ClassNameSplitter(fieldType.getClassName());
        return List.of(new CodeLine(context.getIndentation(), String.format(
                "%s.encode(%s, byteBuf, le);",
                fullClassName.getEncoderClassName(),
                context.getValueAccessor()
        )));
    }

    @Override
    public List<CodeLine> visitBooleanType(ParsedField.PrimitiveFieldType fieldType,
                                           ParsedField field,
                                           EncodeCodeGenContext context) {
        int indentation = context.getIndentation();
        return List.of(
                new CodeLine(indentation, String.format(
                        "byteBuf.writeByte(%s ? 1 : 0);",
                        context.getValueAccessor()
                ))
        );
    }

    @Override
    public List<CodeLine> visitByteType(ParsedField.PrimitiveFieldType fieldType,
                                        ParsedField field,
                                        EncodeCodeGenContext context) {
        int indentation = context.getIndentation();
        return List.of(
                new CodeLine(indentation, String.format(
                        "byteBuf.writeByte(%s);",
                        context.getValueAccessor()
                ))
        );
    }

    @Override
    public List<CodeLine> visitIntType(ParsedField.PrimitiveFieldType fieldType,
                                       ParsedField field,
                                       EncodeCodeGenContext context) {
        int indentation = context.getIndentation();
        if (field.getByteOrderPreference() == ByteOrderPreference.NONE) {
            return List.of(
                    new CodeLine(indentation, "if (le) {"),
                    new CodeLine(indentation + 1, String.format(
                            "byteBuf.writeIntLE(%s);",
                            context.getValueAccessor()
                    )),
                    new CodeLine(indentation, "} else {"),
                    new CodeLine(indentation + 1, String.format(
                            "byteBuf.writeInt(%s);",
                            context.getValueAccessor()
                    )),
                    new CodeLine(indentation, "}")
            );
        } else {
            String suffix = field.getByteOrderPreference() == ByteOrderPreference.LE ? "LE" : "";
            return List.of(
                    new CodeLine(indentation, String.format(
                            "byteBuf.writeInt%s(%s);",
                            suffix,
                            context.getValueAccessor()
                    ))
            );
        }
    }

    @Override
    public List<CodeLine> visitLongType(ParsedField.PrimitiveFieldType fieldType,
                                        ParsedField field,
                                        EncodeCodeGenContext context) {
        int indentation = context.getIndentation();
        if (field.getByteOrderPreference() == ByteOrderPreference.NONE) {
            return List.of(
                    new CodeLine(indentation, "if (le) {"),
                    new CodeLine(indentation + 1, String.format(
                            "byteBuf.writeLongLE(%s);",
                            context.getValueAccessor()
                    )),
                    new CodeLine(indentation, "} else {"),
                    new CodeLine(indentation + 1, String.format(
                            "byteBuf.writeLong(%s);",
                            context.getValueAccessor()
                    )),
                    new CodeLine(indentation, "}")
            );
        } else {
            String suffix = field.getByteOrderPreference() == ByteOrderPreference.LE ? "LE" : "";
            return List.of(
                    new CodeLine(indentation, String.format(
                            "byteBuf.writeLong%s(%s);",
                            suffix,
                            context.getValueAccessor()
                    ))
            );
        }
    }

    @Override
    public List<CodeLine> visitShortType(ParsedField.PrimitiveFieldType fieldType,
                                         ParsedField field,
                                         EncodeCodeGenContext context) {
        int indentation = context.getIndentation();
        if (field.getByteOrderPreference() == ByteOrderPreference.NONE) {
            return List.of(
                    new CodeLine(indentation, "if (le) {"),
                    new CodeLine(indentation + 1, String.format(
                            "byteBuf.writeShortLE(%s);",
                            context.getValueAccessor()
                    )),
                    new CodeLine(indentation, "} else {"),
                    new CodeLine(indentation + 1, String.format(
                            "byteBuf.writeShort(%s);",
                            context.getValueAccessor()
                    )),
                    new CodeLine(indentation, "}")
            );
        } else {
            String suffix = field.getByteOrderPreference() == ByteOrderPreference.LE ? "LE" : "";
            return List.of(
                    new CodeLine(indentation, String.format(
                            "byteBuf.writeShort%s(%s);",
                            suffix,
                            context.getValueAccessor()
                    ))
            );
        }
    }

    @Override
    public List<CodeLine> visitWrappedBooleanType(ParsedField.WrappedPrimitiveFieldType fieldType,
                                                  ParsedField field,
                                                  EncodeCodeGenContext context) {
        int indentation = context.getIndentation();
        return List.of(
                new CodeLine(indentation, String.format(
                        "byteBuf.writeByte(Boolean.TRUE.equals(%s) ? 1 : 0);",
                        context.getValueAccessor()
                ))
        );
    }

    @Override
    public List<CodeLine> visitWrappedByteType(ParsedField.WrappedPrimitiveFieldType fieldType,
                                               ParsedField field,
                                               EncodeCodeGenContext context) {
        int indentation = context.getIndentation();
        return List.of(
                new CodeLine(indentation, String.format(
                        "if (%s == null) {",
                        context.getValueAccessor()
                )),
                new CodeLine(indentation + 1, "byteBuf.writeByte(0);"),
                new CodeLine(indentation, "} else {"),
                new CodeLine(indentation + 1, String.format(
                        "byteBuf.writeByte(%s);",
                        context.getValueAccessor()
                )),
                new CodeLine(indentation, "}")
        );
    }

    @Override
    public List<CodeLine> visitWrappedIntType(ParsedField.WrappedPrimitiveFieldType fieldType,
                                              ParsedField field,
                                              EncodeCodeGenContext context) {
        int indentation = context.getIndentation();
        if (field.getByteOrderPreference() == ByteOrderPreference.NONE) {
            return List.of(
                    new CodeLine(indentation, "if (le) {"),
                    new CodeLine(indentation + 1, String.format(
                            "byteBuf.writeIntLE(%s == null ? 0 : %s);",
                            context.getValueAccessor(),
                            context.getValueAccessor()
                    )),
                    new CodeLine(indentation, "} else {"),
                    new CodeLine(indentation + 1, String.format(
                            "byteBuf.writeInt(%s == null ? 0 : %s);",
                            context.getValueAccessor(),
                            context.getValueAccessor()
                    )),
                    new CodeLine(indentation, "}")
            );
        } else {
            String suffix = field.getByteOrderPreference() == ByteOrderPreference.LE ? "LE" : "";
            return List.of(
                    new CodeLine(indentation, String.format(
                            "byteBuf.writeInt%s(%s == null ? 0 : %s);",
                            suffix,
                            context.getValueAccessor(),
                            context.getValueAccessor()
                    ))
            );
        }
    }

    @Override
    public List<CodeLine> visitWrappedLongType(ParsedField.WrappedPrimitiveFieldType fieldType,
                                               ParsedField field,
                                               EncodeCodeGenContext context) {
        int indentation = context.getIndentation();
        if (field.getByteOrderPreference() == ByteOrderPreference.NONE) {
            return List.of(
                    new CodeLine(indentation, "if (le) {"),
                    new CodeLine(indentation + 1, String.format(
                            "byteBuf.writeLongLE(%s == null ? 0L : %s);",
                            context.getValueAccessor(),
                            context.getValueAccessor()
                    )),
                    new CodeLine(indentation, "} else {"),
                    new CodeLine(indentation + 1, String.format(
                            "byteBuf.writeLong(%s == null ? 0L : %s);",
                            context.getValueAccessor(),
                            context.getValueAccessor()
                    )),
                    new CodeLine(indentation, "}")
            );
        } else {
            String suffix = field.getByteOrderPreference() == ByteOrderPreference.LE ? "LE" : "";
            return List.of(
                    new CodeLine(indentation, String.format(
                            "byteBuf.writeLong%s(%s == null ? 0L : %s);",
                            suffix,
                            context.getValueAccessor(),
                            context.getValueAccessor()
                    ))
            );
        }
    }

    @Override
    public List<CodeLine> visitWrappedShortType(ParsedField.WrappedPrimitiveFieldType fieldType,
                                                ParsedField field,
                                                EncodeCodeGenContext context) {
        int indentation = context.getIndentation();
        if (field.getByteOrderPreference() == ByteOrderPreference.NONE) {
            return List.of(
                    new CodeLine(indentation, "if (le) {"),
                    new CodeLine(indentation + 1, String.format(
                            "byteBuf.writeShortLE(%s == null ? (short) 0 : %s);",
                            context.getValueAccessor(),
                            context.getValueAccessor()
                    )),
                    new CodeLine(indentation, "} else {"),
                    new CodeLine(indentation + 1, String.format(
                            "byteBuf.writeShort(%s == null ? (short) 0 : %s);",
                            context.getValueAccessor(),
                            context.getValueAccessor()
                    )),
                    new CodeLine(indentation, "}")
            );
        } else {
            String suffix = field.getByteOrderPreference() == ByteOrderPreference.LE ? "LE" : "";
            return List.of(
                    new CodeLine(indentation, String.format(
                            "byteBuf.writeShort%s(%s == null ? (short) 0 : %s);",
                            suffix,
                            context.getValueAccessor(),
                            context.getValueAccessor()
                    ))
            );
        }
    }

    @Override
    public List<CodeLine> visitStringType(ParsedField.StringFieldType fieldType,
                                          ParsedField field,
                                          EncodeCodeGenContext context) {
        int indentation = context.getIndentation();
        return List.of(
                new CodeLine(indentation, String.format(
                        "if (%s == null) {",
                        context.getValueAccessor()
                )),
                new CodeLine(indentation + 1, "byteBuf.writeZero(4);"),
                new CodeLine(indentation, "} else {"),
                new CodeLine(indentation + 1, String.format(
                        "byte[] strBytes = %s.getBytes(StandardCharsets.UTF_8);",
                        context.getValueAccessor()
                )),
                new CodeLine(indentation + 1, "if (le) {"),
                new CodeLine(indentation + 2, "byteBuf.writeIntLE(strBytes.length);"),
                new CodeLine(indentation + 1, "} else {"),
                new CodeLine(indentation + 2, "byteBuf.writeInt(strBytes.length);"),
                new CodeLine(indentation + 1, "}"),
                new CodeLine(indentation + 1, "byteBuf.writeBytes(strBytes);"),
                new CodeLine(indentation, "}")
        );
    }

    @Override
    public List<CodeLine> visitBitSetType(ParsedField.BitSetFieldType fieldType,
                                          ParsedField field,
                                          EncodeCodeGenContext context) {
        int indentation = context.getIndentation();

        int encodingSize = 1;
        if (field.getEncodingSize() != null) {
            encodingSize = field.getEncodingSize();
        }

        String bytesVariableName = "bytes" + (indentation + 1);
        String bitsetBytesVariableName = "bitsetBytes" + (indentation + 1);
        String iVariableName = "i" + (indentation + 2);
        return List.of(
                new CodeLine(indentation, String.format(
                        "if (%s == null) {",
                        context.getValueAccessor()
                )),
                new CodeLine(indentation + 1, String.format(
                        "byteBuf.writeZero(%d);",
                        encodingSize
                )),
                new CodeLine(indentation, "} else {"),
                new CodeLine(indentation + 1, String.format(
                        "byte[] %s = new byte[%d];",
                        bytesVariableName,
                        encodingSize
                )),
                new CodeLine(indentation + 1, String.format(
                        "byte[] %s = %s.toByteArray();",
                        bitsetBytesVariableName,
                        context.getValueAccessor()
                )),
                new CodeLine(indentation + 1, "if (le) {"),
                new CodeLine(indentation + 2, String.format(
                        "System.arraycopy(%s, 0, %s, 0, Math.min(%d, %s.length));",
                        bitsetBytesVariableName,
                        bytesVariableName,
                        encodingSize,
                        bitsetBytesVariableName
                )),
                new CodeLine(indentation + 1, "} else {"),
                new CodeLine(indentation + 2, String.format(
                        "for (int %s = 0; %s < Math.min(%d, %s.length); %s++) {",
                        iVariableName,
                        iVariableName,
                        encodingSize,
                        bitsetBytesVariableName,
                        iVariableName
                )),
                new CodeLine(indentation + 3, String.format(
                        "%s[%d - %s - 1] = %s[%s];",
                        bytesVariableName,
                        encodingSize,
                        iVariableName,
                        bitsetBytesVariableName,
                        iVariableName
                )),
                new CodeLine(indentation + 2, "}"),
                new CodeLine(indentation + 1, "}"),
                new CodeLine(indentation + 1, String.format(
                        "byteBuf.writeBytes(%s);",
                        bytesVariableName
                )),
                new CodeLine(indentation, "}")
        );
    }

    @Override
    public List<CodeLine> visitByteArrayType(ParsedField.ByteArrayFieldType fieldType,
                                             ParsedField field,
                                             EncodeCodeGenContext context) {
        int indentation = context.getIndentation();
        List<CodeLine> codeLines = new ArrayList<>();

        codeLines.add(new CodeLine(indentation, String.format(
                "if (%s != null) {",
                context.getValueAccessor()
        )));
        codeLines.add(new CodeLine(indentation + 1, String.format(
                "byte[] bytesToWrite = %s;",
                context.getValueAccessor()
        )));

        if (field.getByteOrderPreference() != ByteOrderPreference.NONE) {
            codeLines.add(new CodeLine(indentation + 1, String.format(
                    "boolean needToReverse = %sle;",
                    field.getByteOrderPreference() == ByteOrderPreference.LE ? "!" : ""
            )));
            codeLines.add(new CodeLine(indentation + 1, "if (needToReverse) {"));
            codeLines.add(new CodeLine(
                    indentation + 2,
                    "for (int i = 0; i < bytesToWrite.length / 2; i++) {"
            ));
            codeLines.add(new CodeLine(indentation + 3, "byte b = bytesToWrite[i];"));
            codeLines.add(new CodeLine(
                    indentation + 3,
                    "bytesToWrite[i] = bytesToWrite[bytesToWrite.length - 1 - i];"
            ));
            codeLines.add(new CodeLine(
                    indentation + 3,
                    "bytesToWrite[bytesToWrite.length - 1 - i] = b;"
            ));
            codeLines.add(new CodeLine(indentation + 2, "}"));
            codeLines.add(new CodeLine(indentation + 1, "}"));
        }

        if (field.isIncludeTypeSize()) {
            if (field.getByteOrderPreference() == ByteOrderPreference.NONE) {
                codeLines.add(new CodeLine(indentation + 1, "if (le) {"));

                String functionName;
                switch (field.getSizeLength()) {
                    case 1 -> functionName = "Byte((byte) ";
                    case 2 -> functionName = "ShortLE((short) ";
                    default -> functionName = "IntLE(";
                }
                codeLines.add(new CodeLine(indentation + 2, String.format(
                        "byteBuf.write%sbytesToWrite.length);",
                        functionName
                )));

                codeLines.add(new CodeLine(indentation + 1, "} else {"));

                switch (field.getSizeLength()) {
                    case 1 -> functionName = "Byte((byte) ";
                    case 2 -> functionName = "Short((short) ";
                    default -> functionName = "Int(";
                }
                codeLines.add(new CodeLine(indentation + 2, String.format(
                        "byteBuf.write%sbytesToWrite.length);",
                        functionName
                )));

                codeLines.add(new CodeLine(indentation + 1, "}"));
            } else {
                String function;
                switch (field.getSizeLength()) {
                    case 1 -> function = "Byte((byte) ";
                    case 2 ->
                            function = "Short" + ((field.getByteOrderPreference() == ByteOrderPreference.LE) ? "LE" : "");
                    default ->
                            function = "Int" + ((field.getByteOrderPreference() == ByteOrderPreference.LE ? "LE" : ""));
                }
                codeLines.add(new CodeLine(indentation + 1, String.format(
                        "byteBuf.write%sbytesToWrite.length);",
                        function
                )));
            }
        }

        if (field.getSizeProperty() != null && !field.getSizeProperty().isEmpty()) {
            codeLines.add(new CodeLine(indentation + 1, String.format(
                    "if (toEncode.%s > bytesToWrite.length) {",
                    field.getSizeProperty()
            )));
            codeLines.add(new CodeLine(indentation + 2, "byteBuf.writeBytes(bytesToWrite);"));
            codeLines.add(new CodeLine(indentation + 2, String.format(
                    "byteBuf.writeZero(toEncode.%s - bytesToWrite.length);",
                    field.getSizeProperty()
            )));
            codeLines.add(new CodeLine(indentation + 2, String.format(
                    "} else if (toEncode.%s < bytesToWrite.length) {",
                    field.getSizeProperty()
            )));
            codeLines.add(new CodeLine(indentation + 2, String.format(
                    "byteBuf.writeBytes(bytesToWrite, 0, bytesToWrite.length - toEncode.%s);",
                    field.getSizeProperty()
            )));
            codeLines.add(new CodeLine(indentation + 1, "} else {"));
            codeLines.add(new CodeLine(indentation + 2, "byteBuf.writeBytes(bytesToWrite);"));
            codeLines.add(new CodeLine(indentation + 1, "}"));
        } else {
            codeLines.add(new CodeLine(indentation + 1, "byteBuf.writeBytes(bytesToWrite);"));
        }

        if (field.isIncludeTypeSize()) {
            codeLines.add(new CodeLine(indentation, "} else {"));
            codeLines.add(new CodeLine(indentation + 1, String.format(
                    "byteBuf.writeZero(%d);",
                    field.getSizeLength()
            )));
        }

        codeLines.add(new CodeLine(indentation, "}"));
        return codeLines;
    }

    @Override
    public List<CodeLine> visitEnumType(ParsedField.EnumFieldType fieldType,
                                        ParsedField field,
                                        EncodeCodeGenContext context) {
        int indentation = context.getIndentation();
        List<CodeLine> codeLines = new ArrayList<>();

        int encodingSize = 1;
        if (field.getEncodingSize() != null) {
            encodingSize = field.getEncodingSize();
        }

        if (encodingSize == 1) {
            codeLines.add(new CodeLine(indentation, String.format(
                    "byteBuf.writeByte((byte) %s.getValueInt());",
                    context.getValueAccessor()
            )));
        } else {
            codeLines.add(new CodeLine(indentation, "if (le) {"));
            codeLines.add(new CodeLine(indentation + 1, String.format(
                    "byteBuf.write%s%s.getValueInt());",
                    (encodingSize == 2) ? "ShortLE((short) " : "IntLE(",
                    context.getValueAccessor()
            )));
            codeLines.add(new CodeLine(indentation, "} else {"));
            codeLines.add(new CodeLine(indentation + 1, String.format(
                    "byteBuf.write%s%s.getValueInt());",
                    (encodingSize == 2) ? "Short((short) " : "Int(",
                    context.getValueAccessor()
            )));
            codeLines.add(new CodeLine(indentation, "}"));
        }

        return codeLines;
    }

    @Override
    public List<CodeLine> visitListType(ParsedField.ListFieldType fieldType,
                                        ParsedField field,
                                        EncodeCodeGenContext context) {
        int indentation = context.getIndentation();
        List<CodeLine> codeLines = new ArrayList<>();

        codeLines.add(new CodeLine(indentation, String.format(
                "if (%s == null) {",
                context.getValueAccessor()
        )));
        codeLines.add(new CodeLine(indentation + 1, "byteBuf.writeZero(4);"));
        codeLines.add(new CodeLine(indentation, "} else {"));
        codeLines.add(new CodeLine(indentation + 1, "if (le) {"));
        codeLines.add(new CodeLine(indentation + 2, String.format(
                "byteBuf.writeIntLE(%s.size());",
                context.getValueAccessor()
        )));
        codeLines.add(new CodeLine(indentation + 1, "} else {"));
        codeLines.add(new CodeLine(indentation + 2, String.format(
                "byteBuf.writeInt(%s.size());",
                context.getValueAccessor()
        )));
        codeLines.add(new CodeLine(indentation + 1, "}"));

        codeLines.add(new CodeLine(indentation + 1, String.format(
                "for (%s element : %s) {",
                fieldType.getElementFieldType().accept(typeParamVisitor, null, null),
                context.getValueAccessor()
        )));
        EncodeCodeGenContext elementContext = new EncodeCodeGenContext(
                indentation + 2,
                "element",
                context.getParsedClasses());
        codeLines.addAll(fieldType.getElementFieldType().accept(this, field, elementContext));
        codeLines.add(new CodeLine(indentation + 1, "}"));
        codeLines.add(new CodeLine(indentation, "}"));

        return codeLines;
    }

    @Override
    public List<CodeLine> visitSetType(ParsedField.SetFieldType fieldType,
                                       ParsedField field,
                                       EncodeCodeGenContext context) {
        int indentation = context.getIndentation();
        List<CodeLine> codeLines = new ArrayList<>();

        codeLines.add(new CodeLine(indentation, String.format(
                "if (%s == null) {",
                context.getValueAccessor()
        )));
        codeLines.add(new CodeLine(indentation + 1, "byteBuf.writeZero(4);"));
        codeLines.add(new CodeLine(indentation, "} else {"));
        codeLines.add(new CodeLine(indentation + 1, "if (le) {"));
        codeLines.add(new CodeLine(indentation + 2, String.format(
                "byteBuf.writeIntLE(%s.size());",
                context.getValueAccessor()
        )));
        codeLines.add(new CodeLine(indentation + 1, "} else {"));
        codeLines.add(new CodeLine(indentation + 2, String.format(
                "byteBuf.writeInt(%s.size());",
                context.getValueAccessor()
        )));
        codeLines.add(new CodeLine(indentation + 1, "}"));

        codeLines.add(new CodeLine(indentation + 1, String.format(
                "for (%s element : %s) {",
                fieldType.getElementFieldType().accept(typeParamVisitor, null, null),
                context.getValueAccessor()
        )));
        EncodeCodeGenContext elementContext = new EncodeCodeGenContext(
                indentation + 2,
                "element",
                context.getParsedClasses());
        codeLines.addAll(fieldType.getElementFieldType().accept(this, field, elementContext));
        codeLines.add(new CodeLine(indentation + 1, "}"));
        codeLines.add(new CodeLine(indentation, "}"));

        return codeLines;
    }

    @Override
    public List<CodeLine> visitMapType(ParsedField.MapFieldType fieldType,
                                       ParsedField field,
                                       EncodeCodeGenContext context) {
        int indentation = context.getIndentation();
        List<CodeLine> codeLines = new ArrayList<>();

        codeLines.add(new CodeLine(indentation, String.format(
                "if (%s == null) {",
                context.getValueAccessor()
        )));
        codeLines.add(new CodeLine(indentation + 1, "byteBuf.writeZero(4);"));
        codeLines.add(new CodeLine(indentation, "} else {"));
        codeLines.add(new CodeLine(indentation + 1, "if (le) {"));
        codeLines.add(new CodeLine(indentation + 2, String.format(
                "byteBuf.writeIntLE(%s.size());",
                context.getValueAccessor()
        )));
        codeLines.add(new CodeLine(indentation + 1, "} else {"));
        codeLines.add(new CodeLine(indentation + 2, String.format(
                "byteBuf.writeInt(%s.size());",
                context.getValueAccessor()
        )));
        codeLines.add(new CodeLine(indentation + 1, "}"));

        String entryVariableName = String.format("entry%d", (indentation + 2));
        codeLines.add(new CodeLine(indentation + 1, String.format(
                "for (Map.Entry<%s, %s> %s : %s.entrySet()) {",
                fieldType.getKeyFieldType().accept(typeParamVisitor, null, null),
                fieldType.getValueFieldType().accept(typeParamVisitor, null, null),
                entryVariableName,
                context.getValueAccessor()
        )));
        EncodeCodeGenContext keyContext = new EncodeCodeGenContext(
                indentation + 2,
                String.format(
                        "%s.getKey()",
                        entryVariableName
                ),
                context.getParsedClasses()
        );
        codeLines.addAll(fieldType.getKeyFieldType().accept(this, field, keyContext));
        EncodeCodeGenContext valueContext = new EncodeCodeGenContext(
                indentation + 2,
                String.format(
                        "%s.getValue()",
                        entryVariableName
                ),
                context.getParsedClasses()
        );
        codeLines.addAll(fieldType.getValueFieldType().accept(this, field, valueContext));
        codeLines.add(new CodeLine(indentation + 1, "}"));
        codeLines.add(new CodeLine(indentation, "}"));

        return codeLines;
    }
}
