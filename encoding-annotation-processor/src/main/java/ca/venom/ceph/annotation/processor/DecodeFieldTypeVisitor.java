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

import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;

public class DecodeFieldTypeVisitor implements FieldTypeVisitor<List<CodeLine>, DecodeCodeGenContext> {
    private FieldTypeToStringVisitor typeParamVisitor = new FieldTypeToStringVisitor();

    @Override
    public List<CodeLine> visitDeclaredType(ParsedField.DeclaredFieldType fieldType,
                                            ParsedField field,
                                            DecodeCodeGenContext context) {
        ClassNameSplitter fullClassName = new ClassNameSplitter(fieldType.getClassName());

        String typeCodeParam = "";
        if (field.getParameterTypeValue() != null) {
            typeCodeParam = String.format(", decoded.%s", field.getParameterTypeValue());
        }

        return List.of(
                new CodeLine(context.getIndentation(), String.format(
                        "%s;",
                        context.getValueSetString(
                                String.format(
                                        "%s.decode(byteBuf, le%s)",
                                        fullClassName.getEncoderClassName(),
                                        typeCodeParam
                                )
                        )
                ))
        );
    }

    @Override
    public List<CodeLine> visitBooleanType(ParsedField.PrimitiveFieldType fieldType,
                                           ParsedField field,
                                           DecodeCodeGenContext context) {
        return List.of(
                new CodeLine(context.getIndentation(), String.format(
                        "%s;",
                        context.getValueSetString("byteBuf.readByte() > 0")
                ))
        );
    }

    @Override
    public List<CodeLine> visitByteType(ParsedField.PrimitiveFieldType fieldType,
                                        ParsedField field,
                                        DecodeCodeGenContext context) {
        return List.of(
                new CodeLine(context.getIndentation(), String.format(
                        "%s;",
                        context.getValueSetString("byteBuf.readByte()")
                ))
        );
    }

    @Override
    public List<CodeLine> visitIntType(ParsedField.PrimitiveFieldType fieldType,
                                       ParsedField field,
                                       DecodeCodeGenContext context) {
        String newValue;
        if (field.getByteOrderPreference() == ByteOrderPreference.BE) {
            newValue = "byteBuf.readInt()";
        } else if (field.getByteOrderPreference() == ByteOrderPreference.LE) {
            newValue = "byteBuf.readIntLE()";
        } else {
            newValue = "le ? byteBuf.readIntLE() : byteBuf.readInt()";
        }

        return List.of(
                new CodeLine(context.getIndentation(), String.format(
                        "%s;",
                        context.getValueSetString(newValue)
                ))
        );
    }

    @Override
    public List<CodeLine> visitLongType(ParsedField.PrimitiveFieldType fieldType,
                                        ParsedField field,
                                        DecodeCodeGenContext context) {
        String newValue;
        if (field.getByteOrderPreference() == ByteOrderPreference.BE) {
            newValue = "byteBuf.readLong()";
        } else if (field.getByteOrderPreference() == ByteOrderPreference.LE) {
            newValue = "byteBuf.readLongLE()";
        } else {
            newValue = "le ? byteBuf.readLongLE() : byteBuf.readLong()";
        }

        return List.of(
                new CodeLine(context.getIndentation(), String.format(
                        "%s;",
                        context.getValueSetString(newValue)
                ))
        );
    }

    @Override
    public List<CodeLine> visitShortType(ParsedField.PrimitiveFieldType fieldType,
                                         ParsedField field,
                                         DecodeCodeGenContext context) {
        String newValue;
        if (field.getByteOrderPreference() == ByteOrderPreference.BE) {
            newValue = "byteBuf.readShort()";
        } else if (field.getByteOrderPreference() == ByteOrderPreference.LE) {
            newValue = "byteBuf.readShortLE()";
        } else {
            newValue = "le ? byteBuf.readShortLE() : byteBuf.readShort()";
        }

        return List.of(
                new CodeLine(context.getIndentation(), String.format(
                        "%s;",
                        context.getValueSetString(newValue)
                ))
        );
    }

