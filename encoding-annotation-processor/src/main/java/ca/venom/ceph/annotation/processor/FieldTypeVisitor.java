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

public interface FieldTypeVisitor<R, P extends CodeGenContext> {
    R visitDeclaredType(VersionField.DeclaredFieldType fieldType, VersionField field, P context);

    R visitBooleanType(VersionField.PrimitiveFieldType fieldType, VersionField field, P context);

    R visitByteType(VersionField.PrimitiveFieldType fieldType, VersionField field, P context);

    R visitIntType(VersionField.PrimitiveFieldType fieldType, VersionField field, P context);

    R visitLongType(VersionField.PrimitiveFieldType fieldType, VersionField field, P context);

    R visitShortType(VersionField.PrimitiveFieldType fieldType, VersionField field, P context);

    R visitWrappedBooleanType(VersionField.WrappedPrimitiveFieldType fieldType, VersionField field, P context);

    R visitWrappedByteType(VersionField.WrappedPrimitiveFieldType fieldType, VersionField field, P context);

    R visitWrappedIntType(VersionField.WrappedPrimitiveFieldType fieldType, VersionField field, P context);

    R visitWrappedLongType(VersionField.WrappedPrimitiveFieldType fieldType, VersionField field, P context);

    R visitWrappedShortType(VersionField.WrappedPrimitiveFieldType fieldType, VersionField field, P context);

    R visitStringType(VersionField.StringFieldType fieldType, VersionField field, P context);

    R visitBitSetType(VersionField.BitSetFieldType fieldType, VersionField field, P context);

    R visitByteArrayType(VersionField.ByteArrayFieldType fieldType, VersionField field, P context);

    R visitEnumType(VersionField.EnumFieldType fieldType, VersionField field, P context);

    R visitListType(VersionField.ListFieldType fieldType, VersionField field, P context);

    R visitSetType(VersionField.SetFieldType fieldType, VersionField field, P context);

    R visitMapType(VersionField.MapFieldType fieldType, VersionField field, P context);
}
