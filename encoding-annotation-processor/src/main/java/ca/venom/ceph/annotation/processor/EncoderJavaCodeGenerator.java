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
import ca.venom.ceph.annotation.processor.parser.ParsedFieldMethod;
import ca.venom.ceph.annotation.processor.parser.VersionField;
import ca.venom.ceph.annotation.processor.parser.VersionGroup;
import ca.venom.ceph.types.VersionWithCompat;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        imports.add("java.util.BitSet");
        imports.add("ca.venom.ceph.types.VersionWithCompat");

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
            imports.add("ca.venom.ceph.protocol.messages.CephMsgHeader2");

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
        } else if (parsedClass.isMessagePayload()) {
            imports.add("ca.venom.ceph.protocol.messages.CephMsgHeader2");
        }

        if (parsedClass.getVersionWithCompatGenerator() != null) {
            imports.add("ca.venom.ceph.types.VersionWithCompat");
        }

        for (List<VersionField> parsedFieldList : parsedClass.getFields().values()) {
            for (VersionField parsedField : parsedFieldList) {
                if (parsedField != null) {
                    addImportsForFieldType(parsedField.getFieldType(), imports, fullClassName);
                }
            }
        }

        return imports;
    }

    private void addImportsForFieldType(VersionField.FieldType fieldType,
                                        Set<String> imports,
                                        ClassNameSplitter fullClassName) {
        if (fieldType instanceof VersionField.DeclaredFieldType declaredFieldType) {
            ClassNameSplitter fieldClassName = new ClassNameSplitter(declaredFieldType.getClassName());
            if (!fullClassName.getPackageName().equals(fieldClassName.getPackageName())) {
                imports.add(fieldClassName.getPackageName() + "." + fieldClassName.getActualClassName());
                imports.add(fieldClassName.getPackageName() + "." + fieldClassName.getEncoderClassName());
            }
        } else if (fieldType instanceof VersionField.EnumFieldType enumFieldType) {
            ClassNameSplitter fieldClassName = new ClassNameSplitter(enumFieldType.getClassName());
            if (!fullClassName.getPackageName().equals(fieldClassName.getPackageName())) {
                imports.add(fieldClassName.getPackageName() + "." + fieldClassName.getActualClassName());
            }
        } else if (fieldType instanceof VersionField.StringFieldType) {
            imports.add("java.nio.charset.StandardCharsets");
        } else if (fieldType instanceof VersionField.ListFieldType listFieldType) {
            imports.add("java.util.List");
            imports.add("java.util.ArrayList");
            addImportsForFieldType(listFieldType.getElementFieldType(), imports, fullClassName);
        } else if (fieldType instanceof VersionField.SetFieldType setFieldType) {
            imports.add("java.util.Set");
            imports.add("java.util.HashSet");
            addImportsForFieldType(setFieldType.getElementFieldType(), imports, fullClassName);
        } else if (fieldType instanceof VersionField.MapFieldType mapFieldType) {
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
                "    public static void encode(%s toEncode, ByteBuf byteBuf, boolean le, BitSet features) {%n",
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
                    "            %sEncoder.encode(toEncodeImpl, byteBuf, le, features);%n",
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
                        "            %s.encode((%s) toEncode, byteBuf, le, features);%n",
                        parsedChildClassName.getEncoderClassName(),
                        parsedChildClassName.getActualClassName()
                );
            }
        }

        if (!first) {
            out.println("        }");
        }
    }

    private String getGetterName(VersionField field) {
        String getter;
        if (field.getFieldType() instanceof VersionField.PrimitiveFieldType primitiveFieldType) {
            if (primitiveFieldType.getTypeKind() == TypeKind.BOOLEAN) {
                getter = "is";
            } else {
                getter = "get";
            }
        } else {
            getter = "get";
        }
        getter += field.getName().substring(0, 1).toUpperCase(Locale.ROOT) + field.getName().substring(1) + "()";

        return getter;
    }

    private String getSetterName(VersionField field) {
        return "set" + field.getName().substring(0, 1).toUpperCase(Locale.ROOT) + field.getName().substring(1);
    }

    private void writeStandardEncodeMethodBody(PrintWriter out) {
        if (parsedClass.getMarker() != null) {
            out.printf("        byteBuf.writeByte((byte) %d);%n", parsedClass.getMarker());
        }

        boolean includeVersion = false;
        if (parsedClass.isMessagePayload()) {
            includeVersion = true;
            out.println(
                    "        VersionWithCompat versionWithCompat = new VersionWithCompat((byte) toEncode.getHeadVersion(features), (byte) toEncode.getHeadCompatVersion(features));"
            );
        } else if (parsedClass.getVersionWithCompatGenerator() != null) {
            includeVersion = true;
            out.printf("        VersionWithCompat versionWithCompat = toEncode.%s(features);%n", parsedClass.getVersionWithCompatGenerator());
            out.println("        byteBuf.writeByte(versionWithCompat.getVersion());");
            out.println("        if (versionWithCompat.getCompat() != null) {");
            out.println("            byteBuf.writeByte(versionWithCompat.getCompat());");
            out.println("        }");
        } else if (parsedClass.getVersion() != null) {
            includeVersion = true;
            if (parsedClass.getCompatVersion() != null) {
                out.printf(
                        "        VersionWithCompat versionWithCompat = new VersionWithCompat((byte) %d, (byte) %d);%n",
                        parsedClass.getVersion(),
                        parsedClass.getCompatVersion()
                );
                out.println("        byteBuf.writeByte(versionWithCompat.getVersion());");
                out.println("        byteBuf.writeByte(versionWithCompat.getCompat());");
            } else {
                out.printf(
                        "        VersionWithCompat versionWithCompat = new VersionWithCompat((byte) %d, (byte) -1);%n",
                        parsedClass.getVersion()
                );
                out.println("        byteBuf.writeByte(versionWithCompat.getVersion());");
            }
        } else {
            out.println("        VersionWithCompat versionWithCompat = null;");
        }

        if (parsedClass.isIncludeSize()) {
            out.println("        int sizeIndex = byteBuf.writerIndex();");
            out.println("        byteBuf.writeZero(4);");
        }

        if (parsedClass.getPreEncodeMethod() != null) {
            out.printf("        toEncode.%s();%n", parsedClass.getPreEncodeMethod());
        }

        final EncodeFieldTypeVisitor fieldTypeVisitor = new EncodeFieldTypeVisitor();
        final Set<VersionGroup> allVersionGroups = new HashSet<>(parsedClass.getFields().keySet());
        allVersionGroups.addAll(parsedClass.getEncodeMethods().keySet());

        boolean checkVersion = allVersionGroups.size() > 1;
        boolean isFirstVersionGroup = true;
        for (VersionGroup versionGroup : allVersionGroups) {
            final List<VersionField> fields = parsedClass.getFields().getOrDefault(versionGroup, Collections.emptyList());
            final List<ParsedFieldMethod> methods = parsedClass.getEncodeMethods().getOrDefault(versionGroup, Collections.emptyList());

            String prefix = "";
            if (checkVersion) {
                String elsePrefix = isFirstVersionGroup ? "" : "} else ";
                isFirstVersionGroup = false;
                if (versionGroup.getMinVersion() > -1 && versionGroup.getMinVersion() != versionGroup.getMaxVersion()) {
                    out.printf(
                            "        %sif (versionWithCompat.getVersion() <= (byte) %d && versionWithCompat.getVersion() >= (byte) %d) {%n",
                            elsePrefix,
                            versionGroup.getMaxVersion(),
                            versionGroup.getMinVersion()
                    );
                } else {
                    out.printf("        %sif (versionWithCompat.getVersion() == (byte) %d) {%n", elsePrefix, versionGroup.getMaxVersion());
                }
                prefix = "    ";
            }

            for (int i = 0; i < fields.size(); i++) {
                final VersionField field = fields.get(i);
                final ParsedFieldMethod method = methods.get(i);
                int indentation = 2;

                if (method != null) {
                    out.printf("%s        toEncode.%s(byteBuf, le, features", prefix, method.getMethodName());
                    if (method.isIncludeVersion()) {
                        if (includeVersion) {
                            out.print(", versionWithCompat.getVersion()");
                        } else {
                            messager.printMessage(
                                    Diagnostic.Kind.ERROR,
                                    String.format("Method %s.%s expects version, but it is not available", parsedClass.getClassName(), method.getMethodName())
                            );
                        }
                    }
                    out.println(");");
                } else if (field != null) {
                    final String getter = "toEncode." + getGetterName(field);
                    EncodeCodeGenContext context = new EncodeCodeGenContext(indentation, getter, parsedClasses);
                    List<CodeLine> codeLines = field.getFieldType().accept(fieldTypeVisitor, field, context);

                    for (CodeLine codeLine : codeLines) {
                        out.printf(
                                "%s%s%s%n",
                                prefix,
                                " ".repeat(4 * codeLine.getIndentation()),
                                codeLine.getText()
                        );
                    }
                } else {
                    messager.printMessage(
                            Diagnostic.Kind.ERROR,
                            String.format(
                                    "Missing field/method [%d] (%d, %d) for class: %s",
                                    i + 1,
                                    versionGroup.getMinVersion(),
                                    versionGroup.getMaxVersion(),
                                    parsedClass.getClassName())
                    );
                }
            }
        }

        if (checkVersion) {
            out.println("        }");
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

                VersionField.DeclaredFieldType currentFieldType = new VersionField.DeclaredFieldType(
                        parsedClass.getClassName()
                );
                VersionField.DeclaredFieldType otherFieldType = new VersionField.DeclaredFieldType(
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
                    "    public static %s decode(ByteBuf byteBuf, boolean le, BitSet features%s) throws DecodingException {%n",
                    fullClassName.getActualClassName(),
                    Boolean.TRUE.equals(parsedAbstractClass.getUseParameter()) ? ", int typeCode" : "");
            writeAbstractDecodeMethodBody(out, parsedAbstractClass);
        } else if ("ca.venom.ceph.protocol.messages.MessagePayload".equals(parsedClass.getClassName())) {
            out.println("    public static MessagePayload decode(ByteBuf byteBuf, boolean le, BitSet features, CephMsgHeader2 header) throws DecodingException {");
            writeMessagePayloadDecodeMethodBody(out, parsedClasses);
        } else {
            out.printf(
                    "    public static %s decode(ByteBuf byteBuf, boolean le, BitSet features%s) throws DecodingException {%n",
                    fullClassName.getActualClassName(),
                    parsedClass.isMessagePayload() ? ", CephMsgHeader2 header" : "");
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
                        "        int typeCode = byteBuf.getByte(byteBuf.readerIndex() + %d);\n",
                        parsedAbstractClass.getTypeOffset()
                );
                case 2 -> out.printf(
                        "        int typeCode = le ? byteBuf.getShortLE(byteBuf.readerIndex() + %d) : byteBuf.getShort(byteBuf.readerIndex() + %d);\n",
                        parsedAbstractClass.getTypeOffset(),
                        parsedAbstractClass.getTypeOffset()
                );
                default -> out.printf(
                        "        int typeCode = le ? byteBuf.getIntLE(byteBuf.readerIndex() + %d) : byteBuf.getInt(byteBuf.readerIndex() + %d);\n",
                        parsedAbstractClass.getTypeOffset(),
                        parsedAbstractClass.getTypeOffset()
                );
            }
        }

        out.println("        switch (typeCode) {");

        final String versionString;
        if ("ca.venom.ceph.protocol.messages.MessagePayload".equals(parsedAbstractClass.getClassName())) {
            versionString = ", version";
        } else {
            versionString = "";
        }

        String defaultClassName = null;
        for (ParsedAbstractClass.ImplementationClass implementation : parsedAbstractClass.getImplementations()) {
            if (implementation.isDefault()) {
                defaultClassName = implementation.getClassName();
                continue;
            }

            out.printf("            case %d:%n", implementation.getTypeCode());
            int lastDotIndex = implementation.getClassName().lastIndexOf('.');
            String simpleClassName = implementation.getClassName().substring(lastDotIndex + 1);
            out.printf("                return %sEncoder.decode(byteBuf, le, features%s);%n", simpleClassName, versionString);
        }

        if (defaultClassName != null) {
            out.println("            default:");
            int lastDotIndex = defaultClassName.lastIndexOf('.');
            String simpleClassName = defaultClassName.substring(lastDotIndex + 1);
            out.printf("                return %sEncoder.decode(byteBuf, le, features%s);%n", simpleClassName, versionString);
            out.println("        }");
        } else {
            out.println("        }");
            out.println("        throw new IllegalArgumentException(\"Unknown type code read: \" + typeCode);");
        }
    }

    public void writeMessagePayloadDecodeMethodBody(PrintWriter out, Collection<ParsedClass> parsedClasses) {
        out.println("        switch (MessageType.getFromCode(header.getType())) {");

        for (ParsedClass parsedChildClass : parsedClasses) {
            if (parsedChildClass.getMessageType() != null) {
                ClassNameSplitter parsedChildClassName = new ClassNameSplitter(parsedChildClass.getClassName());

                out.printf("            case %s:%n", parsedChildClass.getMessageType().name());
                out.printf(
                        "                return %s.decode(byteBuf, le, features, header);%n",
                        parsedChildClassName.getEncoderClassName());
            }
        }

        out.println("        }");
        out.println("        return null;");
    }

    private VersionWithCompat getVersionRange(ParsedClass parsedClass) {
        byte maxVersion = (byte) -1;
        if (parsedClass.getVersion() != null) {
            maxVersion = parsedClass.getVersion();
        }

        byte minVersion = (byte) -1;
        for (VersionGroup versionGroup : parsedClass.getFields().keySet()) {
            if (versionGroup.getMaxVersion() > maxVersion) {
                maxVersion = versionGroup.getMaxVersion();
            }

            if (versionGroup.getMinVersion() >= 0 && (minVersion == (byte) -1 ||
                    versionGroup.getMinVersion() < minVersion)) {
                minVersion = versionGroup.getMinVersion();
            }
        }

        for (VersionGroup versionGroup : parsedClass.getEncodeMethods().keySet()) {
            if (versionGroup.getMaxVersion() > maxVersion) {
                maxVersion = versionGroup.getMaxVersion();
            }

            if (versionGroup.getMinVersion() >= 0 && (minVersion == (byte) -1 ||
                    versionGroup.getMinVersion() < minVersion)) {
                minVersion = versionGroup.getMinVersion();
            }
        }

        return new VersionWithCompat(maxVersion, minVersion == (byte) -1 ? null : minVersion);
    }

    private void writeDecodeForVersionGroup(
            PrintWriter out,
            int indentation,
            VersionGroup versionGroup,
            DecodeFieldTypeVisitor fieldTypeVisitor) {
        final List<VersionField> fields = parsedClass.getFields().get(versionGroup);
        final List<ParsedFieldMethod> methods = parsedClass.getDecodeMethods().get(versionGroup);
        final int fieldsCount = Math.max(fields.size(), methods.size());

        for (int i = 0; i < fieldsCount; i++) {
            VersionField field = fields.get(i);
            ParsedFieldMethod method = methods.get(i);
            if (field != null) {
                String getter = "decoded." + getGetterName(field);
                String setter = "decoded." + getSetterName(field);

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
            } else if (method != null) {
                out.printf(
                        "%sdecoded.%s(byteBuf, le, features%s);%n",
                        " ".repeat(4 * indentation),
                        method.getMethodName(),
                        method.isIncludeVersion() ? ", version" : ""
                );
            }
        }
    }

    private void writeStandardDecodeMethodBody(PrintWriter out, ParsedClass parsedClass) {
        DecodeFieldTypeVisitor fieldTypeVisitor = new DecodeFieldTypeVisitor();

        if (parsedClass.getMarker() != null) {
            out.printf("        if ((byte) %d != byteBuf.readByte()) {%n", parsedClass.getMarker());
            out.println("            throw new DecodingException(\"Invalid marker value\");");
            out.println("        }");
        }

        final boolean haveVersion;
        if (parsedClass.getVersion() != null || parsedClass.getVersionWithCompatGenerator() != null) {
            haveVersion = true;

            out.println("        byte version = byteBuf.readByte();");

            final VersionWithCompat versionRange = getVersionRange(parsedClass);
            if (versionRange.getVersion() != (byte) -1 && versionRange.getCompat() != null) {
                out.printf(
                        "        if (version > (byte) %d || version < %d) {%n",
                        versionRange.getVersion(),
                        versionRange.getCompat()
                );
                out.println("            throw new DecodingException(\"Unsupported version\");");
                out.println("        }");
            } else if (versionRange.getVersion() != (byte) -1) {
                out.printf("        if (version > (byte) %d) {%n", versionRange.getVersion());
                out.println("            throw new DecodingException(\"Unsupported version\");");
                out.println("        }");
            }

            if (parsedClass.getCompatVersionDecider() != null) {
                out.printf("        if (decoded.%s(version)) {%n", parsedClass.getCompatVersionDecider());
                out.println("            byteBuf.readByte();");
                out.println("        }");
            } else if (parsedClass.getCompatVersion() != null) {
                out.println("        byteBuf.readByte();");
            }
        } else if (parsedClass.isMessagePayload()) {
            out.println("        short version = header.getVersion();");
            haveVersion = true;
        } else {
            haveVersion = false;
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

        final List<VersionGroup> versionGroups = new ArrayList<>(parsedClass.getFields().keySet());
        versionGroups.addAll(parsedClass.getDecodeMethods().keySet());

        if (haveVersion) {
            if (versionGroups.size() == 1) {
                final VersionGroup versionGroup = versionGroups.get(0);
                boolean needCloseBlock = false;
                if (versionGroup.getMinVersion() != -1 && versionGroup.getMaxVersion() != -1) {
                    needCloseBlock = true;
                    out.printf(
                            "        if (version >= %d && version <= %d) {%n",
                            versionGroup.getMinVersion(),
                            versionGroup.getMaxVersion()
                    );
                } else if (versionGroup.getMinVersion() != -1) {
                    needCloseBlock = true;
                    out.printf("        if (version >= %d) {%n", versionGroup.getMinVersion());
                }
                writeDecodeForVersionGroup(out, needCloseBlock ? 3 : 2, versionGroup, fieldTypeVisitor);
                if (needCloseBlock) {
                    out.println("        }");
                }
            } else if (versionGroups.size() > 1) {
                boolean isFirst = true;
                for (VersionGroup versionGroup : versionGroups) {
                    if (versionGroup.getMaxVersion() != (byte) -1) {
                        out.printf(
                                "        %sif (version <= (byte) %d && version >= (byte) %d) {%n",
                                isFirst ? "" : "} else ",
                                versionGroup.getMaxVersion(),
                                versionGroup.getMinVersion()
                        );
                    } else {
                        out.printf(
                                "        %sif (version >= (byte) %d) {%n",
                                isFirst ? "" : "} else ",
                                versionGroup.getMinVersion()
                        );
                    }

                    if (isFirst) {
                        isFirst = false;
                    }

                    writeDecodeForVersionGroup(out, 3, versionGroup, fieldTypeVisitor);
                }
                out.println("        }");
            }
        } else {
            final VersionGroup versionGroup = versionGroups.get(0);
            writeDecodeForVersionGroup(out, 2, versionGroup, fieldTypeVisitor);
        }

        if (parsedClass.getPostDecodeMethod() != null) {
            out.printf(
                    "        decoded.%s(%s);%n",
                    parsedClass.getPostDecodeMethod(),
                    haveVersion ? "version" : ""
            );
        }
    }
}