    @Override
    public List<CodeLine> visitWrappedBooleanType(ParsedField.WrappedPrimitiveFieldType fieldType,
                                                  ParsedField field,
                                                  DecodeCodeGenContext context) {
        return List.of(
                new CodeLine(context.getIndentation(), String.format(
                        "%s;",
                        context.getValueSetString("Boolean.valueOf(byteBuf.readByte() > 0)")
                ))
        );
    }

    @Override
    public List<CodeLine> visitWrappedByteType(ParsedField.WrappedPrimitiveFieldType fieldType,
                                               ParsedField field,
                                               DecodeCodeGenContext context) {
        return List.of(
                new CodeLine(context.getIndentation(), String.format(
                        "%s;",
                        context.getValueSetString("Byte.valueOf(byteBuf.readByte())")
                ))
        );
    }

    @Override
    public List<CodeLine> visitWrappedIntType(ParsedField.WrappedPrimitiveFieldType fieldType,
                                              ParsedField field,
                                              DecodeCodeGenContext context) {
        String newValue;
        if (field.getByteOrderPreference() == ByteOrderPreference.BE) {
            newValue = "byteBuf.readInt()";
        } else if (field.getByteOrderPreference() == ByteOrderPreference.LE) {
            newValue = "byteBuf.readIntLE()";
        } else {
            newValue = "le ? byteBuf.readIntLE() : byteBuf.readInt()";
        }

        return List.of(
                new CodeLine(context.getIndentation(), String.format(
                        "Integer.valueOf(%s);",
                        context.getValueSetString(newValue)
                ))
        );
    }

    @Override
    public List<CodeLine> visitWrappedLongType(ParsedField.WrappedPrimitiveFieldType fieldType,
                                               ParsedField field,
                                               DecodeCodeGenContext context) {
        String newValue;
        if (field.getByteOrderPreference() == ByteOrderPreference.BE) {
            newValue = "byteBuf.readLong()";
        } else if (field.getByteOrderPreference() == ByteOrderPreference.LE) {
            newValue = "byteBuf.readLongLE()";
        } else {
            newValue = "le ? byteBuf.readLongLE() : byteBuf.readLong()";
        }

        return List.of(
                new CodeLine(context.getIndentation(), String.format(
                        "Long.valueOf(%s);",
                        context.getValueSetString(newValue)
                ))
        );
    }

    @Override
    public List<CodeLine> visitWrappedShortType(ParsedField.WrappedPrimitiveFieldType fieldType,
                                                ParsedField field,
                                                DecodeCodeGenContext context) {
        String newValue;
        if (field.getByteOrderPreference() == ByteOrderPreference.BE) {
            newValue = "byteBuf.readShort()";
        } else if (field.getByteOrderPreference() == ByteOrderPreference.LE) {
            newValue = "byteBuf.readShortLE()";
        } else {
            newValue = "le ? byteBuf.readShortLE() : byteBuf.readShort()";
        }

        return List.of(
                new CodeLine(context.getIndentation(), String.format(
                        "Short.valueOf(%s);",
                        context.getValueSetString(newValue)
                ))
        );
    }

    @Override
    public List<CodeLine> visitStringType(ParsedField.StringFieldType fieldType,
                                          ParsedField field,
                                          DecodeCodeGenContext context) {
        int indentation = context.getIndentation();

        String sizeVariable = String.format("size%d", (indentation + 1));
        String bytesVariable = String.format("bytes%d", (indentation + 2));
        return List.of(
                new CodeLine(indentation, "{"),
                new CodeLine(indentation + 1, String.format(
                        "int %s = le ? byteBuf.readIntLE() : byteBuf.readInt();",
                        sizeVariable
                )),
                new CodeLine(indentation + 1, String.format(
                        "if (%s == 0) {",
                        sizeVariable
                )),
                new CodeLine(indentation + 2, String.format(
                        "%s;",
                        context.getValueSetString("null")
                )),
                new CodeLine(indentation + 1, "} else {"),
                new CodeLine(indentation + 2, String.format(
                        "byte[] %s = new byte[%s];",
                        bytesVariable,
                        sizeVariable
                )),
                new CodeLine(indentation + 2, String.format(
                        "byteBuf.readBytes(%s);",
                        bytesVariable
                )),
                new CodeLine(indentation + 2, String.format(
                        "%s;",
                        context.getValueSetString(String.format(
                                "new String(%s, StandardCharsets.UTF_8)",
                                bytesVariable
                        ))
                )),
                new CodeLine(indentation + 1, "}"),
                new CodeLine(indentation, "}")
        );
    }

