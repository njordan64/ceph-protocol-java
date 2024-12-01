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

import ca.venom.ceph.annotation.processor.fields.BitSetCodeGenerator;
import ca.venom.ceph.annotation.processor.fields.BooleanCodeGenerator;
import ca.venom.ceph.annotation.processor.fields.BooleanPrimitiveCodeGenerator;
import ca.venom.ceph.annotation.processor.fields.ByteArrayCodeGenerator;
import ca.venom.ceph.annotation.processor.fields.ByteCodeGenerator;
import ca.venom.ceph.annotation.processor.fields.BytePrimitiveCodeGenerator;
import ca.venom.ceph.annotation.processor.fields.CephTypeCodeGenerator;
import ca.venom.ceph.annotation.processor.fields.EnumCodeGenerator;
import ca.venom.ceph.annotation.processor.fields.FieldCodeGenerator;
import ca.venom.ceph.annotation.processor.fields.IntCodeGenerator;
import ca.venom.ceph.annotation.processor.fields.IntPrimitiveCodeGenerator;
import ca.venom.ceph.annotation.processor.fields.ListCodeGenerator;
import ca.venom.ceph.annotation.processor.fields.LongCodeGenerator;
import ca.venom.ceph.annotation.processor.fields.LongPrimitiveCodeGenerator;
import ca.venom.ceph.annotation.processor.fields.MapCodeGenerator;
import ca.venom.ceph.annotation.processor.fields.SetCodeGenerator;
import ca.venom.ceph.annotation.processor.fields.ShortCodeGenerator;
import ca.venom.ceph.annotation.processor.fields.ShortPrimitiveCodeGenerator;
import ca.venom.ceph.annotation.processor.fields.StringCodeGenerator;
import ca.venom.ceph.types.MessageType;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Uses the model and metadata of @CephType annotated classes to generate classes that can encode and decode
 * the modeled classes.
 */
public class CodeGenerator {
    private Map<String, FieldCodeGenerator> fieldCodeGenerators;
    private Filer filer;
    private String packageName;
    private CodeGenerationContext context;

    /**
     * Creates a new CodeGenerator
     *
     * @param filer used to manage source files
     * @param messager used for reporting errors/warnings
     */
    public CodeGenerator(Filer filer, Messager messager) {
        this.filer = filer;

        fieldCodeGenerators = new HashMap<>();
        context = new CodeGenerationContext(messager, fieldCodeGenerators, new HashMap<>());

        fieldCodeGenerators.put("boolean", new BooleanPrimitiveCodeGenerator(context));
        fieldCodeGenerators.put("java.lang.Boolean", new BooleanCodeGenerator(context));
        fieldCodeGenerators.put("byte", new BytePrimitiveCodeGenerator(context));
        fieldCodeGenerators.put("java.lang.Byte", new ByteCodeGenerator(context));
        fieldCodeGenerators.put("short", new ShortPrimitiveCodeGenerator(context));
        fieldCodeGenerators.put("java.lang.Short", new ShortCodeGenerator(context));
        fieldCodeGenerators.put("int", new IntPrimitiveCodeGenerator(context));
        fieldCodeGenerators.put("java.lang.Integer", new IntCodeGenerator(context));
        fieldCodeGenerators.put("long", new LongPrimitiveCodeGenerator(context));
        fieldCodeGenerators.put("java.lang.Long", new LongCodeGenerator(context));
        fieldCodeGenerators.put("java.lang.String", new StringCodeGenerator(context));
        fieldCodeGenerators.put("byte[]", new ByteArrayCodeGenerator(context));
        fieldCodeGenerators.put("Enum", new EnumCodeGenerator(context));
        fieldCodeGenerators.put("java.util.BitSet", new BitSetCodeGenerator(context));
        fieldCodeGenerators.put("java.util.Set", new SetCodeGenerator(context));
        fieldCodeGenerators.put("java.util.List", new ListCodeGenerator(context));
        fieldCodeGenerators.put("java.util.Map", new MapCodeGenerator(context));
        fieldCodeGenerators.put("CephType", new CephTypeCodeGenerator(context));
    }

    /**
     * Updates the map of classes that were annotated with @CephType
     * @param encodableClasses map of classes annotated with @CephType
     */
    public void setEncodableClasses(Map<String, EncodableClass> encodableClasses) {
        context.setEncodableClasses(encodableClasses);
    }

