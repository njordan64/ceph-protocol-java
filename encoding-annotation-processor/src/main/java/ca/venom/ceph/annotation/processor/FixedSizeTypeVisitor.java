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
import ca.venom.ceph.annotation.processor.parser.VersionField;
import ca.venom.ceph.annotation.processor.parser.VersionGroup;

import java.util.List;

public class FixedSizeTypeVisitor implements FieldTypeVisitor<Integer, CodeGenContext> {
    @Override
    public Integer visitDeclaredType(VersionField.DeclaredFieldType fieldType,
                                     VersionField field,
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

            final VersionGroup versionGroup = new VersionGroup(field.getMinVersion(), field.getMaxVersion());
            for (VersionField subField : fieldParsedClass.getFields().get(versionGroup)) {
                size += subField.getFieldType().accept(this, subField, context);
            }

            return size;
        } else {
            throw new RuntimeException(fieldType.getClassName() + " is not encodable");
        }
    }

    @Override
    public Integer visitBooleanType(VersionField.PrimitiveFieldType fieldType,
                                    VersionField field,
                                    CodeGenContext context) {
        return 1;
    }

    @Override
    public Integer visitByteType(VersionField.PrimitiveFieldType fieldType,
                                 VersionField field,
                                 CodeGenContext context) {
        return 1;
    }

    @Override
    public Integer visitIntType(VersionField.PrimitiveFieldType fieldType,
                                VersionField field,
                                CodeGenContext context) {
        return 4;
    }

    @Override
    public Integer visitLongType(VersionField.PrimitiveFieldType fieldType,
                                 VersionField field,
                                 CodeGenContext context) {
        return 8;
    }

    @Override
    public Integer visitShortType(VersionField.PrimitiveFieldType fieldType,
                                  VersionField field,
                                  CodeGenContext context) {
        return 2;
    }

    @Override
    public Integer visitWrappedBooleanType(VersionField.WrappedPrimitiveFieldType fieldType,
                                           VersionField field,
                                           CodeGenContext context) {
        return 1;
    }

    @Override
    public Integer visitWrappedByteType(VersionField.WrappedPrimitiveFieldType fieldType,
                                        VersionField field,
                                        CodeGenContext context) {
        return 1;
    }

    @Override
    public Integer visitWrappedIntType(VersionField.WrappedPrimitiveFieldType fieldType,
                                       VersionField field,
                                       CodeGenContext context) {
        return 4;
    }

    @Override
    public Integer visitWrappedLongType(VersionField.WrappedPrimitiveFieldType fieldType,
                                        VersionField field,
                                        CodeGenContext context) {
        return 8;
    }

    @Override
    public Integer visitWrappedShortType(VersionField.WrappedPrimitiveFieldType fieldType,
                                         VersionField field,
                                         CodeGenContext context) {
        return 2;
    }

    @Override
    public Integer visitStringType(VersionField.StringFieldType fieldType,
                                   VersionField field,
                                   CodeGenContext context) {
        throw new RuntimeException("String is not of fixed size");
    }

    @Override
    public Integer visitBitSetType(VersionField.BitSetFieldType fieldType,
                                   VersionField field,
                                   CodeGenContext context) {
        if (field.getEncodingSize() != null) {
            return field.getEncodingSize();
        }

        return 1;
    }

    @Override
    public Integer visitByteArrayType(VersionField.ByteArrayFieldType fieldType,
                                      VersionField field,
                                      CodeGenContext context) {
        if (field.getEncodingSize() != null) {
            return field.getEncodingSize();
        }

        throw new RuntimeException("Byte array does not have a fixed length");
    }

    @Override
    public Integer visitEnumType(VersionField.EnumFieldType fieldType,
                                 VersionField field,
                                 CodeGenContext context) {
        if (field.getEncodingSize() != null) {
            return field.getEncodingSize();
        }

        return 1;
    }

    @Override
    public Integer visitListType(VersionField.ListFieldType fieldType,
                                 VersionField field,
                                 CodeGenContext context) {
        throw new RuntimeException("List does not have a fixed length");
    }

    @Override
    public Integer visitSetType(VersionField.SetFieldType fieldType,
                                VersionField field,
                                CodeGenContext context) {
        throw new RuntimeException("Set does not have a fixed length");
    }

    @Override
    public Integer visitMapType(VersionField.MapFieldType fieldType,
                                VersionField field,
                                CodeGenContext context) {
        throw new RuntimeException("Map does not have a fixed length");
    }
}