    @Override
    public List<CodeLine> visitBitSetType(ParsedField.BitSetFieldType fieldType,
                                          ParsedField field,
                                          DecodeCodeGenContext context) {
        if (field.getEncodingSize() == null) {
            context.getMessager().printMessage(Diagnostic.Kind.ERROR, "BitSet missing encoding size");
        }

        int indentation = context.getIndentation();
        String bytesVariableName = "bytes" + (indentation + 1);
        String bVariableName = "b" + (indentation + 3);
        String iVariableName = "i" + (indentation + 2);
        return List.of(
                new CodeLine(indentation, "{"),
                new CodeLine(indentation + 1, String.format(
                        "byte[] %s = new byte[%d];",
                        bytesVariableName,
                        field.getEncodingSize()
                )),
                new CodeLine(indentation + 1, String.format(
                        "byteBuf.readBytes(%s);",
                        bytesVariableName
                )),
                new CodeLine(indentation + 1, "if (!le) {"),
                new CodeLine(indentation + 2, String.format(
                        "for (int %s = 0; %s < %d / 2; %s++) {",
                        iVariableName,
                        iVariableName,
                        field.getEncodingSize(),
                        iVariableName
                )),
                new CodeLine(indentation + 3, String.format(
                        "byte %s = %s[%d - %s - 1];",
                        bVariableName,
                        bytesVariableName,
                        field.getEncodingSize(),
                        iVariableName
                )),
                new CodeLine(indentation + 3, String.format(
                        "%s[%d - %s - 1] = %s[%s];",
                        bytesVariableName,
                        field.getEncodingSize(),
                        iVariableName,
                        bytesVariableName,
                        iVariableName
                )),
                new CodeLine(indentation + 3, String.format(
                        "%s[%s] = %s;",
                        bytesVariableName,
                        iVariableName,
                        bVariableName
                )),
                new CodeLine(indentation + 2, "}"),
                new CodeLine(indentation + 1, "}"),
                new CodeLine(indentation + 1, String.format(
                        "%s;",
                        context.getValueSetString(String.format("BitSet.valueOf(%s)", bytesVariableName))
                )),
                new CodeLine(indentation, "}")
        );
    }

