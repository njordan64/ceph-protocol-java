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

import ca.venom.ceph.annotation.processor.parser.VersionField;

class FieldTypeToStringVisitor implements FieldTypeVisitor<String, EncodeCodeGenContext> {
    @Override
    public String visitDeclaredType(VersionField.DeclaredFieldType fieldType,
                                    VersionField field,
                                    EncodeCodeGenContext context) {
        ClassNameSplitter fullClassName = new ClassNameSplitter(fieldType.getClassName());
        return fullClassName.getActualClassName();
    }

    @Override
    public String visitBooleanType(VersionField.PrimitiveFieldType fieldType, VersionField field, EncodeCodeGenContext context) {
        return "boolean";
    }

    @Override
    public String visitByteType(VersionField.PrimitiveFieldType fieldType, VersionField field, EncodeCodeGenContext context) {
        return "byte";
    }

    @Override
    public String visitIntType(VersionField.PrimitiveFieldType fieldType, VersionField field, EncodeCodeGenContext context) {
        return "int";
    }

    @Override
    public String visitLongType(VersionField.PrimitiveFieldType fieldType, VersionField field, EncodeCodeGenContext context) {
        return "long";
    }

    @Override
    public String visitShortType(VersionField.PrimitiveFieldType fieldType, VersionField field, EncodeCodeGenContext context) {
        return "short";
    }

    @Override
    public String visitWrappedBooleanType(VersionField.WrappedPrimitiveFieldType fieldType, VersionField field, EncodeCodeGenContext context) {
        return "Boolean";
    }

    @Override
    public String visitWrappedByteType(VersionField.WrappedPrimitiveFieldType fieldType, VersionField field, EncodeCodeGenContext context) {
        return "Byte";
    }

    @Override
    public String visitWrappedIntType(VersionField.WrappedPrimitiveFieldType fieldType, VersionField field, EncodeCodeGenContext context) {
        return "Integer";
    }

    @Override
    public String visitWrappedLongType(VersionField.WrappedPrimitiveFieldType fieldType, VersionField field, EncodeCodeGenContext context) {
        return "Long";
    }

    @Override
    public String visitWrappedShortType(VersionField.WrappedPrimitiveFieldType fieldType, VersionField field, EncodeCodeGenContext context) {
        return "Short";
    }

    @Override
    public String visitStringType(VersionField.StringFieldType fieldType, VersionField field, EncodeCodeGenContext context) {
        return "String";
    }

    @Override
    public String visitBitSetType(VersionField.BitSetFieldType fieldType, VersionField field, EncodeCodeGenContext context) {
        return "BitSet";
    }

    @Override
    public String visitByteArrayType(VersionField.ByteArrayFieldType fieldType, VersionField field, EncodeCodeGenContext context) {
        return "byte[]";
    }

    @Override
    public String visitEnumType(VersionField.EnumFieldType fieldType, VersionField field, EncodeCodeGenContext context) {
        ClassNameSplitter fullClassName = new ClassNameSplitter(fieldType.getClassName());
        return fullClassName.getActualClassName();
    }

    @Override
    public String visitListType(VersionField.ListFieldType fieldType, VersionField field, EncodeCodeGenContext context) {
        return String.format(
                "List<%s>",
                fieldType.getElementFieldType().accept(this, field, context)
        );
    }

    @Override
    public String visitSetType(VersionField.SetFieldType fieldType, VersionField field, EncodeCodeGenContext context) {
        return String.format(
                "Set<%s>",
                fieldType.getElementFieldType().accept(this, field, context)
        );
    }

    @Override
    public String visitMapType(VersionField.MapFieldType fieldType, VersionField field, EncodeCodeGenContext context) {
        return String.format(
                "Map<%s, %s>",
                fieldType.getKeyFieldType().accept(this, field, context),
                fieldType.getValueFieldType().accept(this, field, context)
        );
    }
}
