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

import ca.venom.ceph.encoding.annotations.ByteOrderPreference;
import ca.venom.ceph.encoding.annotations.CephChildType;
import ca.venom.ceph.encoding.annotations.CephChildTypes;
import ca.venom.ceph.encoding.annotations.CephEncodingSize;
import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephMarker;
import ca.venom.ceph.encoding.annotations.CephParentType;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephTypeSize;
import ca.venom.ceph.encoding.annotations.CephTypeVersion;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({
        "ca.venom.ceph.encoding.annotations.CephType"
})
public class EncodingAnnotationProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, EncodableClass> encodableClasses = new HashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(CephType.class)) {
            EncodableClass encodableClass = getEncodableClass(element);
            encodableClasses.put(
                    encodableClass.getPackageName() + "." + encodableClass.getClassName(),
                    encodableClass);
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(CephField.class)) {
            TypeMirror elementType = element.asType();
            EncodableClass encodableClass = encodableClasses.get(element.getEnclosingElement().asType().toString());
            if (encodableClass == null) {
                continue;
            }

            EncodableField encodableField = new EncodableField();
            encodableField.setName(element.toString());
            encodableField.setType(element.asType().toString());

            try {
                if (elementType instanceof ClassType) {
                    com.sun.tools.javac.util.List<com.sun.tools.javac.code.Type> interfaces = ((ClassType) elementType).interfaces_field;
                    if (interfaces != null) {
                        encodableField.setInterfaces(
                                interfaces
                                        .stream()
                                        .map(Type::toString)
                                        .collect(Collectors.toList())
                        );
                    }
                }
            } catch (Throwable th) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream exceptionStream = new PrintStream(baos);
                th.printStackTrace(exceptionStream);
                exceptionStream.close();

                messager.printMessage(Diagnostic.Kind.ERROR, baos.toString(StandardCharsets.UTF_8));
            }

            CephField field = element.getAnnotation(CephField.class);
            encodableField.setOrder(field.order());
            encodableField.setByteOrderPreference(field.byteOrderPreference());
            encodableField.setIncludeSize(field.includeSize());
            encodableField.setSizeLength(field.sizeLength());

            CephEncodingSize encodingSize = element.getAnnotation(CephEncodingSize.class);
            if (encodingSize != null) {
                encodableField.setEncodingSize(encodingSize.value());
            }

            CephTypeSize typeSize = element.getAnnotation(CephTypeSize.class);
            if (typeSize != null) {
                encodableField.setIncludeSize(true);
            }

            encodableClass.getFields().add(encodableField);
        }

        for (EncodableClass encodableClass : encodableClasses.values()) {
            encodableClass.getFields().sort(Comparator.comparingInt(EncodableField::getOrder));
        }

        try {
            for (EncodableClass encodableClass : encodableClasses.values()) {
                generateEncodingSource(encodableClass, encodableClasses);
            }
        } catch (Throwable th) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream exceptionStream = new PrintStream(baos);
            th.printStackTrace(exceptionStream);
            exceptionStream.close();

            messager.printMessage(Diagnostic.Kind.ERROR, baos.toString(StandardCharsets.UTF_8));
        }

        return false;
    }

    private EncodableClass getEncodableClass(Element element) {
        String fullName = element.asType().toString();
        int lastPeriodIndex = fullName.lastIndexOf('.');
        String packageName = fullName.substring(0, lastPeriodIndex);
        String className = fullName.substring(lastPeriodIndex + 1);

        EncodableClass encodableClass = new EncodableClass();
        encodableClass.setPackageName(packageName);
        encodableClass.setClassName(className);

        CephParentType parentType = element.getAnnotation(CephParentType.class);
        if (parentType != null) {
            encodableClass.setParentType(parentType);
        }
        CephChildTypes childTypes = element.getAnnotation(CephChildTypes.class);
        if (childTypes != null) {
            List<ChildTypeSimple> childTypesList = new ArrayList<>(childTypes.value().length);
            for (CephChildType childType : childTypes.value()) {
                ChildTypeSimple childTypeSimple = new ChildTypeSimple();
                childTypeSimple.setTypeCode(childType.typeValue());
                Pattern pattern = Pattern.compile(".*, typeClass=(.*)\\.class\\)$");
                Matcher matcher = pattern.matcher(childType.toString());
                if (matcher.matches()) {
                    childTypeSimple.setClassName(matcher.group(1));
                    childTypesList.add(childTypeSimple);
                }
            }

            encodableClass.setChildTypes(childTypesList);
        }

        CephMarker marker = element.getAnnotation(CephMarker.class);
        if (marker != null) {
            encodableClass.setMarker(marker.value());
        }

        CephTypeSize typeSize = element.getAnnotation(CephTypeSize.class);
        if (typeSize != null) {
            encodableClass.setIncludeSize(true);
        }

        CephTypeVersion typeVersion = element.getAnnotation(CephTypeVersion.class);
        if (typeVersion != null) {
            encodableClass.setVersion(typeVersion.version());
            if (typeVersion.compatVersion() > 0) {
                encodableClass.setCompatVersion(typeVersion.compatVersion());
            }
        }

        return encodableClass;
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

    private void generateEncodingSource(
            EncodableClass encodableClass,
            Map<String, EncodableClass> encodableClasses) throws IOException {
        String packageName = getEncodingPackageName(encodableClass.getPackageName());
        List<String> imports = new ArrayList<>();
        imports.add("ca.venom.ceph.protocol.DecodingException");
        imports.add("io.netty.buffer.ByteBuf");
        imports.add("java.nio.charset.StandardCharsets");
        imports.add("java.util.BitSet");
        imports.add("java.util.List");
        imports.add("java.util.Map");
        imports.add("java.util.Set");
        String className = encodableClass.getClassName() + "Encodable";

        JavaFileObject sourceFile = filer.createSourceFile(
                packageName + "." + className
        );

        try (PrintWriter out = new PrintWriter(sourceFile.openWriter())) {
            out.println("package " + packageName + ";");
            out.println();

            for (String importEntry : imports) {
                out.println("import " + importEntry + ";");
            }
            out.println();

            out.println("public class " + className + " {");
            if (encodableClass.getParentType() != null) {
                generateAbstractEncodeMethod(encodableClass, out);
            } else {
                generateEncodeMethod(encodableClass, encodableClasses.keySet(), out);
            }
            out.println();
            if (encodableClass.getParentType() != null) {
                generateAbstractDecodeMethod(encodableClass, out);
            } else {
                generateDecodeMethod(encodableClass, encodableClasses.keySet(), out);
            }
            out.println("}");
        }
    }

    private void generateAbstractEncodeMethod(EncodableClass encodableClass,
                                              PrintWriter out) {
        out.print("    public static void encode(");
        out.print(encodableClass.getPackageName() + ".");
        out.print(encodableClass.getClassName());
        out.println(" toEncode, ByteBuf byteBuf, boolean le) {");

        boolean isFirst = true;
        for (ChildTypeSimple childType : encodableClass.getChildTypes()) {
            out.println(String.format(
                    "%s%sif (toEncode instanceof %s toEncodeImpl) {",
                    getIndentString(2),
                    isFirst ? "" : "} else ",
                    childType.getClassName()
            ));
            String packageName = childType.getClassName().substring(0, childType.getClassName().lastIndexOf('.'));
            String className = childType.getClassName().substring(childType.getClassName().lastIndexOf('.') + 1);
            out.println(String.format(
                    "%s%s.%sEncodable.encode(toEncodeImpl, byteBuf, le);",
                    getIndentString(3),
                    getEncodingPackageName(packageName),
                    className
            ));

            isFirst = false;
        }

        if (!isFirst) {
            out.println("        }");
        }

        out.println("    }");
    }

    private void generateEncodeMethod(EncodableClass encodableClass,
                                      Set<String> encodableClasses,
                                      PrintWriter out) {
        out.print("    public static void encode(");
        out.print(encodableClass.getPackageName() + ".");
        out.print(encodableClass.getClassName());
        out.println(" toEncode, ByteBuf byteBuf, boolean le) {");

        if (encodableClass.getMarker() != null) {
            out.print("        byteBuf.writeByte((byte) ");
            out.print(encodableClass.getMarker().toString());
            out.println(");");
        }

        if (encodableClass.getVersion() != null) {
            out.print("        byteBuf.writeByte((byte) ");
            out.print(encodableClass.getVersion().toString());
            out.println(");");

            if (encodableClass.getCompatVersion() != null) {
                out.print("        byteBuf.writeByte((byte) ");
                out.print(encodableClass.getCompatVersion());
                out.println(");\n");
            }
        }

        if (encodableClass.isIncludeSize()) {
            out.println("        int sizeIndex = byteBuf.writerIndex();");
            out.println("        byteBuf.writeZero(4);");
        }

        out.println("        if (toEncode == null) {");
        out.println("            return;");
        out.println("        }");
        out.println();

        boolean first = true;
        for (EncodableField encodableField : encodableClass.getFields()) {
            if (first) {
                first = false;
            } else {
                out.println();
            }

            encodeField(
                    "toEncode",
                    encodableField.getType(),
                    2,
                    encodableField,
                    encodableClasses,
                    out);
        }

        if (encodableClass.isIncludeSize()) {
            out.println("        if (le) {");
            out.println("            byteBuf.setIntLE(sizeIndex, byteBuf.writerIndex() - sizeIndex - 4);");
            out.println("        } else {");
            out.println("            byteBuf.setInt(sizeIndex, byteBuf.writerIndex() - sizeIndex - 4);");
            out.println("        }");
        }

        out.println("    }");
    }

    private void encodeField(String variableName,
                             String variableType,
                             int indentation,
                             EncodableField encodableField,
                             Set<String> encodableClasses,
                             PrintWriter out) {
        if ("boolean".equals(variableType)) {
            encodeBooleanPrimitive(variableName, indentation, variableType, encodableField, out);
        } else if ("java.lang.Boolean".equals(variableType)) {
            encodeBoolean(variableName, indentation, variableType, encodableField, out);
        } else if ("byte".equals(variableType)) {
            encodeBytePrimitive(variableName, indentation, variableType, encodableField, out);
        } else if ("java.lang.Byte".equals(variableType)) {
            encodeByte(variableName, indentation, variableType, encodableField, out);
        } else if ("short".equals(variableType)) {
            encodeShortPrimitive(variableName, indentation, variableType, encodableField, out);
        } else if ("java.lang.Short".equals(variableType)) {
            encodeShort(variableName, indentation, variableType, encodableField, out);
        } else if ("int".equals(variableType)) {
            encodeIntPrimitive(variableName, indentation, variableType, encodableField, out);
        } else if ("java.lang.Integer".equals(variableType)) {
            encodeInt(variableName, indentation, variableType, encodableField, out);
        } else if ("long".equals(variableType)) {
            encodeLongPrimitive(variableName, indentation, variableType, encodableField, out);
        } else if ("java.lang.Long".equals(variableType)) {
            encodeLong(variableName, indentation, variableType, encodableField, out);
        } else if ("java.lang.String".equals(variableType)) {
            encodeString(variableName, indentation, variableType, encodableField, out);
        } else if ("byte[]".equals(variableType)) {
            encodeByteArray(variableName, indentation, variableType, encodableField, out);
        } else if (isEnum(encodableField)) {
            encodeEnum(variableName, indentation, variableType, encodableField, out);
        } else if ("java.util.BitSet".equals(variableType)) {
            encodeBitSet(variableName, indentation, variableType, encodableField, out);
        } else if (variableType.matches("^java\\.util\\.Set<.+>$")) {
            encodeSet(variableName, indentation, variableType, encodableField, encodableClasses, out);
        } else if (variableType.matches("^java\\.util\\.List<.+>$")) {
            encodeList(variableName, indentation, variableType, encodableField, encodableClasses, out);
        } else if (variableType.matches("^java\\.util\\.Map<[^,]+\s*,\s*(.+)>$")) {
            encodeMap(variableName, indentation, variableType, encodableField, encodableClasses, out);
        } else if (encodableClasses.contains(variableType)) {
            encodeEncodable(variableName, indentation, variableType, encodableField, out);
        }
    }

    private boolean isEnum(EncodableField encodableField) {
        return encodableField.getInterfaces() != null &&
                encodableField.getInterfaces().contains("ca.venom.ceph.protocol.EnumWithIntValue");
    }

    private String getGetterName(EncodableField encodableField, String typeName, String variableName, String fieldName) {
        if (encodableField.getType().equals(typeName)) {
            String prefix;
            if ("boolean".equals(typeName) || "java.lang.Boolean".equals(typeName)) {
                prefix = "is";
            } else {
                prefix = "get";
            }

            return String.format(
                    "%s.%s%s%s()",
                    variableName,
                    prefix,
                    fieldName.substring(0, 1).toUpperCase(),
                    fieldName.substring(1));
        } else {
            return variableName;
        }
    }

    private String getSetter(EncodableField encodableField,
                             String typeName,
                             String variableName,
                             String fieldName,
                             String value) {
        if (encodableField.getType().equals(typeName)) {
            String prefix = "set";
            return String.format(
                    "%s.%s%s%s(%s);",
                    variableName,
                    prefix,
                    fieldName.substring(0, 1).toUpperCase(),
                    fieldName.substring(1),
                    value);
        } else {
            return String.format(
                    "%s = %s;",
                    variableName,
                    value
            );
        }
    }

    private String getIndentString(int indentation) {
        return "    ".repeat(indentation);
    }

    private void encodeBooleanPrimitive(String variableName,
                                        int indentation,
                                        String type,
                                        EncodableField encodableField,
                                        PrintWriter out) {
        String getter = getGetterName(encodableField, type, variableName, encodableField.getName());

        out.println(String.format(
                "%sbyteBuf.writeByte(%s ? 1 : 0);",
                getIndentString(indentation),
                getter
        ));
    }

    private void encodeBoolean(String variableName,
                               int indentation,
                               String type,
                               EncodableField encodableField,
                               PrintWriter out) {
        String getter = getGetterName(encodableField, type, variableName, encodableField.getName());

        out.println(String.format(
                "%sbyteBuf.writeByte(Boolean.TRUE.equals(%s) ? 1 : 0);",
                getIndentString(indentation),
                getter
        ));
    }

    private void encodeBytePrimitive(String variableName,
                                     int indentation,
                                     String type,
                                     EncodableField encodableField,
                                     PrintWriter out) {
        String getter = getGetterName(encodableField, type, variableName, encodableField.getName());

        out.println(String.format(
                "%sbyteBuf.writeByte(%s);",
                getIndentString(indentation),
                getter
        ));
    }

    private void encodeByte(String variableName,
                            int indentation,
                            String type,
                            EncodableField encodableField,
                            PrintWriter out) {
        String getter = getGetterName(encodableField, type, variableName, encodableField.getName());

        out.println(String.format(
                "%sif (%s == null) {",
                getIndentString(indentation),
                getter
        ));
        out.println(String.format(
                "%sbyteBuf.writeByte(0);",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%s} else {",
                getIndentString(indentation)
        ));
        out.println(String.format(
                "%sbyteBuf.writeByte(%s);",
                getIndentString(indentation + 1),
                getter
        ));
        out.println(String.format(
                "%s}",
                getIndentString(indentation)
        ));
    }

    private void encodeShortPrimitive(String variableName,
                                      int indentation,
                                      String type,
                                      EncodableField encodableField,
                                      PrintWriter out) {
        String getter = getGetterName(encodableField, type, variableName, encodableField.getName());

        switch (encodableField.getByteOrderPreference()) {
            case NONE -> {
                out.println(String.format(
                        "%sif (le) {",
                        getIndentString(indentation)
                ));
                out.println(String.format(
                        "%sbyteBuf.writeShortLE(%s);",
                        getIndentString(indentation + 1),
                        getter
                ));
                out.println(String.format(
                        "%s} else {",
                        getIndentString(indentation)
                ));
                out.println(String.format(
                        "%sbyteBuf.writeShort(%s);",
                        getIndentString(indentation + 1),
                        getter
                ));
                out.println(String.format(
                        "%s}",
                        getIndentString(indentation)
                ));
            }
            case LE -> {
                out.println(String.format(
                        "%sbyteBuf.writeShortLE(%s);",
                        getIndentString(indentation),
                        getter
                ));
            }
            case BE -> {
                out.println(String.format(
                        "%sbyteBuf.writeShort(%s);",
                        getIndentString(indentation),
                        getter
                ));
            }
        }
    }

    private void encodeShort(String variableName,
                             int indentation,
                             String type,
                             EncodableField encodableField,
                             PrintWriter out) {
        String getter = getGetterName(encodableField, type, variableName, encodableField.getName());

        if (encodableField.getByteOrderPreference() == ByteOrderPreference.NONE) {
            out.println(String.format(
                    "%sif (le) {",
                    getIndentString(indentation)
            ));
            out.println(String.format(
                    "%sbyteBuf.writeShortLE(%s == null ? (short) 0 : %s);",
                    getIndentString(indentation + 1),
                    getter,
                    getter
            ));
            out.println(String.format(
                    "%s} else {",
                    getIndentString(indentation)
            ));
            out.println(String.format(
                    "%sbyteBuf.writeShort(%s == null ? (short) 0 : %s);",
                    getIndentString(indentation + 1),
                    getter,
                    getter
            ));
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation)
            ));
        } else {
            out.println(String.format(
                    "%sbyteBuf.writeShort%s(%s == null ? (short) 0 : %s);",
                    getIndentString(indentation),
                    (encodableField.getByteOrderPreference() == ByteOrderPreference.LE) ? "LE" : "",
                    getter,
                    getter
            ));
        }
    }

    private void encodeIntPrimitive(String variableName,
                                    int indentation,
                                    String type,
                                    EncodableField encodableField,
                                    PrintWriter out) {
        String getter = getGetterName(encodableField, type, variableName, encodableField.getName());

        if (encodableField.getByteOrderPreference() == ByteOrderPreference.NONE) {
            out.println(String.format(
                    "%sif (le) {",
                    getIndentString(indentation)
            ));
            out.println(String.format(
                    "%sbyteBuf.writeIntLE(%s);",
                    getIndentString(indentation + 1),
                    getter
            ));
            out.println(String.format(
                    "%s} else {",
                    getIndentString(indentation)
            ));
            out.println(String.format(
                    "%sbyteBuf.writeInt(%s);",
                    getIndentString(indentation + 1),
                    getter
            ));
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation)
            ));
        } else {
            out.println(String.format(
                    "%sbyteBuf.writeInt%s(%s);",
                    getIndentString(indentation),
                    (encodableField.getByteOrderPreference() == ByteOrderPreference.LE) ? "LE" : "",
                    variableName,
                    getter
            ));
        }
    }

    private void encodeInt(String variableName,
                           int indentation,
                           String type,
                           EncodableField encodableField,
                           PrintWriter out) {
        String getter = getGetterName(encodableField, type, variableName, encodableField.getName());

        if (encodableField.getByteOrderPreference() == ByteOrderPreference.NONE) {
            out.println(String.format(
                    "%sif (le) {",
                    getIndentString(indentation)
            ));
            out.println(String.format(
                    "%sbyteBuf.writeIntLE(%s == null ? 0 : %s);",
                    getIndentString(indentation + 1),
                    getter,
                    getter
            ));
            out.println(String.format(
                    "%s} else {",
                    getIndentString(indentation)
            ));
            out.println(String.format(
                    "%sbyteBuf.writeInt(%s == null ? 0 : %s);",
                    getIndentString(indentation + 1),
                    getter,
                    getter
            ));
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation)
            ));
        } else {
            out.println(String.format(
                    "%sbyteBuf.writeInt%s(%s == null ? 0 : %s);",
                    getIndentString(indentation),
                    (encodableField.getByteOrderPreference() == ByteOrderPreference.LE) ? "LE" : "",
                    getter,
                    getter
            ));
        }
    }

    private void encodeLongPrimitive(String variableName,
                                     int indentation,
                                     String type,
                                     EncodableField encodableField,
                                     PrintWriter out) {
        String getter = getGetterName(encodableField, type, variableName, encodableField.getName());

        if (encodableField.getByteOrderPreference() == ByteOrderPreference.NONE) {
            out.println(String.format(
                    "%sif (le) {",
                    getIndentString(indentation)
            ));
            out.println(String.format(
                    "%sbyteBuf.writeLongLE(%s);",
                    getIndentString(indentation + 1),
                    getter
            ));
            out.println(String.format(
                    "%s} else {",
                    getIndentString(indentation)
            ));
            out.println(String.format(
                    "%sbyteBuf.writeLong(%s);",
                    getIndentString(indentation + 1),
                    getter
            ));
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation)
            ));
        } else {
            out.println(String.format(
                    "%sbyteBuf.writeLong%s(%s);",
                    getIndentString(indentation),
                    (encodableField.getByteOrderPreference() == ByteOrderPreference.LE) ? "LE" : "",
                    getter
            ));
        }
    }

    private void encodeLong(String variableName,
                            int indentation,
                            String type,
                            EncodableField encodableField,
                            PrintWriter out) {
        String getter = getGetterName(encodableField, type, variableName, encodableField.getName());

        if (encodableField.getByteOrderPreference() == ByteOrderPreference.NONE) {
            out.println(String.format(
                    "%sif (le) {",
                    getIndentString(indentation)
            ));
            out.println(String.format(
                    "%sbyteBuf.writeLongLE(%s == null ? 0L : %s);",
                    getIndentString(indentation + 1),
                    getter,
                    getter
            ));
            out.println(String.format(
                    "%s} else {",
                    getIndentString(indentation)
            ));
            out.println(String.format(
                    "%sbyteBuf.writeLong(%s == null ? 0L : %s);",
                    getIndentString(indentation + 1),
                    getter,
                    getter
            ));
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation)
            ));
        } else {
            out.println(String.format(
                    "%sbyteBuf.writeLong%s(%s == null ? 0L : %s);",
                    getIndentString(indentation),
                    (encodableField.getByteOrderPreference() == ByteOrderPreference.LE) ? "LE" : "",
                    getter,
                    getter
            ));
        }
    }

    private void encodeString(String variableName,
                              int indentation,
                              String type,
                              EncodableField encodableField,
                              PrintWriter out) {
        String getter = getGetterName(encodableField, type, variableName, encodableField.getName());

        out.println(String.format(
                "%sif (%s == null) {",
                getIndentString(indentation),
                getter
        ));
        out.println(String.format(
                "%sbyteBuf.writeZero(4);",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%s} else {",
                getIndentString(indentation)
        ));
        out.println(String.format(
                "%sbyte[] strBytes = %s.getBytes(StandardCharsets.UTF_8);",
                getIndentString(indentation + 1),
                getter
        ));
        out.println(String.format(
                "%sif (le) {",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%sbyteBuf.writeIntLE(strBytes.length);",
                getIndentString(indentation + 2)
        ));
        out.println(String.format(
                "%s} else {",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%sbyteBuf.writeInt(strBytes.length);",
                getIndentString(indentation + 2)
        ));
        out.println(String.format(
                "%s}",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%sbyteBuf.writeBytes(strBytes);",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%s}",
                getIndentString(indentation)
        ));
    }

    private void encodeByteArray(String variableName,
                                 int indentation,
                                 String type,
                                 EncodableField encodableField,
                                 PrintWriter out) {
        String getter = getGetterName(encodableField, type, variableName, encodableField.getName());

        out.println(String.format(
                "%sif (%s != null) {",
                getIndentString(indentation),
                getter
        ));
        out.println(String.format(
                "%sbyte[] bytesToWrite = %s;",
                getIndentString(indentation + 1),
                getter
        ));
        if (encodableField.getByteOrderPreference() != ByteOrderPreference.NONE) {
            out.println(String.format(
                    "%sboolean needToReverse = %sle;",
                    getIndentString(indentation + 1),
                    (encodableField.getByteOrderPreference() == ByteOrderPreference.LE ? "!" : "")
            ));
            out.println(String.format(
                    "%sif (needToReverse) {",
                    getIndentString(indentation + 1)
            ));
            out.println(String.format(
                    "%sfor (int i = 0; i < bytesToWrite.length / 2; i++) {",
                    getIndentString(indentation + 2)
            ));
            out.println(String.format(
                    "%sbyte b = bytesToWrite[i];",
                    getIndentString(indentation + 3)
            ));
            out.println(String.format(
                    "%sbytesToWrite[i] = bytesToWrite[bytesToWrite.length - 1 - i];",
                    getIndentString(indentation + 3)
            ));
            out.println(String.format(
                    "%sbytesToWrite[bytesToWrite.length - 1 - i] = b;",
                    getIndentString(indentation + 3)
            ));
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation + 2)
            ));
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation + 1)
            ));
        }

        if (encodableField.isIncludeSize()) {
            if (encodableField.getByteOrderPreference() == ByteOrderPreference.NONE) {
                out.println(String.format(
                        "%sif (le) {",
                        getIndentString(indentation + 1)
                ));
                String functionName;
                switch (encodableField.getSizeLength()) {
                    case 1 -> functionName = "Byte((byte) ";
                    case 2 -> functionName = "ShortLE((short) ";
                    default -> functionName = "IntLE(";
                }
                out.println(String.format(
                        "%sbyteBuf.write%sbytesToWrite.length);",
                        getIndentString(indentation + 2),
                        functionName
                ));
                out.println(String.format(
                        "%s} else {",
                        getIndentString(indentation + 1)
                ));
                switch (encodableField.getSizeLength()) {
                    case 1 -> functionName = "Byte((byte) ";
                    case 2 -> functionName = "Short((short) ";
                    default -> functionName = "Int(";
                }
                out.println(String.format(
                        "%sbyteBuf.write%sbytesToWrite.length);",
                        getIndentString(indentation + 2),
                        functionName
                ));
                out.println(String.format(
                        "%s}",
                        getIndentString(indentation + 1)
                ));
            } else {
                String function;
                switch (encodableField.getSizeLength()) {
                    case 1 -> function = "Byte((byte) ";
                    case 2 -> function = "Short" + ((encodableField.getByteOrderPreference() == ByteOrderPreference.LE) ? "LE" : "");
                    default -> function = "Int" + ((encodableField.getByteOrderPreference() == ByteOrderPreference.LE ? "LE" : ""));
                }
                out.println(String.format(
                        "%sbyteBuf.write%sbytesToWrite.length);",
                        getIndentString(indentation + 2),
                        function
                ));
                out.println(String.format(
                        "%s}",
                        getIndentString(indentation + 1)
                ));
            }
        }

        out.println(String.format(
                "%sbyteBuf.writeBytes(bytesToWrite);",
                getIndentString(indentation + 1)
        ));

        if (encodableField.isIncludeSize()) {
            out.println(String.format(
                    "%s} else {",
                    getIndentString(indentation)
            ));
            out.println(String.format(
                    "%sbyteBuf.writeZero(%d);",
                    getIndentString(indentation + 1),
                    encodableField.getSizeLength()
            ));
        }

        out.println(String.format(
                "%s}",
                getIndentString(indentation)
        ));
    }

    private void encodeEnum(String variableName,
                            int indentation,
                            String type,
                            EncodableField encodableField,
                            PrintWriter out) {
        int encodingSize = 1;
        if (encodableField.getEncodingSize() != null) {
            encodingSize = encodableField.getEncodingSize();
        }
        String getter = getGetterName(encodableField, type, variableName, encodableField.getName());

        if (encodingSize == 1) {
            out.println(String.format(
                    "%sbyteBuf.writeByte((byte) %s.getValueInt());",
                    getIndentString(indentation),
                    getter
            ));
        } else {
            out.println(String.format(
                    "%sif (le) {",
                    getIndentString(indentation)
            ));
            out.println(String.format(
                    "%sbyteBuf.write%s%s.getValueInt());",
                    getIndentString(indentation + 1),
                    (encodingSize == 2) ? "ShortLE((short) " : "IntLE(",
                    getter
            ));
            out.println(String.format(
                    "%s} else {",
                    getIndentString(indentation)
            ));
            out.println(String.format(
                    "%sbyteBuf.write%s%s.getValueInt());",
                    getIndentString(indentation + 1),
                    (encodingSize == 2) ? "Short((short) " : "Int(",
                    getter
            ));
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation)
            ));
        }
    }

    private void encodeBitSet(String variableName,
                              int indentation,
                              String type,
                              EncodableField encodableField,
                              PrintWriter out) {
        String getter = getGetterName(encodableField, type, variableName, encodableField.getName());
        int encodingSize = 1;
        if (encodableField.getEncodingSize() != null) {
            encodingSize = encodableField.getEncodingSize();
        }

        out.println(String.format(
                "%sif (%s == null) {",
                getIndentString(indentation),
                getter
        ));
        out.println(String.format(
                "%sbyteBuf.writeZero(%d);",
                getIndentString(indentation + 1),
                encodingSize
        ));
        out.println(String.format(
                "%s} else {",
                getIndentString(indentation)
        ));
        out.println(String.format(
                "%sbyte[] bytes = new byte[%d];",
                getIndentString(indentation + 1),
                encodingSize
        ));
        out.println(String.format(
                "%sbyte[] bitsetBytes = %s.toByteArray();",
                getIndentString(indentation + 1),
                getter
        ));
        out.println(String.format(
                "%sif (le) {",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%sSystem.arraycopy(bitsetBytes, 0, bytes, 0, Math.min(%d, bitsetBytes.length));",
                getIndentString(indentation + 2),
                encodingSize
        ));
        out.println(String.format(
                "%s} else {",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%sfor (int i = 0; i < Math.min(%d, bitsetBytes.length); i++) {",
                getIndentString(indentation + 2),
                encodingSize / 2
        ));
        out.println(String.format(
                "%sbytes[%d - i - 1] = bitsetBytes[i];",
                getIndentString(indentation + 3),
                encodingSize
        ));
        out.println(String.format(
                "%s}",
                getIndentString(indentation + 2)
        ));
        out.println(String.format(
                "%s}",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%sbyteBuf.writeBytes(bytes);",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%s}",
                getIndentString(indentation)
        ));
    }

    private void encodeSet(String variableName,
                           int indentation,
                           String type,
                           EncodableField encodableField,
                           Set<String> encodableClasses,
                           PrintWriter out) {
        String getter = getGetterName(encodableField, type, variableName, encodableField.getName());

        out.println(String.format(
                "%sif (%s == null) {",
                getIndentString(indentation),
                getter
        ));
        out.println(String.format(
                "%sbyteBuf.writeZero(4);",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%s} else {",
                getIndentString(indentation)
        ));
        out.println(String.format(
                "%sif (le) {",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%sbyteBuf.writeIntLE(%s.size());",
                getIndentString(indentation + 2),
                getter
        ));
        out.println(String.format(
                "%s} else {",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%sbyteBuf.writeInt(%s.size());",
                getIndentString(indentation + 2),
                getter
        ));
        out.println(String.format(
                "%s}",
                getIndentString(indentation + 1)
        ));

        Pattern pattern = Pattern.compile("^java\\.util\\.Set<(.+)>$");
        Matcher matcher = pattern.matcher(type);
        if (matcher.matches()) {
            String innerType = matcher.group(1);
            out.println(String.format(
                    "%sfor (%s element : %s) {",
                    getIndentString(indentation + 1),
                    innerType,
                    getter
            ));
            encodeField(
                    "element",
                    innerType,
                    indentation + 2,
                    encodableField,
                    encodableClasses,
                    out);
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation + 1)
            ));
        }

        out.println(String.format(
                "%s}",
                getIndentString(indentation)
        ));
    }

    private void encodeList(String variableName,
                            int indentation,
                            String type,
                            EncodableField encodableField,
                            Set<String> encodableClasses,
                            PrintWriter out) {
        String getter = getGetterName(encodableField, type, variableName, encodableField.getName());

        out.println(String.format(
                "%sif (%s == null) {",
                getIndentString(indentation),
                getter
        ));
        out.println(String.format(
                "%sbyteBuf.writeZero(4);",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%s} else {",
                getIndentString(indentation)
        ));
        out.println(String.format(
                "%sif (le) {",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%sbyteBuf.writeIntLE(%s.size());",
                getIndentString(indentation + 2),
                getter
        ));
        out.println(String.format(
                "%s} else {",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%sbyteBuf.writeInt(%s.size());",
                getIndentString(indentation + 2),
                getter
        ));
        out.println(String.format(
                "%s}",
                getIndentString(indentation + 1)
        ));

        Pattern pattern = Pattern.compile("^java\\.util\\.List<(.+)>$");
        Matcher matcher = pattern.matcher(type);
        if (matcher.matches()) {
            String innerType = matcher.group(1);
            out.println(String.format(
                    "%sfor (%s element : %s) {",
                    getIndentString(indentation + 1),
                    innerType,
                    getter
            ));
            encodeField(
                    "element",
                    innerType,
                    indentation + 2,
                    encodableField,
                    encodableClasses,
                    out);
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation + 1)
            ));
        }

        out.println(String.format(
                "%s}",
                getIndentString(indentation)
        ));
    }

    private void encodeMap(String variableName,
                           int indentation,
                           String type,
                           EncodableField encodableField,
                           Set<String> encodableClasses,
                           PrintWriter out) {
        String getter = getGetterName(encodableField, type, variableName, encodableField.getName());

        out.println(String.format(
                "%sif (%s == null) {",
                getIndentString(indentation),
                getter
        ));
        out.println(String.format(
                "%sbyteBuf.writeZero(4);",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%s} else {",
                getIndentString(indentation)
        ));
        out.println(String.format(
                "%sif (le) {",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%sbyteBuf.writeIntLE(%s.size());",
                getIndentString(indentation + 2),
                getter
        ));
        out.println(String.format(
                "%s} else {",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%sbyteBuf.writeInt(%s.size());",
                getIndentString(indentation + 2),
                getter
        ));
        out.println(String.format(
                "%s}",
                getIndentString(indentation + 1)
        ));

        Pattern pattern = Pattern.compile("^java\\.util\\.Map<([^,]+)\s*,\s*(.+)>$");
        Matcher matcher = pattern.matcher(type);
        if (matcher.matches()) {
            String keyType = matcher.group(1);
            String valueType = matcher.group(2);
            out.println(String.format(
                    "%sfor (Map.Entry<%s, %s> item : %s.entrySet()) {",
                    getIndentString(indentation + 1),
                    keyType,
                    valueType,
                    getter
            ));
            out.println(String.format(
                    "%s%s key = item.getKey();",
                    getIndentString(indentation + 2),
                    keyType
            ));
            out.println(String.format(
                    "%s%s value = item.getValue();",
                    getIndentString(indentation + 2),
                    valueType
            ));
            encodeField(
                    "key",
                    keyType,
                    indentation + 2,
                    encodableField,
                    encodableClasses,
                    out);
            encodeField(
                    "value",
                    valueType,
                    indentation + 2,
                    encodableField,
                    encodableClasses,
                    out);
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation + 1)
            ));
        }

        out.println(String.format(
                "%s}",
                getIndentString(indentation)
        ));
    }

    private void encodeEncodable(String variableName,
                                 int indentation,
                                 String type,
                                 EncodableField encodableField,
                                 PrintWriter out) {
        String packageName = type.substring(0, type.lastIndexOf('.'));
        String className = type.substring(packageName.length() + 1);
        packageName = getEncodingPackageName(packageName);
        String getter = getGetterName(encodableField, type, variableName, encodableField.getName());

        out.println(String.format(
                "%s%s.%sEncodable.encode(%s, byteBuf, le);",
                getIndentString(indentation),
                packageName,
                className,
                getter
        ));
    }

    private void generateAbstractDecodeMethod(EncodableClass encodableClass,
                                              PrintWriter out) {
        out.println(String.format(
                "%spublic static %s.%s decode(ByteBuf byteBuf, boolean le) throws DecodingException {",
                getIndentString(1),
                encodableClass.getPackageName(),
                encodableClass.getClassName()
        ));

        switch (encodableClass.getParentType().typeSize()) {
            case 1 -> out.println(String.format(
                    "%sint typeCode = byteBuf.getByte(byteBuf.readerIndex() + %d);",
                    getIndentString(2),
                    encodableClass.getParentType().typeOffset()
            ));
            case 2 -> out.println(String.format(
                    "%sint typeCode = le ? byteBuf.getShortLE(byteBuf.readerIndex() + %d) : byteBuf.getShort(byteBuf.readerIndex() + %d);",
                    getIndentString(2),
                    encodableClass.getParentType().typeOffset(),
                    encodableClass.getParentType().typeOffset()
            ));
            default -> out.println(String.format(
                    "%sint typeCode = le ? byteBuf.getIntLE(byteBuf.readerIndex() + %d) : byteBuf.getInt(byteBuf.readerIndex() + %d);",
                    getIndentString(2),
                    encodableClass.getParentType().typeOffset(),
                    encodableClass.getParentType().typeOffset()
            ));
        }

        boolean isFirst = true;
        for (ChildTypeSimple childType : encodableClass.getChildTypes()) {
            out.println(String.format(
                    "%s%sif (%d == typeCode) {",
                    getIndentString(2),
                    isFirst ? "" : "} else ",
                    childType.getTypeCode()
            ));
            String packageName = childType.getClassName().substring(0, childType.getClassName().lastIndexOf('.'));
            String className = childType.getClassName().substring(childType.getClassName().lastIndexOf('.') + 1);
            out.println(String.format(
                    "%sreturn %s.%sEncodable.decode(byteBuf, le);",
                    getIndentString(3),
                    getEncodingPackageName(packageName),
                    className
            ));

            isFirst = false;
        }

        if (isFirst) {
            out.println(String.format(
                    "%sthrow new DecodingException(\"Unknown type code read\");",
                    getIndentString(2)
            ));
        } else {
            out.println(String.format(
                    "%s} else {",
                    getIndentString(2)
            ));
            out.println(String.format(
                    "%sthrow new DecodingException(\"Unknown type code read\");",
                    getIndentString(3)
            ));
            out.println(String.format(
                    "%s}",
                    getIndentString(2)
            ));
        }

        out.println("    }");
    }

    private void generateDecodeMethod(EncodableClass encodableClass,
                                      Set<String> encodableClasses,
                                      PrintWriter out) {
        out.println(String.format(
                "    public static %s.%s decode(ByteBuf byteBuf, boolean le) throws DecodingException {",
                encodableClass.getPackageName(),
                encodableClass.getClassName()
        ));

        int bytesRead = 0;

        if (encodableClass.getMarker() != null) {
            out.println(String.format(
                    "        if ((byte) %d != byteBuf.readByte()) {",
                    encodableClass.getMarker()
            ));
            out.println("            throw new DecodingException(\"Invalid marker value\");");
            out.println("        }");
            bytesRead++;
        }

        if (encodableClass.getVersion() != null) {
            out.println(String.format(
                    "        if ((byte) %d != byteBuf.readByte()) {",
                    encodableClass.getVersion()
            ));
            out.println("            throw new DecodingException(\"Unsupported version\");");
            out.println("        }");
            bytesRead++;

            if (encodableClass.getCompatVersion() != null) {
                out.println(String.format(
                        "        if ((byte) %d != byteBuf.readByte()) {",
                        encodableClass.getCompatVersion()
                ));
                out.println("            throw new DecodingException(\"Unsupported compat version\");");
                out.println("        }");
                bytesRead++;
            }
        }

        if (encodableClass.isIncludeSize()) {
            out.println("        if (le) {");
            out.println("            if (byteBuf.readIntLE() > byteBuf.readableBytes()) {");
            out.println("                throw new DecodingException(\"Not enough bytes available\");");
            out.println("            }");
            out.println("        } else {");
            out.println("            if (byteBuf.readInt() > byteBuf.readableBytes()) {");
            out.println("                throw new DecodingException(\"Not enough bytes available\");");
            out.println("            }");
            out.println("        }");
            bytesRead += 4;
        }

        if (encodableClass.getParentType() != null) {
            switch (encodableClass.getParentType().typeSize()) {
                case 1 -> out.println(String.format(
                        "%sint typeCode = byteBuf.getByte(byteBuf.readerIndex() + %d);",
                        getIndentString(2),
                        encodableClass.getParentType().typeOffset() - bytesRead
                ));
                case 2 -> out.println(String.format(
                        "%sint typeCode = le ? byteBuf.getShortLE(byteBuf.readerIndex() + %d) : byteBuf.getShort(byteBuf.readerIndex() + %d);",
                        getIndentString(2),
                        encodableClass.getParentType().typeOffset() - bytesRead,
                        encodableClass.getParentType().typeOffset() - bytesRead
                ));
                default -> out.println(String.format(
                        "%sint typeCode = le ? byteBuf.getIntLE(byteBuf.readerIndex() + %d) : byteBuf.getInt(byteBuf.readerIndex() + %d);",
                        getIndentString(2),
                        encodableClass.getParentType().typeOffset() - bytesRead,
                        encodableClass.getParentType().typeOffset() - bytesRead
                ));
            }

            boolean isFirst = true;
            for (ChildTypeSimple childType : encodableClass.getChildTypes()) {
                out.println(String.format(
                        "%s%sif (%d == typeCode) {",
                        getIndentString(2),
                        isFirst ? "" : "} else ",
                        childType.getTypeCode()
                ));

                String packageName = childType.getClassName().substring(0, childType.getClassName().lastIndexOf('.'));
                String className = childType.getClassName().substring(childType.getClassName().lastIndexOf('.') + 1);
                out.println(String.format(
                        "%sreturn %s.%sEncodable.decode(byteBuf, le);",
                        getIndentString(3),
                        getEncodingPackageName(packageName),
                        className
                ));

                isFirst = false;
            }

            if (!encodableClass.getChildTypes().isEmpty()) {
                out.println(String.format(
                        "%s} else {",
                        getIndentString(2)
                ));
                out.println(String.format(
                        "%sthrow new DecodingException(\"Unknown type code read\");",
                        getIndentString(3)
                ));
                out.println(String.format(
                        "%s}",
                        getIndentString(2)
                ));
            } else {
                out.println(String.format(
                        "%sthrow new DecodingException(\"Unable to decode abstract type\");",
                        getIndentString(2)
                ));
            }

            out.println("    }");
        } else {
            out.println(String.format(
                    "%s%s.%s toDecode = new %s.%s();",
                    getIndentString(2),
                    encodableClass.getPackageName(),
                    encodableClass.getClassName(),
                    encodableClass.getPackageName(),
                    encodableClass.getClassName()
            ));

            boolean first = true;
            for (EncodableField encodableField : encodableClass.getFields()) {
                if (first) {
                    first = false;
                } else {
                    out.println();
                }

                decodeField(
                        "toDecode",
                        encodableField.getType(),
                        2,
                        encodableField,
                        encodableClasses,
                        out);
            }

            out.println("        return toDecode;");
            out.println("    }");
        }
    }

    private void decodeField(String variableName,
                             String variableType,
                             int indentation,
                             EncodableField encodableField,
                             Set<String> encodableClasses,
                             PrintWriter out) {
        if ("boolean".equals(variableType)) {
            decodeBooleanPrimitive(variableName, indentation, variableType, encodableField, out);
        } else if ("java.lang.Boolean".equals(variableType)) {
            decodeBoolean(variableName, indentation, variableType, encodableField, out);
        } else if ("byte".equals(variableType)) {
            decodeBytePrimitive(variableName, indentation, variableType, encodableField, out);
        } else if ("java.lang.Byte".equals(variableType)) {
            decodeByte(variableName, indentation, variableType, encodableField, out);
        } else if ("short".equals(variableType)) {
            decodeShortPrimitive(variableName, indentation, variableType, encodableField, out);
        } else if ("java.lang.Short".equals(variableType)) {
            decodeShort(variableName, indentation, variableType, encodableField, out);
        } else if ("int".equals(variableType)) {
            decodeIntPrimitive(variableName, indentation, variableType, encodableField, out);
        } else if ("java.lang.Integer".equals(variableType)) {
            decodeInt(variableName, indentation, variableType, encodableField, out);
        } else if ("long".equals(variableType)) {
            decodeLongPrimitive(variableName, indentation, variableType, encodableField, out);
        } else if ("java.lang.Long".equals(variableType)) {
            decodeLong(variableName, indentation, variableType, encodableField, out);
        } else if ("java.lang.String".equals(variableType)) {
            decodeString(variableName, indentation, variableType, encodableField, out);
        } else if ("byte[]".equals(variableType)) {
            decodeByteArray(variableName, indentation, variableType, encodableField, out);
        } else if (isEnum(encodableField)) {
            decodeEnum(variableName, indentation, variableType, encodableField, out);
        } else if ("java.util.BitSet".equals(variableType)) {
            decodeBitSet(variableName, indentation, variableType, encodableField, out);
        } else if (variableType.matches("^java\\.util\\.Set<.+>$")) {
            decodeSet(variableName, indentation, variableType, encodableField, encodableClasses, out);
        } else if (variableType.matches("^java\\.util\\.List<.+>$")) {
            decodeList(variableName, indentation, variableType, encodableField, encodableClasses, out);
        } else if (variableType.matches("^java\\.util\\.Map<[^,]+\s*,\s*(.+)>$")) {
            decodeMap(variableName, indentation, variableType, encodableField, encodableClasses, out);
        } else if (encodableClasses.contains(variableType)) {
            decodeEncodable(variableName, indentation, variableType, encodableField, out);
        }
    }

    private void decodeBooleanPrimitive(String variableName,
                                        int indentation,
                                        String type,
                                        EncodableField encodableField,
                                        PrintWriter out) {
        String setter = getSetter(
                encodableField,
                type,
                variableName,
                encodableField.getName(),
                "byteBuf.readByte() != 0");
        out.print(getIndentString(indentation));
        out.println(setter);
    }

    private void decodeBoolean(String variableName,
                                        int indentation,
                                        String type,
                                        EncodableField encodableField,
                                        PrintWriter out) {
        String setter = getSetter(
                encodableField,
                type,
                variableName,
                encodableField.getName(),
                "byteBuf.readByte() != 0 ? Boolean.TRUE : Boolean.FALSE");
        out.print(getIndentString(indentation));
        out.println(setter);
    }

    private void decodeBytePrimitive(String variableName,
                                     int indentation,
                                     String type,
                                     EncodableField encodableField,
                                     PrintWriter out) {
        String setter = getSetter(
                encodableField,
                type,
                variableName,
                encodableField.getName(),
                "byteBuf.readByte()");
        out.print(getIndentString(indentation));
        out.println(setter);
    }

    private void decodeByte(String variableName,
                            int indentation,
                            String type,
                            EncodableField encodableField,
                            PrintWriter out) {
        String setter = getSetter(
                encodableField,
                type,
                variableName,
                encodableField.getName(),
                "new Byte(byteBuf.readByte())");
        out.print(getIndentString(indentation));
        out.println(setter);
    }

    private void decodeShortPrimitive(String variableName,
                                      int indentation,
                                      String type,
                                      EncodableField encodableField,
                                      PrintWriter out) {
        if (encodableField.getByteOrderPreference() == ByteOrderPreference.LE) {
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "byteBuf.readShortLE()");
            out.print(getIndentString(indentation));
            out.println(setter);
        } else if (encodableField.getByteOrderPreference() == ByteOrderPreference.BE) {
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "byteBuf.readShort()");
            out.print(getIndentString(indentation));
            out.println(setter);
        } else {
            out.println(String.format(
                    "%sif (le) {",
                    getIndentString(indentation)
            ));
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "byteBuf.readShortLE()");
            out.print(getIndentString(indentation + 1));
            out.println(setter);

            out.println(String.format(
                    "%s} else {",
                    getIndentString(indentation)
            ));
            setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "byteBuf.readShort()");
            out.print(getIndentString(indentation + 1));
            out.println(setter);
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation)
            ));
        }
    }

    private void decodeShort(String variableName,
                             int indentation,
                             String type,
                             EncodableField encodableField,
                             PrintWriter out) {
        if (encodableField.getByteOrderPreference() == ByteOrderPreference.LE) {
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "new Short(byteBuf.readShortLE())");
            out.print(getIndentString(indentation));
            out.println(setter);
        } else if (encodableField.getByteOrderPreference() == ByteOrderPreference.BE) {
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "new Short(byteBuf.readShort())");
            out.print(getIndentString(indentation));
            out.println(setter);
        } else {
            out.println(String.format(
                    "%sif (le) {",
                    getIndentString(indentation)
            ));
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "new Short(byteBuf.readShortLE())");
            out.print(getIndentString(indentation + 1));
            out.println(setter);

            out.println(String.format(
                    "%s} else {",
                    getIndentString(indentation)
            ));
            setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "new Short(byteBuf.readShort())");
            out.print(getIndentString(indentation + 1));
            out.println(setter);
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation)
            ));
        }
    }

    private void decodeIntPrimitive(String variableName,
                                    int indentation,
                                    String type,
                                    EncodableField encodableField,
                                    PrintWriter out) {
        if (encodableField.getByteOrderPreference() == ByteOrderPreference.LE) {
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "byteBuf.readIntLE()");
            out.print(getIndentString(indentation));
            out.println(setter);
        } else if (encodableField.getByteOrderPreference() == ByteOrderPreference.BE) {
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "byteBuf.readInt()");
            out.print(getIndentString(indentation));
            out.println(setter);
        } else {
            out.println(String.format(
                    "%sif (le) {",
                    getIndentString(indentation)
            ));
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "byteBuf.readIntLE()");
            out.print(getIndentString(indentation + 1));
            out.println(setter);

            out.println(String.format(
                    "%s} else {",
                    getIndentString(indentation)
            ));
            setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "byteBuf.readInt()");
            out.print(getIndentString(indentation + 1));
            out.println(setter);
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation)
            ));
        }
    }

    private void decodeInt(String variableName,
                           int indentation,
                           String type,
                           EncodableField encodableField,
                           PrintWriter out) {
        if (encodableField.getByteOrderPreference() == ByteOrderPreference.LE) {
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "new Integer(byteBuf.readIntLE())");
            out.print(getIndentString(indentation));
            out.println(setter);
        } else if (encodableField.getByteOrderPreference() == ByteOrderPreference.BE) {
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "new Integer(byteBuf.readInt())");
            out.print(getIndentString(indentation));
            out.println(setter);
        } else {
            out.println(String.format(
                    "%sif (le) {",
                    getIndentString(indentation)
            ));
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "new Integer(byteBuf.readIntLE())");
            out.print(getIndentString(indentation + 1));
            out.println(setter);

            out.println(String.format(
                    "%s} else {",
                    getIndentString(indentation)
            ));
            setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "new Integer(byteBuf.readInt())");
            out.print(getIndentString(indentation + 1));
            out.println(setter);
            out.println("        }");
        }
    }

    private void decodeLongPrimitive(String variableName,
                                     int indentation,
                                     String type,
                                     EncodableField encodableField,
                                     PrintWriter out) {
        if (encodableField.getByteOrderPreference() == ByteOrderPreference.LE) {
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "byteBuf.readLongLE()");
            out.print(getIndentString(indentation));
            out.println(setter);
        } else if (encodableField.getByteOrderPreference() == ByteOrderPreference.BE) {
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "byteBuf.readLong()");
            out.print(getIndentString(indentation));
            out.println(setter);
        } else {
            out.println(String.format(
                    "%sif (le) {",
                    getIndentString(indentation)
            ));
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "byteBuf.readLongLE()");
            out.print(getIndentString(indentation + 1));
            out.println(setter);

            out.println(String.format(
                    "%s} else {",
                    getIndentString(indentation)
            ));
            setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "byteBuf.readLong()");
            out.print(getIndentString(indentation + 1));
            out.println(setter);
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation)
            ));
        }
    }

    private void decodeLong(String variableName,
                            int indentation,
                            String type,
                            EncodableField encodableField,
                            PrintWriter out) {
        if (encodableField.getByteOrderPreference() == ByteOrderPreference.LE) {
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "new Long(byteBuf.readLongLE())");
            out.print(getIndentString(indentation));
            out.println(setter);
        } else if (encodableField.getByteOrderPreference() == ByteOrderPreference.BE) {
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "new Long(byteBuf.readLongLE())");
            out.print(getIndentString(indentation));
            out.println(setter);
        } else {
            out.println(String.format(
                    "%sif (le) {",
                    getIndentString(indentation)
            ));
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "new Long(byteBuf.readLongLE())");
            out.print(getIndentString(indentation + 1));
            out.println(setter);

            out.println(String.format(
                    "%s} else {",
                    getIndentString(indentation)));
            setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "new Long(byteBuf.readLong())");
            out.print(getIndentString(indentation + 1));
            out.println(setter);
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation)
            ));
        }
    }

    private void decodeString(String variableName,
                              int indentation,
                              String type,
                              EncodableField encodableField,
                              PrintWriter out) {
        out.println(String.format(
                "%s{",
                getIndentString(indentation)
        ));
        out.println(String.format(
                "%sint size = le ? byteBuf.readIntLE() : byteBuf.readInt();",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%sif (size == 0) {",
                getIndentString(indentation + 1)
        ));
        String setter = getSetter(
                encodableField,
                type,
                variableName,
                encodableField.getName(),
                "null"
        );
        out.print(getIndentString(indentation + 2));
        out.println(setter);
        out.println(String.format(
                "%s} else {",
                getIndentString(indentation + 1)
        ));

        out.println(String.format(
                "%sbyte[] bytes = new byte[size];",
                getIndentString(indentation + 2)
        ));
        out.println(String.format(
                "%sbyteBuf.readBytes(bytes);",
                getIndentString(indentation + 2)
        ));
        setter = getSetter(
                encodableField,
                type,
                variableName,
                encodableField.getName(),
                "new String(bytes, StandardCharsets.UTF_8)"
        );
        out.print(getIndentString(indentation + 2));
        out.println(setter);
        out.println(String.format(
                "%s}",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%s}",
                getIndentString(indentation)
        ));
    }

    private void decodeByteArray(String variableName,
                                 int indentation,
                                 String type,
                                 EncodableField encodableField,
                                 PrintWriter out) {
        out.println(String.format(
                "%s{",
                getIndentString(indentation)
        ));
        if (encodableField.isIncludeSize()) {
            out.println(String.format(
                    "%sint size;",
                    getIndentString(indentation + 1)
            ));
            out.println(String.format(
                    "%sif (le) {",
                    getIndentString(indentation + 1)
            ));
            switch (encodableField.getSizeLength()) {
                case 1 -> out.println(String.format(
                        "%ssize = byteBuf.readByte();",
                        getIndentString(indentation + 2)
                ));
                case 2 -> out.println(String.format(
                        "%ssize = byteBuf.readShortLE();",
                        getIndentString(indentation + 2)
                ));
                default -> out.println(String.format(
                        "%ssize = byteBuf.readIntLE();",
                        getIndentString(indentation + 2)
                ));
            }
            out.println(String.format(
                    "%s} else {",
                    getIndentString(indentation + 1)
            ));
            switch (encodableField.getSizeLength()) {
                case 1 -> out.println(String.format(
                        "%ssize = byteBuf.readByte();",
                        getIndentString(indentation + 2)
                ));
                case 2 -> out.println(String.format(
                        "%ssize = byteBuf.readShort();",
                        getIndentString(indentation + 2)
                ));
                default -> out.println(String.format(
                        "%ssize = byteBuf.readInt();",
                        getIndentString(indentation + 2)
                ));
            }
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation + 1)
            ));
        } else if (encodableField.getEncodingSize() != null) {
            out.println(String.format(
                    "%sint size = %d;",
                    getIndentString(indentation + 1),
                    encodableField.getEncodingSize()
            ));
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "Byte array without included size or encoding size");
        }

        out.println(String.format(
                "%sbyte[] bytes = new byte[size];",
                getIndentString(indentation + 1)
        ));
        out.println(String.format(
                "%sbyteBuf.readBytes(bytes);",
                getIndentString(indentation + 1)
        ));

        if (encodableField.getByteOrderPreference() != ByteOrderPreference.NONE) {
            out.println(String.format(
                    "%sboolean needToReverse = %sle;",
                    getIndentString(indentation + 1),
                    (encodableField.getByteOrderPreference() == ByteOrderPreference.LE) ? "!" : ""
            ));
            out.println(String.format(
                    "%sif (needToReverse) {",
                    getIndentString(indentation + 1)
            ));
            out.println(String.format(
                    "%sfor (int i = 0; i < size / 2; i++) {",
                    getIndentString(indentation + 2)
            ));
            out.println(String.format(
                    "%sbyte b = bytes[i];",
                    getIndentString(indentation + 3)
            ));
            out.println(String.format(
                    "%sbytes[i] = bytes[size - i - 1];",
                    getIndentString(indentation + 3)
            ));
            out.println(String.format(
                    "%sbytes[size - i - 1] = b;",
                    getIndentString(indentation + 3)
            ));
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation + 2)
            ));
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation + 1)
            ));
        }

        String setter = getSetter(
                encodableField,
                type,
                variableName,
                encodableField.getName(),
                "bytes"
        );
        out.print(getIndentString(indentation + 1));
        out.println(setter);
        out.println(String.format(
                "%s}",
                getIndentString(indentation)
        ));
    }

    private void decodeEnum(String variableName,
                            int indentation,
                            String type,
                            EncodableField encodableField,
                            PrintWriter out) {
        int encodingSize = 1;
        if (encodableField.getEncodingSize() != null) {
            encodingSize = encodableField.getEncodingSize();
        }
        out.println(String.format(
                "%s{",
                getIndentString(indentation)
        ));
        switch (encodingSize) {
            case 1 -> out.println(String.format(
                    "%sint value = byteBuf.readByte();",
                    getIndentString(indentation + 1)
            ));
            case 2 -> out.println(String.format(
                    "%sint value = (le) ? byteBuf.readShortLE() : byteBuf.readShort();",
                    getIndentString(indentation + 1)
            ));
            default -> out.println(String.format(
                    "%sint value = (le) ? byteBuf.readIntLE() : byteBuf.readInt();",
                    getIndentString(indentation + 1)
            ));
        }
        String value = String.format(
                "%s.getFromValueInt(value)",
                type
        );
        String setter = getSetter(
                encodableField,
                type,
                variableName,
                encodableField.getName(),
                value
        );
        out.print(getIndentString(indentation + 1));
        out.println(setter);
        out.println(String.format(
                "%s}",
                getIndentString(indentation)
        ));
    }

    private void decodeBitSet(String variableName,
                              int indentation,
                              String type,
                              EncodableField encodableField,
                              PrintWriter out) {
        if (encodableField.getEncodingSize() != null) {
            out.println(String.format(
                    "%s{",
                    getIndentString(indentation)
            ));
            out.println(String.format(
                    "%sbyte[] bytes = new byte[%d];",
                    getIndentString(indentation + 1),
                    encodableField.getEncodingSize()
            ));
            out.println(String.format(
                    "%sbyteBuf.readBytes(bytes);",
                    getIndentString(indentation + 1)
            ));
            out.println(String.format(
                    "%sif (!le) {",
                    getIndentString(indentation + 1)
            ));
            out.println(String.format(
                    "%sfor (int i = 0; i < %d / 2; i++) {",
                    getIndentString(indentation + 2),
                    encodableField.getEncodingSize()
            ));
            out.println(String.format(
                    "%sbyte b = bytes[%d - i - 1];",
                    getIndentString(indentation + 3),
                    encodableField.getEncodingSize()
            ));
            out.println(String.format(
                    "%sbytes[%d - i - 1] = bytes[i];",
                    getIndentString(indentation + 3),
                    encodableField.getEncodingSize()
            ));
            out.println(String.format(
                    "%sbytes[i] = b;",
                    getIndentString(indentation + 3)
            ));
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation + 2)
            ));
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation + 1)
            ));

            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "java.util.BitSet.valueOf(bytes)"
            );
            out.print(getIndentString(indentation + 1));
            out.println(setter);

            out.println(String.format(
                    "%s",
                    getIndentString(indentation)
            ));
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation)
            ));
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "BitSet missing encoding size");
        }
    }

    private void decodeSet(String variableName,
                           int indentation,
                           String type,
                           EncodableField encodableField,
                           Set<String> encodableClasses,
                           PrintWriter out) {
        out.println(String.format(
                "%s{",
                getIndentString(indentation)
        ));
        out.println(String.format(
                "%sint setSize = le ? byteBuf.readIntLE() : byteBuf.readInt();",
                getIndentString(indentation + 1)
        ));

        Pattern pattern = Pattern.compile("^java\\.util\\.Set<(.+)>$");
        Matcher matcher = pattern.matcher(type);
        if (matcher.matches()) {
            String innerType = matcher.group(1);
            out.println(String.format(
                    "%sSet<%s> decodedSet = new java.util.HashSet<>();",
                    getIndentString(indentation + 1),
                    innerType
            ));
            out.println(String.format(
                    "%sfor (int i = 0; i < setSize; i++) {",
                    getIndentString(indentation + 1)
            ));
            out.println(String.format(
                    "%s%s element;",
                    getIndentString(indentation + 2),
                    innerType
            ));
            decodeField(
                    "element",
                    innerType,
                    indentation + 2,
                    encodableField,
                    encodableClasses,
                    out);
            out.println(String.format(
                    "%sdecodedSet.add(element);",
                    getIndentString(indentation + 2)
            ));
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation + 1)
            ));
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "decodedSet"
            );
            out.print(getIndentString(indentation + 1));
            out.println(setter);
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "Unable to determine element type for set");
        }

        out.println(String.format(
                "%s}",
                getIndentString(indentation)
        ));
    }

    private void decodeList(String variableName,
                            int indentation,
                            String type,
                            EncodableField encodableField,
                            Set<String> encodableClasses,
                            PrintWriter out) {
        out.println(String.format(
                "%s{",
                getIndentString(indentation)
        ));
        out.println(String.format(
                "%sint listSize = le ? byteBuf.readIntLE() : byteBuf.readInt();",
                getIndentString(indentation + 1)
        ));

        Pattern pattern = Pattern.compile("^java\\.util\\.List<(.+)>$");
        Matcher matcher = pattern.matcher(type);
        if (matcher.matches()) {
            String innerType = matcher.group(1);
            out.println(String.format(
                    "%sList<%s> decodedList = new java.util.ArrayList<>();",
                    getIndentString(indentation + 1),
                    innerType
            ));
            out.println(String.format(
                    "%sfor (int i = 0; i < listSize; i++) {",
                    getIndentString(indentation + 1)
            ));
            out.println(String.format(
                    "%s%s element;",
                    getIndentString(indentation + 2),
                    innerType
            ));
            decodeField(
                    "element",
                    innerType,
                    indentation + 2,
                    encodableField,
                    encodableClasses,
                    out);
            out.println(String.format(
                    "%sdecodedList.add(element);",
                    getIndentString(indentation + 2)
            ));
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation + 1)
            ));
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "decodedList"
            );
            out.print(getIndentString(indentation + 1));
            out.println(setter);
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "Unable to determine element type for list");
        }

        out.println(String.format(
                "%s}",
                getIndentString(indentation)
        ));
    }

    private void decodeMap(String variableName,
                           int indentation,
                           String type,
                           EncodableField encodableField,
                           Set<String> encodableClasses,
                           PrintWriter out) {
        out.println(String.format(
                "%s{",
                getIndentString(indentation)
        ));
        out.println(String.format(
                "%sint mapSize = le ? byteBuf.readIntLE() : byteBuf.readInt();",
                getIndentString(indentation + 1)
        ));

        Pattern pattern = Pattern.compile("^java\\.util\\.Map<([^,]+)\\s*,\\s*(.*)>$");
        Matcher matcher = pattern.matcher(type);
        if (matcher.matches()) {
            String keyType = matcher.group(1);
            String valueType = matcher.group(2);
            out.println(String.format(
                    "%sMap<%s, %s> decodedMap = new java.util.HashMap<>();",
                    getIndentString(indentation + 1),
                    keyType,
                    valueType
            ));
            out.println(String.format(
                    "%sfor (int i = 0; i < mapSize; i++) {",
                    getIndentString(indentation + 1)
            ));
            out.println(String.format(
                    "%s%s key;",
                    getIndentString(indentation + 2),
                    keyType
            ));
            decodeField(
                    "key",
                    keyType,
                    indentation + 2,
                    encodableField,
                    encodableClasses,
                    out);
            out.println(String.format(
                    "%s%s value;",
                    getIndentString(indentation + 2),
                    valueType
            ));
            decodeField(
                    "value",
                    valueType,
                    indentation + 2,
                    encodableField,
                    encodableClasses,
                    out);
            out.println(String.format(
                    "%sdecodedMap.put(key, value);",
                    getIndentString(indentation + 2)
            ));
            out.println(String.format(
                    "%s}",
                    getIndentString(indentation + 1)
            ));
            String setter = getSetter(
                    encodableField,
                    type,
                    variableName,
                    encodableField.getName(),
                    "decodedMap"
            );
            out.print(getIndentString(indentation + 1));
            out.println(setter);
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "Unable to determine key/value types for map");
        }

        out.println(String.format(
                "%s}",
                getIndentString(indentation)
        ));
    }

    private void decodeEncodable(String variableName,
                                 int indentation,
                                 String type,
                                 EncodableField encodableField,
                                 PrintWriter out) {
        String packageName = type.substring(0, type.lastIndexOf('.'));
        String className = type.substring(packageName.length() + 1);
        packageName = getEncodingPackageName(packageName);

        String value = String.format(
                "%s.%sEncodable.decode(byteBuf, le)",
                packageName,
                className
        );
        String setter = getSetter(
                encodableField,
                type,
                variableName,
                encodableField.getName(),
                value
        );
        out.print(getIndentString(indentation));
        out.println(setter);
    }
}