    private String getEncodingPackageName(String packageName) {
        String encodingPackageName = packageName;
        if (packageName.matches(".*[A-Z].*")) {
            String outerClassName = packageName.substring(packageName.lastIndexOf('.') + 1);
            encodingPackageName = packageName.substring(0, packageName.lastIndexOf('.'));
            encodingPackageName += "._generated." + outerClassName;
        } else {
            encodingPackageName += "._generated";
        }

        return encodingPackageName;
    }

    public void setMessageTypeClasses(Map<MessageType, String> messageTypeClasses) {
        context.setMessageTypeClasses(messageTypeClasses);
    }

    /**
     * Generate the source code files for the encoding/decoding.
     * @throws IOException
     */
    public void generateEncodingSources() throws IOException {
        for (EncodableClass encodableClass : context.getEncodableClasses().values()) {
            encodableClass.getFields().sort(Comparator.comparingInt(EncodableField::getOrder));
            generateEncodingSource(encodableClass);
        }
    }

    private Integer getFixedSize(String targetClassName) {
        EncodableClass encodableClass = context.getEncodableClasses().get(targetClassName);
        if (encodableClass == null) {
            return null;
        }

        int size = 0;
        if (encodableClass.getMarker() != null) {
            size++;
        }
        if (encodableClass.getVersion() != null) {
            size++;
            if (encodableClass.getCompatVersion() != null) {
                size++;
            }
        }
        if (encodableClass.isIncludeSize()) {
            size += 4;
        }

        for (EncodableField field : encodableClass.getFields()) {
            Integer fieldSize = null;
            switch (field.getType()) {
                case "boolean":
                case "java.lang.Boolean":
                case "byte":
                case "java.lang.Byte":
                    fieldSize = 1;
                    break;
                case "short":
                case "java.lang.Short":
                    fieldSize = 2;
                    break;
                case "int":
                case "java.lang.Integer":
                    fieldSize = 4;
                    break;
                case "long":
                case "java.lang.Long":
                    fieldSize = 8;
                    break;
            }

            if (fieldSize != null) {
                size += fieldSize;
                continue;
            }

            if (context.getEncodableClasses().containsKey(field.getType())) {
                fieldSize = getFixedSize(field.getType());
                if (fieldSize == null) {
                    return null;
                }

                size += fieldSize;
                continue;
            }

            if (field.getInterfaces() != null &&
                    field.getInterfaces().contains("ca.venom.ceph.types.EnumWithIntValue")) {
                if (field.getEncodingSize() == null) {
                    size++;
                } else {
                    size += field.getEncodingSize();
                }

                continue;
            }

            return null;
        }

        return size;
    }

    private void generateEncodingSource(EncodableClass encodableClass) throws IOException {
        packageName = getEncodingPackageName(encodableClass.getPackageName());

        context.getImports().clear();
        context.getImports().add("ca.venom.ceph.protocol.DecodingException");
        context.getImports().add("io.netty.buffer.ByteBuf");
        context.getImports().add("java.nio.charset.StandardCharsets");
        String className = encodableClass.getClassName() + "Encodable";

        JavaFileObject sourceFile = filer.createSourceFile(
                packageName + "." + className
        );

        try (PrintWriter out = new PrintWriter(sourceFile.openWriter())) {
            String encodeMethod;
            String decodeMethod;
            if ("ca.venom.ceph.protocol.messages.MessagePayload"
                    .equals(encodableClass.getPackageName() + "." + encodableClass.getClassName())) {
                encodeMethod = generateMessagePayloadEncodeMethod(encodableClass);
                decodeMethod = generateMessagePayloadDecodeMethod(encodableClass);
            } else if (encodableClass.getParentType() != null) {
                encodeMethod = generateAbstractEncodeMethod(encodableClass);
                decodeMethod = generateAbstractDecodeMethod(encodableClass);
            } else {
                encodeMethod = generateEncodeMethod(encodableClass);
                decodeMethod = generateDecodeMethod(encodableClass);
            }

            out.println("package " + packageName + ";");
            out.println();

            for (String importEntry : context.getImports()) {
                out.println("import " + importEntry + ";");
            }
            out.println();

            out.println("public class " + className + " {");
            out.println(encodeMethod);
            out.print(decodeMethod);
            out.println("}\n");
        }
    }

