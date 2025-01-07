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

import java.util.List;

public interface FieldTypeVisitor<R, P extends CodeGenContext> {
    R visitDeclaredType(ParsedField.DeclaredFieldType fieldType, ParsedField field, P context);

    R visitBooleanType(ParsedField.PrimitiveFieldType fieldType, ParsedField field, P context);

    R visitByteType(ParsedField.PrimitiveFieldType fieldType, ParsedField field, P context);

    R visitIntType(ParsedField.PrimitiveFieldType fieldType, ParsedField field, P context);

    R visitLongType(ParsedField.PrimitiveFieldType fieldType, ParsedField field, P context);

    R visitShortType(ParsedField.PrimitiveFieldType fieldType, ParsedField field, P context);

    R visitWrappedBooleanType(ParsedField.WrappedPrimitiveFieldType fieldType, ParsedField field, P context);

    R visitWrappedByteType(ParsedField.WrappedPrimitiveFieldType fieldType, ParsedField field, P context);

    R visitWrappedIntType(ParsedField.WrappedPrimitiveFieldType fieldType, ParsedField field, P context);

    R visitWrappedLongType(ParsedField.WrappedPrimitiveFieldType fieldType, ParsedField field, P context);

    R visitWrappedShortType(ParsedField.WrappedPrimitiveFieldType fieldType, ParsedField field, P context);

    R visitStringType(ParsedField.StringFieldType fieldType, ParsedField field, P context);

    R visitBitSetType(ParsedField.BitSetFieldType fieldType, ParsedField field, P context);

    R visitByteArrayType(ParsedField.ByteArrayFieldType fieldType, ParsedField field, P context);

    R visitEnumType(ParsedField.EnumFieldType fieldType, ParsedField field, P context);

    R visitListType(ParsedField.ListFieldType fieldType, ParsedField field, P context);

    R visitSetType(ParsedField.SetFieldType fieldType, ParsedField field, P context);

    R visitMapType(ParsedField.MapFieldType fieldType, ParsedField field, P context);
}
