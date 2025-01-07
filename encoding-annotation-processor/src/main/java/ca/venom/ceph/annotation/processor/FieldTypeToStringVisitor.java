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

class FieldTypeToStringVisitor implements FieldTypeVisitor<String, EncodeCodeGenContext> {
    @Override
    public String visitDeclaredType(ParsedField.DeclaredFieldType fieldType,
                                    ParsedField field,
                                    EncodeCodeGenContext context) {
        ClassNameSplitter fullClassName = new ClassNameSplitter(fieldType.getClassName());
        return fullClassName.getActualClassName();
    }

    @Override
    public String visitBooleanType(ParsedField.PrimitiveFieldType fieldType, ParsedField field, EncodeCodeGenContext context) {
        return "boolean";
    }

    @Override
    public String visitByteType(ParsedField.PrimitiveFieldType fieldType, ParsedField field, EncodeCodeGenContext context) {
        return "byte";
    }

    @Override
    public String visitIntType(ParsedField.PrimitiveFieldType fieldType, ParsedField field, EncodeCodeGenContext context) {
        return "int";
    }

    @Override
    public String visitLongType(ParsedField.PrimitiveFieldType fieldType, ParsedField field, EncodeCodeGenContext context) {
        return "long";
    }

    @Override
    public String visitShortType(ParsedField.PrimitiveFieldType fieldType, ParsedField field, EncodeCodeGenContext context) {
        return "short";
    }

    @Override
    public String visitWrappedBooleanType(ParsedField.WrappedPrimitiveFieldType fieldType, ParsedField field, EncodeCodeGenContext context) {
        return "Boolean";
    }

    @Override
    public String visitWrappedByteType(ParsedField.WrappedPrimitiveFieldType fieldType, ParsedField field, EncodeCodeGenContext context) {
        return "Byte";
    }

    @Override
    public String visitWrappedIntType(ParsedField.WrappedPrimitiveFieldType fieldType, ParsedField field, EncodeCodeGenContext context) {
        return "Integer";
    }

    @Override
    public String visitWrappedLongType(ParsedField.WrappedPrimitiveFieldType fieldType, ParsedField field, EncodeCodeGenContext context) {
        return "Long";
    }

    @Override
    public String visitWrappedShortType(ParsedField.WrappedPrimitiveFieldType fieldType, ParsedField field, EncodeCodeGenContext context) {
        return "Short";
    }

    @Override
    public String visitStringType(ParsedField.StringFieldType fieldType, ParsedField field, EncodeCodeGenContext context) {
        return "String";
    }

    @Override
    public String visitBitSetType(ParsedField.BitSetFieldType fieldType, ParsedField field, EncodeCodeGenContext context) {
        return "BitSet";
    }

    @Override
    public String visitByteArrayType(ParsedField.ByteArrayFieldType fieldType, ParsedField field, EncodeCodeGenContext context) {
        return "byte[]";
    }

    @Override
    public String visitEnumType(ParsedField.EnumFieldType fieldType, ParsedField field, EncodeCodeGenContext context) {
        ClassNameSplitter fullClassName = new ClassNameSplitter(fieldType.getClassName());
        return fullClassName.getActualClassName();
    }

    @Override
    public String visitListType(ParsedField.ListFieldType fieldType, ParsedField field, EncodeCodeGenContext context) {
        return String.format(
                "List<%s>",
                fieldType.getElementFieldType().accept(this, field, context)
        );
    }

    @Override
    public String visitSetType(ParsedField.SetFieldType fieldType, ParsedField field, EncodeCodeGenContext context) {
        return String.format(
                "Set<%s>",
                fieldType.getElementFieldType().accept(this, field, context)
        );
    }

    @Override
    public String visitMapType(ParsedField.MapFieldType fieldType, ParsedField field, EncodeCodeGenContext context) {
        return String.format(
                "Map<%s, %s>",
                fieldType.getKeyFieldType().accept(this, field, context),
                fieldType.getValueFieldType().accept(this, field, context)
        );
    }
}