    @Override
    public List<CodeLine> visitByteArrayType(ParsedField.ByteArrayFieldType fieldType,
                                             ParsedField field,
                                             DecodeCodeGenContext context) {
        int indentation = context.getIndentation();
        List<CodeLine> codeLines = new ArrayList<>();

        codeLines.add(new CodeLine(indentation, "{"));
        String sizeVariableName = "size" + (indentation + 1);

        if (field.isIncludeTypeSize()) {
            codeLines.add(new CodeLine(indentation + 1, String.format(
                    "int %s;",
                    sizeVariableName
            )));
            codeLines.add(new CodeLine(indentation + 1, "if (le) {"));

            switch (field.getSizeLength()) {
                case 1 -> codeLines.add(new CodeLine(indentation + 2, String.format(
                        "%s = byteBuf.readByte();",
                        sizeVariableName
                )));
                case 2 -> codeLines.add(new CodeLine(indentation + 2, String.format(
                        "%s = byteBuf.readShortLE();",
                        sizeVariableName
                )));
                default -> codeLines.add(new CodeLine(indentation + 2, String.format(
                        "%s = byteBuf.readIntLE();",
                        sizeVariableName
                )));
            }
            codeLines.add(new CodeLine(indentation + 1, "} else {"));

            switch (field.getSizeLength()) {
                case 1 -> codeLines.add(new CodeLine(indentation + 2, String.format(
                        "%s = byteBuf.readByte();",
                        sizeVariableName
                )));
                case 2 -> codeLines.add(new CodeLine(indentation + 2, String.format(
                        "%s = byteBuf.readShort();",
                        sizeVariableName
                )));
                default -> codeLines.add(new CodeLine(indentation + 2, String.format(
                        "%s = byteBuf.readInt();",
                        sizeVariableName
                )));
            }
            codeLines.add(new CodeLine(indentation + 1, "}"));
        } else if (field.getEncodingSize() != null) {
            codeLines.add(new CodeLine(indentation + 1, String.format(
                    "int %s = %d;",
                    sizeVariableName,
                    field.getEncodingSize()
            )));
        } else if (field.getSizeProperty() != null && !field.getSizeProperty().isEmpty()) {
            codeLines.add(new CodeLine(indentation + 1, String.format(
                    "int %s = decoded.%s;",
                    sizeVariableName,
                    field.getSizeProperty()
            )));
        } else {
            context.getMessager().printMessage(Diagnostic.Kind.ERROR, "Byte array without included size or encoding size");
        }

        String bytesVariableName = "bytes" + (indentation + 1);
        codeLines.add(new CodeLine(indentation + 1, String.format(
                "byte[] %s = new byte[%s];",
                bytesVariableName,
                sizeVariableName
        )));
        codeLines.add(new CodeLine(indentation + 1, String.format(
                "byteBuf.readBytes(%s);",
                bytesVariableName
        )));

        if (field.getByteOrderPreference() != ByteOrderPreference.NONE) {
            String needToReverseVariableName = "needToReverse" + (indentation + 1);
            codeLines.add(new CodeLine(indentation + 1, String.format(
                    "boolean %s = %sle;",
                    needToReverseVariableName,
                    (field.getByteOrderPreference() == ByteOrderPreference.LE) ? "!" : ""
            )));

            codeLines.add(new CodeLine(indentation + 1, String.format(
                    "if (%s) {",
                    needToReverseVariableName
            )));

            String iVariableName = "i" + (indentation + 2);
            codeLines.add(new CodeLine(indentation + 2, String.format(
                    "for (int %s = 0; %s < %s / 2; %s++) {",
                    iVariableName,
                    iVariableName,
                    sizeVariableName,
                    iVariableName
            )));
            String bVariableName = "b" + (indentation + 3);
            codeLines.add(new CodeLine(indentation + 3, String.format(
                    "byte %s = %s[%s];",
                    bVariableName,
                    bytesVariableName,
                    iVariableName
            )));
            codeLines.add(new CodeLine(indentation + 3, String.format(
                    "%s[%s] = %s[%s - %s - 1];",
                    bytesVariableName,
                    iVariableName,
                    bytesVariableName,
                    sizeVariableName,
                    iVariableName
            )));
            codeLines.add(new CodeLine(indentation + 3, String.format(
                    "%s[%s - %s - 1] = %s;",
                    bytesVariableName,
                    sizeVariableName,
                    iVariableName,
                    bVariableName
            )));
            codeLines.add(new CodeLine(indentation + 2, "}"));
            codeLines.add(new CodeLine(indentation + 1, "}"));
        }

        codeLines.add(new CodeLine(indentation + 1, String.format(
                "%s;",
                context.getValueSetString(bytesVariableName)
        )));
        codeLines.add(new CodeLine(indentation, "}"));

        return codeLines;
    }