    private String generateAbstractEncodeMethod(EncodableClass encodableClass) {
        StringBuilder sb = new StringBuilder();

        sb.append("    public static void encode(");
        sb.append(encodableClass.getPackageName());
        sb.append(".");
        sb.append(encodableClass.getClassName());
        sb.append(" toEncode, ByteBuf byteBuf, boolean le) {\n");

        boolean isFirst = true;
        for (ChildTypeSimple childType : encodableClass.getChildTypes()) {
            sb.append(String.format(
                    "        %sif (toEncode instanceof %s toEncodeImpl) {\n",
                    isFirst ? "" : "} else ",
                    childType.getClassName()
            ));
            String packageName = childType.getClassName().substring(0, childType.getClassName().lastIndexOf('.'));
            String className = childType.getClassName().substring(childType.getClassName().lastIndexOf('.') + 1);
            sb.append(String.format(
                    "            %s.%sEncodable.encode(toEncodeImpl, byteBuf, le);\n",
                    getEncodingPackageName(packageName),
                    className
            ));

            isFirst = false;
        }

        if (!isFirst) {
            sb.append("        }\n");
        }

        sb.append("    }\n");

        return sb.toString();
    }

    private String generateAbstractDecodeMethod(EncodableClass encodableClass) {
        StringBuilder sb = new StringBuilder();

        if (encodableClass.isUseTypeCodeParameter()) {
            sb.append(String.format(
                    "    public static %s.%s decode(ByteBuf byteBuf, boolean le, int typeCode) throws DecodingException {\n",
                    encodableClass.getPackageName(),
                    encodableClass.getClassName()
            ));
        } else {
            sb.append(String.format(
                    "    public static %s.%s decode(ByteBuf byteBuf, boolean le) throws DecodingException {\n",
                    encodableClass.getPackageName(),
                    encodableClass.getClassName()
            ));

            switch (encodableClass.getParentType().typeSize()) {
                case 1 -> sb.append(String.format(
                        "        int typeCode = byteBuf.getByte(byteBuf.readerIndex() + %d);\n",
                        encodableClass.getParentType().typeOffset()
                ));
                case 2 -> sb.append(String.format(
                        "        int typeCode = le ? byteBuf.getShortLE(byteBuf.readerIndex() + %d) : byteBuf.getShort(byteBuf.readerIndex() + %d);\n",
                        encodableClass.getParentType().typeOffset(),
                        encodableClass.getParentType().typeOffset()
                ));
                default -> sb.append(String.format(
                        "        int typeCode = le ? byteBuf.getIntLE(byteBuf.readerIndex() + %d) : byteBuf.getInt(byteBuf.readerIndex() + %d);\n",
                        encodableClass.getParentType().typeOffset(),
                        encodableClass.getParentType().typeOffset()
                ));
            }
        }

        boolean isFirst = true;
        ChildTypeSimple defaultChildType = null;
        for (ChildTypeSimple childType : encodableClass.getChildTypes()) {
            if (childType.isDefault()) {
                defaultChildType = childType;
                continue;
            }

            sb.append(String.format(
                    "        %sif (%d == typeCode) {\n",
                    isFirst ? "" : "} else ",
                    childType.getTypeCode()
            ));
            String packageName = childType.getClassName().substring(0, childType.getClassName().lastIndexOf('.'));
            String className = childType.getClassName().substring(childType.getClassName().lastIndexOf('.') + 1);
            sb.append(String.format(
                    "            return %s.%sEncodable.decode(byteBuf, le);\n",
                    getEncodingPackageName(packageName),
                    className
            ));

            isFirst = false;
        }

        if (isFirst) {
            sb.append("        throw new IllegalArgumentException(\"Unknown type code read\");");
        } else if (defaultChildType != null) {
            sb.append("        } else {\n");
            String packageName = defaultChildType.getClassName().substring(0, defaultChildType.getClassName().lastIndexOf('.'));
            String className = defaultChildType.getClassName().substring(defaultChildType.getClassName().lastIndexOf('.') + 1);
            sb.append(String.format(
                    "            return %s.%sEncodable.decode(byteBuf, le);\n",
                    getEncodingPackageName(packageName),
                    className
            ));
            sb.append("        }\n");
        } else {
            sb.append("        } else {\n");
            sb.append("            throw new IllegalArgumentException(\"Unknown type code read: \" + typeCode);\n");
            sb.append("        }\n");
        }

        sb.append("    }\n");

        return sb.toString();
    }

