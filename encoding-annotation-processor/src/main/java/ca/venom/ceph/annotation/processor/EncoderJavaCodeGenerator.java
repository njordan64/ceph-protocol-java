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

import ca.venom.ceph.annotation.processor.parser.ParsedAbstractClass;
import ca.venom.ceph.annotation.processor.parser.ParsedClass;
import ca.venom.ceph.annotation.processor.parser.ParsedField;
import ca.venom.ceph.encoding.annotations.ConditonOperator;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EncoderJavaCodeGenerator {
    private final ParsedClass parsedClass;
    private final Filer filer;
    private final Map<String, ParsedClass> parsedClasses;
    private final Messager messager;

    public EncoderJavaCodeGenerator(ParsedClass parsedClass,
                                    Filer filer,
                                    Map<String, ParsedClass> parsedClasses,
                                    Messager messager) {
        this.parsedClass = parsedClass;
        this.filer = filer;
        this.parsedClasses = parsedClasses;
        this.messager = messager;
    }

    public void generateJavaCode(Collection<ParsedClass> parsedClasses) throws IOException {
        ClassNameSplitter fullClassName = new ClassNameSplitter(parsedClass.getClassName());

        JavaFileObject sourceFile = filer.createSourceFile(
                fullClassName.getPackageName() + "." + fullClassName.getEncoderClassName()
        );

        try (PrintWriter out = new PrintWriter(sourceFile.openWriter())) {
            out.printf("package %s;%n%n", fullClassName.getPackageName());
            for (String importEntry : getImports(parsedClasses)) {
                out.printf("import %s;%n", importEntry);
            }
            out.println();

            out.printf("public class %s {%n", fullClassName.getEncoderClassName());
            writeEncodeMethod(out, fullClassName, parsedClasses);

            out.println();
            writeDecodeMethod(out, fullClassName, parsedClasses);

            out.println("}");
        }
    }

    private Set<String> getImports(Collection<ParsedClass> parsedClasses) {
        Set<String> imports = new HashSet<>();
        imports.add("ca.venom.ceph.protocol.DecodingException");
        imports.add("io.netty.buffer.ByteBuf");

        ClassNameSplitter fullClassName = new ClassNameSplitter(parsedClass.getClassName());

        if (parsedClass instanceof ParsedAbstractClass parsedAbstractClass) {
            for (ParsedAbstractClass.ImplementationClass implementationClass : parsedAbstractClass.getImplementations()) {
                ClassNameSplitter implClassName = new ClassNameSplitter(implementationClass.getClassName());

                if (!fullClassName.getPackageName().equals(implClassName.getPackageName())) {
                    imports.add(implementationClass.getClassName());
                    imports.add(implClassName.getPackageName() + "." + implClassName.getEncoderClassName());
                }
            }

            return imports;
        } else if ("ca.venom.ceph.protocol.messages.MessagePayload".equals(parsedClass.getClassName())) {
            imports.add("ca.venom.ceph.types.MessageType");

            ClassNameSplitter parsedClassName = new ClassNameSplitter(parsedClass.getClassName());
            for (ParsedClass parsedChildClass : parsedClasses) {
                if (parsedChildClass.getMessageType() != null) {
                    ClassNameSplitter parsedChildClassName = new ClassNameSplitter(parsedChildClass.getClassName());
                    if (!parsedClassName.getPackageName().equals(parsedChildClassName.getPackageName())) {
                        imports.add(parsedChildClassName.getPackageName() + "." +
                                parsedChildClassName.getActualClassName());
                        imports.add(parsedChildClassName.getPackageName() + "." +
                                parsedChildClassName.getEncoderClassName());
                    }
                }
            }
        }

        for (ParsedField parsedField : parsedClass.getFields()) {
            addImportsForFieldType(parsedField.getFieldType(), imports, fullClassName);
        }

        return imports;
    }

    private void addImportsForFieldType(ParsedField.FieldType fieldType,
                                        Set<String> imports,
                                        ClassNameSplitter fullClassName) {
        if (fieldType instanceof ParsedField.DeclaredFieldType declaredFieldType) {
            ClassNameSplitter fieldClassName = new ClassNameSplitter(declaredFieldType.getClassName());
            if (!fullClassName.getPackageName().equals(fieldClassName.getPackageName())) {
                imports.add(fieldClassName.getPackageName() + "." + fieldClassName.getActualClassName());
                imports.add(fieldClassName.getPackageName() + "." + fieldClassName.getEncoderClassName());
            }
        } else if (fieldType instanceof ParsedField.BitSetFieldType) {
            imports.add("java.util.BitSet");
        } else if (fieldType instanceof ParsedField.EnumFieldType enumFieldType) {
            ClassNameSplitter fieldClassName = new ClassNameSplitter(enumFieldType.getClassName());
            if (!fullClassName.getPackageName().equals(fieldClassName.getPackageName())) {
                imports.add(fieldClassName.getPackageName() + "." + fieldClassName.getActualClassName());
            }
        } else if (fieldType instanceof ParsedField.StringFieldType) {
            imports.add("java.nio.charset.StandardCharsets");
        } else if (fieldType instanceof ParsedField.ListFieldType listFieldType) {
            imports.add("java.util.List");
            imports.add("java.util.ArrayList");
            addImportsForFieldType(listFieldType.getElementFieldType(), imports, fullClassName);
        } else if (fieldType instanceof ParsedField.SetFieldType setFieldType) {
            imports.add("java.util.Set");
            imports.add("java.util.HashSet");
            addImportsForFieldType(setFieldType.getElementFieldType(), imports, fullClassName);
        } else if (fieldType instanceof ParsedField.MapFieldType mapFieldType) {
            imports.add("java.util.Map");
            imports.add("java.util.HashMap");
            addImportsForFieldType(mapFieldType.getKeyFieldType(), imports, fullClassName);
            addImportsForFieldType(mapFieldType.getValueFieldType(), imports, fullClassName);
        }
    }

    private void writeEncodeMethod(PrintWriter out,
                                   ClassNameSplitter fullClassName,
                                   Collection<ParsedClass> parsedClasses) {
        out.printf(
                "    public static void encode(%s toEncode, ByteBuf byteBuf, boolean le) {%n",
                fullClassName.getActualClassName()
        );

        if (parsedClass instanceof ParsedAbstractClass parsedAbstractClass) {
            writeAbstractEncodeMethodBody(out, parsedAbstractClass);
        } else if ("ca.venom.ceph.protocol.messages.MessagePayload".equals(parsedClass.getClassName())) {
            writeMessagePayloadEncodeMethodBody(out, parsedClasses);
        } else {
            writeStandardEncodeMethodBody(out);
        }

        out.printf("    }%n");
    }

    private void writeAbstractEncodeMethodBody(PrintWriter out, ParsedAbstractClass parsedAbstractClass) {
        boolean isFirst = true;
        for (ParsedAbstractClass.ImplementationClass implementationClass : parsedAbstractClass.getImplementations()) {
            String implementationClassName = implementationClass.getClassName();
            int lastDotIndex = implementationClass.getClassName().lastIndexOf('.');
            String simpleClassName = implementationClassName.substring(lastDotIndex + 1);

            out.printf(
                    "        %sif (toEncode instanceof %s toEncodeImpl) {%n",
                    isFirst ? "" : "} else ",
                    simpleClassName);
            out.printf(
                    "            %sEncoder.encode(toEncodeImpl, byteBuf, le);%n",
                    simpleClassName
            );

            isFirst = false;
        }

        if (!isFirst) {
            out.println("        }");
        }
    }

    private void writeMessagePayloadEncodeMethodBody(PrintWriter out, Collection<ParsedClass> parsedClasses) {
        boolean first = true;
        for (ParsedClass parsedChildClass : parsedClasses) {
            if (parsedChildClass.getMessageType() != null) {
                if (first) {
                    first = false;
                    out.print("        ");
                } else {
                    out.print("        } else ");
                }

                ClassNameSplitter parsedChildClassName = new ClassNameSplitter(parsedChildClass.getClassName());
                out.printf("if (toEncode instanceof %s) {%n", parsedChildClassName.getActualClassName());
                out.printf(
                        "            %s.encode((%s) toEncode, byteBuf, le);%n",
                        parsedChildClassName.getEncoderClassName(),
                        parsedChildClassName.getActualClassName()
                );
            }
        }

        if (!first) {
            out.println("        }");
        }
    }

    private void writeStandardEncodeMethodBody(PrintWriter out) {
        if (parsedClass.getMarker() != null) {
            out.printf("        byteBuf.writeByte((byte) %d);%n", parsedClass.getMarker());
        }

        if (parsedClass.getVersion() != null) {
            out.printf("        byteBuf.writeByte((byte) %d);%n", parsedClass.getVersion());
            if (parsedClass.getCompatVersion() != null) {
                out.printf("        byteBuf.writeByte((byte) %d);%n", parsedClass.getCompatVersion());
            }
        }

        if (parsedClass.isIncludeSize()) {
            out.println("        int sizeIndex = byteBuf.writerIndex();");
            out.println("        byteBuf.writeZero(4);");
        }

        EncodeFieldTypeVisitor fieldTypeVisitor = new EncodeFieldTypeVisitor();
        for (ParsedField field : parsedClass.getFields()) {
            int indentation = 2;

            if (field.getCondition() != null) {
                indentation++;
                out.printf(
                        "        if (toEncode.%s %s %s) {%n",
                        field.getCondition().getProperty(),
                        field.getCondition().getOperator() == ConditonOperator.EQUAL ? "==" : "!=",
                        field.getCondition().getValues()[0]
                );
            }

            String getter = "toEncode.";
            if (field.getFieldType() instanceof ParsedField.PrimitiveFieldType primitiveFieldType) {
                if (primitiveFieldType.getTypeKind() == TypeKind.BOOLEAN) {
                    getter += "is";
                } else {
                    getter += "get";
                }
            } else {
                getter += "get";
            }
            getter += field.getName().substring(0, 1).toUpperCase(Locale.ROOT) + field.getName().substring(1) + "()";
            EncodeCodeGenContext context = new EncodeCodeGenContext(indentation, getter, parsedClasses);
            List<CodeLine> codeLines = field.getFieldType().accept(fieldTypeVisitor, field, context);

            for (CodeLine codeLine : codeLines) {
                out.printf(
                        "%s%s%n",
                        " ".repeat(4 * codeLine.getIndentation()),
                        codeLine.getText()
                );
            }

            if (field.getCondition() != null) {
                out.println("        }");
            }
        }

        if (parsedClass.isIncludeSize()) {
            out.println("        if (le) {");
            out.println("            byteBuf.setIntLE(sizeIndex, byteBuf.writerIndex() - sizeIndex - 4);");
            out.println("        } else {");
            out.println("            byteBuf.setInt(sizeIndex, byteBuf.writerIndex() - sizeIndex - 4);");
            out.println("        }");
        }

        if (parsedClass.getZeroPadToSizeOfClass() != null) {
            if (parsedClasses.containsKey(parsedClass.getZeroPadToSizeOfClass())) {
                ParsedClass otherParsedClass = parsedClasses.get(parsedClass.getZeroPadToSizeOfClass());
                EncodeCodeGenContext context = new EncodeCodeGenContext(0, "", parsedClasses);
                FixedSizeTypeVisitor sizeVisitor = new FixedSizeTypeVisitor();

                ParsedField.DeclaredFieldType currentFieldType = new ParsedField.DeclaredFieldType(
                        parsedClass.getClassName()
                );
                ParsedField.DeclaredFieldType otherFieldType = new ParsedField.DeclaredFieldType(
                        otherParsedClass.getClassName()
                );

                int currentSize = currentFieldType.accept(sizeVisitor, null, context);
                int otherSize = otherFieldType.accept(sizeVisitor, null, context);

                if (currentSize < otherSize) {
                    out.printf("        byteBuf.writeZero(%d);%n", otherSize - currentSize);
                }
            }
        }
    }

    private void writeDecodeMethod(PrintWriter out,
                                   ClassNameSplitter fullClassName,
                                   Collection<ParsedClass> parsedClasses) {
        if (parsedClass instanceof ParsedAbstractClass parsedAbstractClass) {
            out.printf(
                    "    public static %s decode(ByteBuf byteBuf, boolean le%s) throws DecodingException {%n",
                    fullClassName.getActualClassName(),
                    Boolean.TRUE.equals(parsedAbstractClass.getUseParameter()) ? ", int typeCode" : "");
            writeAbstractDecodeMethodBody(out, parsedAbstractClass);
        } else if ("ca.venom.ceph.protocol.messages.MessagePayload".equals(parsedClass.getClassName())) {
            out.println("    public static MessagePayload decode(ByteBuf byteBuf, boolean le, int typeCode) throws DecodingException {");
            writeMessagePayloadDecodeMethodBody(out, parsedClasses);
        } else {
            out.printf(
                    "    public static %s decode(ByteBuf byteBuf, boolean le) throws DecodingException {%n",
                    fullClassName.getActualClassName());
            out.printf(
                    "        %s decoded = new %s();%n",
                    fullClassName.getActualClassName(),
                    fullClassName.getActualClassName()
            );
            writeStandardDecodeMethodBody(out, parsedClass);
            out.println("        return decoded;");
        }

        out.println("    }");
    }

    private void writeAbstractDecodeMethodBody(PrintWriter out, ParsedAbstractClass parsedAbstractClass) {
        if (!Boolean.TRUE.equals(parsedAbstractClass.getUseParameter())) {
            switch (parsedAbstractClass.getTypeSize()) {
                case 1 -> out.printf(
                        "        int typeCode = byteBuf.getByte(byteBuf.readerIndex() + %d);",
                        parsedAbstractClass.getTypeOffset()
                );
                case 2 -> out.printf(
                        "        int typeCode = le ? byteBuf.getShortLE(byteBuf.readerIndex() + %d) : byteBuf.getShort(byteBuf.readerIndex() + %d);",
                        parsedAbstractClass.getTypeOffset(),
                        parsedAbstractClass.getTypeOffset()
                );
                default -> out.printf(
                        "        int typeCode = le ? byteBuf.getIntLE(byteBuf.readerIndex() + %d) : byteBuf.getInt(byteBuf.readerIndex() + %d);",
                        parsedAbstractClass.getTypeOffset(),
                        parsedAbstractClass.getTypeOffset()
                );
            }
        }

        out.println("        switch (typeCode) {");

        String defaultClassName = null;
        for (ParsedAbstractClass.ImplementationClass implementation : parsedAbstractClass.getImplementations()) {
            if (implementation.isDefault()) {
                defaultClassName = implementation.getClassName();
                continue;
            }

            out.printf("            case %d:%n", implementation.getTypeCode());
            int lastDotIndex = implementation.getClassName().lastIndexOf('.');
            String simpleClassName = implementation.getClassName().substring(lastDotIndex + 1);
            out.printf("                return %sEncoder.decode(byteBuf, le);%n", simpleClassName);
        }

        if (defaultClassName != null) {
            out.println("            default:");
            int lastDotIndex = defaultClassName.lastIndexOf('.');
            String simpleClassName = defaultClassName.substring(lastDotIndex + 1);
            out.printf("                return %sEncoder.decode(byteBuf, le);%n", simpleClassName);
            out.println("        }");
        } else {
            out.println("        }");
            out.println("        throw new IllegalArgumentException(\"Unknown type code read: \" + typeCode);");
        }
    }

    public void writeMessagePayloadDecodeMethodBody(PrintWriter out, Collection<ParsedClass> parsedClasses) {
        out.println("        switch (MessageType.getFromCode(typeCode)) {");

        for (ParsedClass parsedChildClass : parsedClasses) {
            if (parsedChildClass.getMessageType() != null) {
                ClassNameSplitter parsedChildClassName = new ClassNameSplitter(parsedChildClass.getClassName());

                out.printf("            case %s:%n", parsedChildClass.getMessageType().name());
                out.printf(
                        "                return %s.decode(byteBuf, le);%n",
                        parsedChildClassName.getEncoderClassName());
            }
        }

        out.println("        }");
        out.println("        return null;");
    }

    private void writeStandardDecodeMethodBody(PrintWriter out, ParsedClass parsedClass) {
        DecodeFieldTypeVisitor fieldTypeVisitor = new DecodeFieldTypeVisitor();

        if (parsedClass.getMarker() != null) {
            out.printf("        if ((byte) %d != byteBuf.readByte()) {%n", parsedClass.getMarker());
            out.println("            throw new DecodingException(\"Invalid marker value\");");
            out.println("        }");
        }

        if (parsedClass.getVersion() != null) {
            out.printf("        if ((byte) %d != byteBuf.readByte()) {", parsedClass.getVersion());
            out.println("            throw new DecodingException(\"Unsupported version\");");
            out.println("        }");

            if (parsedClass.getCompatVersion() != null) {
                out.printf("        if ((byte) %d != byteBuf.readByte()) {", parsedClass.getCompatVersion());
                out.println("            throw new DecodingException(\"Unsupported compat version\");");
                out.println("        }");
            }
        }

        if (parsedClass.isIncludeSize()) {
            out.println("        if (le) {");
            out.println("            if (byteBuf.readIntLE() > byteBuf.readableBytes()) {");
            out.println("                throw new DecodingException(\"Not enough bytes available\");");
            out.println("            }");
            out.println("        } else {");
            out.println("            if (byteBuf.readInt() > byteBuf.readableBytes()) {");
            out.println("                throw new DecodingException(\"Not enough bytes available\");");
            out.println("            }");
            out.println("        }");
        }

        for (ParsedField field : parsedClass.getFields()) {
            int indentation = 2;

            if (field.getCondition() != null) {
                indentation++;

                out.printf(
                        "        if (decoded.%s %s %s) {%n",
                        field.getCondition().getProperty(),
                        field.getCondition().getOperator() == ConditonOperator.EQUAL ? "==" : "!=",
                        field.getCondition().getValues()[0]
                );
            }

            String getter = "decoded.";
            String setter = "decoded.set";
            if (field.getFieldType() instanceof ParsedField.PrimitiveFieldType primitiveFieldType) {
                if (primitiveFieldType.getTypeKind() == TypeKind.BOOLEAN) {
                    getter += "is";
                } else {
                    getter += "get";
                }
            } else {
                getter = "get";
            }
            String capitalizedField = field.getName().substring(0, 1).toUpperCase(Locale.ROOT) +
                    field.getName().substring(1);
            getter += capitalizedField;
            setter += capitalizedField;

            DecodeCodeGenContext fieldContext = new DecodeCodeGenContext(
                    indentation,
                    String.format("decoded.%s", getter),
                    setter,
                    true,
                    parsedClasses,
                    messager
            );
            List<CodeLine> codeLines = field.getFieldType().accept(fieldTypeVisitor, field, fieldContext);

            for (CodeLine codeLine : codeLines) {
                out.printf(
                        "%s%s%n",
                        " ".repeat(4 * codeLine.getIndentation()),
                        codeLine.getText()
                );
            }

            if (field.getCondition() != null) {
                out.println("        }");
            }
        }
    }
}