    @Override
    public List<CodeLine> visitEnumType(ParsedField.EnumFieldType fieldType, ParsedField field, DecodeCodeGenContext context) {
        int indentation = context.getIndentation();
        List<CodeLine> codeLines = new ArrayList<>();

        int encodingSize = 1;
        if (field.getEncodingSize() != null) {
            encodingSize = field.getEncodingSize();
        }

        String valueString = switch (encodingSize) {
            case 1 -> "byteBuf.readByte()";
            case 2 -> "(le) ? byteBuf.readShortLE() : byteBuf.readShort()";
            default -> "(le) ? byteBuf.readIntLE() : byteBuf.readInt()";
        };

        ClassNameSplitter enumClassName = new ClassNameSplitter(fieldType.getClassName());
        codeLines.add(new CodeLine(indentation, String.format(
                "%s;",
                context.getValueSetString(String.format(
                        "%s.getFromValueInt(%s)",
                        enumClassName.getActualClassName(),
                        valueString
                ))
        )));

        return codeLines;
    }

    @Override
    public List<CodeLine> visitListType(ParsedField.ListFieldType fieldType, ParsedField field, DecodeCodeGenContext context) {
        int indentation = context.getIndentation();
        List<CodeLine> codeLines = new ArrayList<>();

        codeLines.add(new CodeLine(indentation, "{"));
        String listSizeVariable = "listSize" + (indentation + 1);
        codeLines.add(new CodeLine(indentation + 1, String.format(
                "int %s = le ? byteBuf.readIntLE() : byteBuf.readInt();",
                listSizeVariable
        )));

        String parameterTypeString = fieldType.getElementFieldType().accept(typeParamVisitor, field, null);
        String decodedListVariableName = "decodedList" + (indentation + 1);
        codeLines.add(new CodeLine(indentation + 1, String.format(
                "List<%s> %s = new ArrayList<>();",
                parameterTypeString,
                decodedListVariableName
        )));
        String iVariableName = "i" + (indentation + 1);
        codeLines.add(new CodeLine(indentation + 1, String.format(
                "for (int %s = 0; %s < %s; %s++) {",
                iVariableName,
                iVariableName,
                listSizeVariable,
                iVariableName
        )));
        String elementVariableName = "element" + (indentation + 2);
        codeLines.add(new CodeLine(indentation + 2, String.format(
                "%s %s;",
                parameterTypeString,
                elementVariableName
        )));
        codeLines.addAll(fieldType.getElementFieldType().accept(
                this, field, new DecodeCodeGenContext(
                        indentation + 2,
                        String.format("%s.get(%s)", context.getValueAccessor(), iVariableName),
                        elementVariableName,
                        false,
                        context.getParsedClasses(),
                        context.getMessager())
        ));
        codeLines.add(new CodeLine(indentation + 2, String.format(
                "%s.add(%s);",
                decodedListVariableName,
                elementVariableName
        )));
        codeLines.add(new CodeLine(indentation + 1, "}"));

        codeLines.add(new CodeLine(indentation + 1, String.format(
                "%s;",
                context.getValueSetString(decodedListVariableName)
        )));
        codeLines.add(new CodeLine(indentation, "}"));

        return codeLines;
    }