    private String generateMessagePayloadEncodeMethod(EncodableClass encodableClass) {
        StringBuilder sb = new StringBuilder();

        sb.append("    public static void encode(");
        sb.append(encodableClass.getPackageName());
        sb.append(".");
        sb.append(encodableClass.getClassName());
        sb.append(" toEncode, ByteBuf byteBuf, boolean le) {\n");

        boolean first = true;
        for (Map.Entry<MessageType, String> messageTypeClass : context.getMessageTypeClasses().entrySet()) {
            if (first) {
                first = false;
                sb.append("        ");
            } else {
                sb.append("        } else ");
            }
            sb.append(String.format("if (toEncode instanceof %s) {\n", messageTypeClass.getValue()));

            String packageName = messageTypeClass.getValue().substring(0, messageTypeClass.getValue().lastIndexOf('.'));
            String className = messageTypeClass.getValue().substring(packageName.length() + 1);
            packageName = getEncodingPackageName(packageName);
            sb.append(String.format("            %s.%sEncodable.encode((%s) toEncode, byteBuf, le);\n",
                    packageName,
                    className,
                    messageTypeClass.getValue()));
        }

        if (!first) {
            sb.append("        }\n");
        }

        sb.append("    }\n");

        return sb.toString();
    }

    private String generateMessagePayloadDecodeMethod(EncodableClass encodableClass) {
        StringBuilder sb = new StringBuilder();

        sb.append("    public static ca.venom.ceph.protocol.messages.MessagePayload ");
        sb.append("decode(ByteBuf byteBuf, boolean le, int typeCode) throws DecodingException {\n");
        sb.append("        switch (ca.venom.ceph.types.MessageType.getFromCode(typeCode)) {\n");

        for (Map.Entry<MessageType, String> messageTypeClass : context.getMessageTypeClasses().entrySet()) {
            String packageName = messageTypeClass.getValue().substring(0, messageTypeClass.getValue().lastIndexOf('.'));
            String className = messageTypeClass.getValue().substring(packageName.length() + 1);
            packageName = getEncodingPackageName(packageName);

            sb.append(String.format("        case %s:\n", messageTypeClass.getKey().name()));
            sb.append(String.format("            return %s.%sEncodable.decode(byteBuf, le);\n",
                    packageName,
                    className));
        }

        sb.append("        }\n");
        sb.append("        return null;\n");
        sb.append("    }\n");

        return sb.toString();
    }

    private String generateEncodeMethod(EncodableClass encodableClass) {
        StringBuilder sb = new StringBuilder();

        sb.append("    public static void encode(");
        sb.append(encodableClass.getPackageName());
        sb.append(".");
        sb.append(encodableClass.getClassName());
        sb.append(" toEncode, ByteBuf byteBuf, boolean le) {\n");

        if (encodableClass.getMarker() != null) {
            sb.append("        byteBuf.writeByte((byte) ");
            sb.append(encodableClass.getMarker().toString());
            sb.append(");\n");
        }

        if (encodableClass.getVersion() != null) {
            sb.append("        byteBuf.writeByte((byte) ");
            sb.append(encodableClass.getVersion().toString());
            sb.append(");\n");

            if (encodableClass.getCompatVersion() != null) {
                sb.append("        byteBuf.writeByte((byte) ");
                sb.append(encodableClass.getCompatVersion());
                sb.append(");\n");
            }
        }

        if (encodableClass.isIncludeSize()) {
            sb.append("        int sizeIndex = byteBuf.writerIndex();\n");
            sb.append("        byteBuf.writeZero(4);\n");
        }

        sb.append("        if (toEncode == null) {\n");
        sb.append("            return;\n");
        sb.append("        }\n");
        sb.append("\n");

        if ("ca.venom.ceph.protocol.frames.MessageFrame"
                .equals(encodableClass.getPackageName() + "." + encodableClass.getClassName())) {
            boolean first = true;

            for (Map.Entry<MessageType, String> messageTypeClass : context.getMessageTypeClasses().entrySet()) {
                if (first) {
                    first = false;
                    sb.append("        ");
                } else {
                    sb.append("        } else ");
                }

                sb.append(String.format("if (toEncode instanceof %s) {\n", messageTypeClass.getValue()));
                sb.append(String.format("            toEncode.getHead().setType((short) %s.getValueInt());\n",
                        messageTypeClass.getKey()));
            }

            if (!first) {
                sb.append("        }\n");
            }
        }

        for (EncodableField field : encodableClass.getFields()) {
            FieldCodeGenerator fieldCodeGenerator = FieldCodeGenerator.getFieldCodeGenerator(
                    context,
                    field,
                    field.getType()
            );
            if (fieldCodeGenerator != null) {
                if (field.getCondition() == null) {
                    fieldCodeGenerator.generateEncodeJavaCode(
                            sb,
                            field,
                            2,
                            "toEncode",
                            field.getType()
                    );
                } else {
                    sb.append(String.format(
                            "        if (toEncode.%s == %s) {\n",
                            field.getCondition().getProperty(),
                            field.getCondition().getValues()[0]
                    ));
                    fieldCodeGenerator.generateEncodeJavaCode(
                            sb,
                            field,
                            3,
                            "toEncode",
                            field.getType()
                    );
                    sb.append("        }\n");
                }
            }
        }

        if (encodableClass.isIncludeSize()) {
            sb.append("        if (le) {\n");
            sb.append("            byteBuf.setIntLE(sizeIndex, byteBuf.writerIndex() - sizeIndex - 4);\n");
            sb.append("        } else {\n");
            sb.append("            byteBuf.setInt(sizeIndex, byteBuf.writerIndex() - sizeIndex - 4);\n");
            sb.append("        }\n");
        }

        if (encodableClass.getPadToSizeOfClass() != null) {
            Integer currentSize =
                    getFixedSize(encodableClass.getPackageName() + "." + encodableClass.getClassName());
            Integer otherSize = getFixedSize(encodableClass.getPadToSizeOfClass());

            if (currentSize != null && otherSize != null && otherSize > currentSize) {
                sb.append(String.format(
                        "        byteBuf.writeZero(%d);\n",
                        otherSize - currentSize
                ));
            }
        }

        sb.append("    }\n");

        return sb.toString();
    }

