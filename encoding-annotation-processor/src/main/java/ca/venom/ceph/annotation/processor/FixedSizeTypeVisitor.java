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

import ca.venom.ceph.annotation.processor.parser.ParsedClass;
import ca.venom.ceph.annotation.processor.parser.ParsedField;

public class FixedSizeTypeVisitor implements FieldTypeVisitor<Integer, CodeGenContext> {
    @Override
    public Integer visitDeclaredType(ParsedField.DeclaredFieldType fieldType,
                                     ParsedField field,
                                     CodeGenContext context) {
        if (context.getParsedClasses().containsKey(fieldType.getClassName())) {
            ParsedClass fieldParsedClass = context.getParsedClasses().get(fieldType.getClassName());
            int size = 0;

            if (fieldParsedClass.getMarker() != null) {
                size++;
            }

            if (fieldParsedClass.getVersion() != null) {
                size++;
                if (fieldParsedClass.getCompatVersion() != null) {
                    size++;
                }
            }

            if (fieldParsedClass.isIncludeSize()) {
                size += 4;
            }

            for (ParsedField subField : fieldParsedClass.getFields()) {
                size += subField.getFieldType().accept(this, subField, context);
            }

            return size;
        } else {
            throw new RuntimeException(fieldType.getClassName() + " is not encodable");
        }
    }

    @Override
    public Integer visitBooleanType(ParsedField.PrimitiveFieldType fieldType,
                                    ParsedField field,
                                    CodeGenContext context) {
        return 1;
    }

    @Override
    public Integer visitByteType(ParsedField.PrimitiveFieldType fieldType,
                                 ParsedField field,
                                 CodeGenContext context) {
        return 1;
    }

    @Override
    public Integer visitIntType(ParsedField.PrimitiveFieldType fieldType,
                                ParsedField field,
                                CodeGenContext context) {
        return 4;
    }

    @Override
    public Integer visitLongType(ParsedField.PrimitiveFieldType fieldType,
                                 ParsedField field,
                                 CodeGenContext context) {
        return 8;
    }

    @Override
    public Integer visitShortType(ParsedField.PrimitiveFieldType fieldType,
                                  ParsedField field,
                                  CodeGenContext context) {
        return 2;
    }

    @Override
    public Integer visitWrappedBooleanType(ParsedField.WrappedPrimitiveFieldType fieldType,
                                           ParsedField field,
                                           CodeGenContext context) {
        return 1;
    }

    @Override
    public Integer visitWrappedByteType(ParsedField.WrappedPrimitiveFieldType fieldType,
                                        ParsedField field,
                                        CodeGenContext context) {
        return 1;
    }

    @Override
    public Integer visitWrappedIntType(ParsedField.WrappedPrimitiveFieldType fieldType,
                                       ParsedField field,
                                       CodeGenContext context) {
        return 4;
    }

    @Override
    public Integer visitWrappedLongType(ParsedField.WrappedPrimitiveFieldType fieldType,
                                        ParsedField field,
                                        CodeGenContext context) {
        return 8;
    }

    @Override
    public Integer visitWrappedShortType(ParsedField.WrappedPrimitiveFieldType fieldType,
                                         ParsedField field,
                                         CodeGenContext context) {
        return 2;
    }

    @Override
    public Integer visitStringType(ParsedField.StringFieldType fieldType,
                                   ParsedField field,
                                   CodeGenContext context) {
        throw new RuntimeException("String is not of fixed size");
    }

    @Override
    public Integer visitBitSetType(ParsedField.BitSetFieldType fieldType,
                                   ParsedField field,
                                   CodeGenContext context) {
        if (field.getEncodingSize() != null) {
            return field.getEncodingSize();
        }

        return 1;
    }

    @Override
    public Integer visitByteArrayType(ParsedField.ByteArrayFieldType fieldType,
                                      ParsedField field,
                                      CodeGenContext context) {
        if (field.getEncodingSize() != null) {
            return field.getEncodingSize();
        }

        throw new RuntimeException("Byte array does not have a fixed length");
    }

    @Override
    public Integer visitEnumType(ParsedField.EnumFieldType fieldType,
                                 ParsedField field,
                                 CodeGenContext context) {
        if (field.getEncodingSize() != null) {
            return field.getEncodingSize();
        }

        return 1;
    }

    @Override
    public Integer visitListType(ParsedField.ListFieldType fieldType,
                                 ParsedField field,
                                 CodeGenContext context) {
        throw new RuntimeException("List does not have a fixed length");
    }

    @Override
    public Integer visitSetType(ParsedField.SetFieldType fieldType,
                                ParsedField field,
                                CodeGenContext context) {
        throw new RuntimeException("Set does not have a fixed length");
    }

    @Override
    public Integer visitMapType(ParsedField.MapFieldType fieldType,
                                ParsedField field,
                                CodeGenContext context) {
        throw new RuntimeException("Map does not have a fixed length");
    }
}