    @Override
    public List<CodeLine> visitSetType(ParsedField.SetFieldType fieldType, ParsedField field, DecodeCodeGenContext context) {
        int indentation = context.getIndentation();
        List<CodeLine> codeLines = new ArrayList<>();

        codeLines.add(new CodeLine(indentation, "{"));
        String setSizeVariable = "setSize" + (indentation + 1);
        codeLines.add(new CodeLine(indentation + 1, String.format(
                "int %s = le ? byteBuf.readIntLE() : byteBuf.readInt();",
                setSizeVariable
        )));

        String parameterTypeString = fieldType.getElementFieldType().accept(typeParamVisitor, field, null);
        String decodedListVariableName = "decodedSet" + (indentation + 1);
        codeLines.add(new CodeLine(indentation + 1, String.format(
                "Set<%s> %s = new HashSet<>();",
                parameterTypeString,
                decodedListVariableName
        )));
        String iVariableName = "i" + (indentation + 1);
        codeLines.add(new CodeLine(indentation + 1, String.format(
                "for (int %s = 0; %s < %s; %s++) {",
                iVariableName,
                iVariableName,
                setSizeVariable,
                iVariableName
        )));
        String elementVariableName = "element" + (indentation + 2);
        codeLines.add(new CodeLine(indentation + 2, String.format(
                "%s %s;",
                parameterTypeString,
                elementVariableName
        )));
        codeLines.addAll(fieldType.getElementFieldType().accept(
                this, field, new DecodeCodeGenContext(
                        indentation + 2,
                        String.format("%s.get(%s)", context.getValueAccessor(), iVariableName),
                        elementVariableName,
                        false,
                        context.getParsedClasses(),
                        context.getMessager())
        ));
        codeLines.add(new CodeLine(indentation + 2, String.format(
                "%s.add(%s);",
                decodedListVariableName,
                elementVariableName
        )));
        codeLines.add(new CodeLine(indentation + 1, "}"));

        codeLines.add(new CodeLine(indentation + 1, String.format(
                "%s;",
                context.getValueSetString(decodedListVariableName)
        )));
        codeLines.add(new CodeLine(indentation, "}"));

        return codeLines;
    }

    @Override
    public List<CodeLine> visitMapType(ParsedField.MapFieldType fieldType, ParsedField field, DecodeCodeGenContext context) {
        int indentation = context.getIndentation();
        List<CodeLine> codeLines = new ArrayList<>();

        codeLines.add(new CodeLine(indentation, "{"));
        String mapSizeVariable = "mapSize" + (indentation + 1);
        codeLines.add(new CodeLine(indentation + 1, String.format(
                "int %s = le ? byteBuf.readIntLE() : byteBuf.readInt();",
                mapSizeVariable
        )));

        String keyTypeString = fieldType.getKeyFieldType().accept(typeParamVisitor, field, null);
        String valueTypeString = fieldType.getValueFieldType().accept(typeParamVisitor, field, null);
        String decodedListVariableName = "decodedMap" + (indentation + 1);
        codeLines.add(new CodeLine(indentation + 1, String.format(
                "Map<%s, %s> %s = new HashMap<>();",
                keyTypeString,
                valueTypeString,
                decodedListVariableName
        )));
        String iVariableName = "i" + (indentation + 1);
        codeLines.add(new CodeLine(indentation + 1, String.format(
                "for (int %s = 0; %s < %s; %s++) {",
                iVariableName,
                iVariableName,
                mapSizeVariable,
                iVariableName
        )));
        String keyVariableName = "key" + (indentation + 2);
        String valueVariableName = "value" + (indentation + 2);
        codeLines.add(new CodeLine(indentation + 2, String.format(
                "%s %s;",
                keyTypeString,
                keyVariableName
        )));
        codeLines.addAll(fieldType.getKeyFieldType().accept(
                this, field, new DecodeCodeGenContext(
                        indentation + 2,
                        String.format("%s.get(%s)", context.getValueAccessor(), iVariableName),
                        keyVariableName,
                        false,
                        context.getParsedClasses(),
                        context.getMessager())
        ));
        codeLines.add(new CodeLine(indentation + 2, String.format(
                "%s %s;",
                valueTypeString,
                valueVariableName
        )));
        codeLines.addAll(fieldType.getValueFieldType().accept(
                this, field, new DecodeCodeGenContext(
                        indentation + 2,
                        String.format("%s.get(%s)", context.getValueAccessor(), iVariableName),
                        valueVariableName,
                        false,
                        context.getParsedClasses(),
                        context.getMessager())
        ));
        codeLines.add(new CodeLine(indentation + 2, String.format(
                "%s.put(%s, %s);",
                decodedListVariableName,
                keyVariableName,
                valueVariableName
        )));
        codeLines.add(new CodeLine(indentation + 1, "}"));

        codeLines.add(new CodeLine(indentation + 1, String.format(
                "%s;",
                context.getValueSetString(decodedListVariableName)
        )));
        codeLines.add(new CodeLine(indentation, "}"));

        return codeLines;
    }
}