    private String generateDecodeMethod(EncodableClass encodableClass) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format(
                "    public static %s.%s decode(ByteBuf byteBuf, boolean le) throws DecodingException {\n",
                encodableClass.getPackageName(),
                encodableClass.getClassName()
        ));
        sb.append(String.format(
                "        %s.%s toDecode = new %s.%s();\n",
                encodableClass.getPackageName(),
                encodableClass.getClassName(),
                encodableClass.getPackageName(),
                encodableClass.getClassName()
        ));

        if (encodableClass.getMarker() != null) {
            sb.append(String.format(
                    "        if ((byte) %d != byteBuf.readByte()) {\n",
                    encodableClass.getMarker()
            ));
            sb.append("            throw new DecodingException(\"Invalid marker value\");\n");
            sb.append("        }\n");
        }

        if (encodableClass.getVersion() != null) {
            sb.append(String.format(
                    "        if ((byte) %d != byteBuf.readByte()) {\n",
                    encodableClass.getVersion()
            ));
            sb.append("            throw new DecodingException(\"Unsupported version\");\n");
            sb.append("        }\n");

            if (encodableClass.getCompatVersion() != null) {
                sb.append(String.format(
                        "        if ((byte) %d != byteBuf.readByte()) {\n",
                        encodableClass.getCompatVersion()
                ));
                sb.append("            throw new DecodingException(\"Unsupported compat version\");\n");
                sb.append("        }\n");
            }
        }

        if (encodableClass.isIncludeSize()) {
            sb.append("        if (le) {\n");
            sb.append("            if (byteBuf.readIntLE() > byteBuf.readableBytes()) {\n");
            sb.append("                throw new DecodingException(\"Not enough bytes available\");\n");
            sb.append("            }\n");
            sb.append("        } else {\n");
            sb.append("            if (byteBuf.readInt() > byteBuf.readableBytes()) {\n");
            sb.append("                throw new DecodingException(\"Not enough bytes available\");\n");
            sb.append("            }\n");
            sb.append("        }\n");
        }

        for (EncodableField field : encodableClass.getFields()) {
            FieldCodeGenerator fieldCodeGenerator = FieldCodeGenerator.getFieldCodeGenerator(
                    context,
                    field,
                    field.getType()
            );
            if (fieldCodeGenerator == null) {
                context.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to decode field");
            } else {
                if (field.getCondition() == null) {
                    fieldCodeGenerator.generateDecodeJavaCode(
                            sb,
                            field,
                            2,
                            "toDecode",
                            field.getType()
                    );
                } else {
                    sb.append(String.format(
                            "        if (toDecode.%s == %s) {\n",
                            field.getCondition().getProperty(),
                            field.getCondition().getValues()[0]
                    ));
                    fieldCodeGenerator.generateEncodeJavaCode(
                            sb,
                            field,
                            3,
                            "toDecode",
                            field.getType()
                    );
                    sb.append("        }\n");
                }
            }
        }

        if (encodableClass.getPadToSizeOfClass() != null) {
            Integer currentSize =
                    getFixedSize(encodableClass.getPackageName() + "." + encodableClass.getClassName());
            Integer otherSize = getFixedSize(encodableClass.getPadToSizeOfClass());

            if (currentSize != null && otherSize != null && otherSize > currentSize) {
                sb.append(String.format(
                        "        byteBuf.skipBytes(%d);\n",
                        otherSize - currentSize
                ));
            }
        }

        sb.append("        return toDecode;\n");
        sb.append("    }\n");

        return sb.toString();
    }
}
