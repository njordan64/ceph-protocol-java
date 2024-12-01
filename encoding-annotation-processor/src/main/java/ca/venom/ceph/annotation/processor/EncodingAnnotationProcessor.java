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

import ca.venom.ceph.encoding.annotations.CephChildType;
import ca.venom.ceph.encoding.annotations.CephChildTypes;
import ca.venom.ceph.encoding.annotations.CephCondition;
import ca.venom.ceph.encoding.annotations.CephEncodingSize;
import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephMarker;
import ca.venom.ceph.encoding.annotations.CephMessagePayload;
import ca.venom.ceph.encoding.annotations.CephParentType;
import ca.venom.ceph.encoding.annotations.CephParentTypeValue;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephTypeSize;
import ca.venom.ceph.encoding.annotations.CephTypeVersion;
import ca.venom.ceph.encoding.annotations.CephZeroPadToSizeOf;
import ca.venom.ceph.types.MessageType;

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
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Process Ceph annotations. These are used to describe classes that can be encoded and decoded for communicating
 * with Ceph servers. Classes will be generated that can encode and decode the annotated classes.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({
        "ca.venom.ceph.encoding.annotations.CephType"
})
public class EncodingAnnotationProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;
    private Types typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.typeUtils = processingEnv.getTypeUtils();
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

        Map<MessageType, String> messageTypeClasses = new HashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(CephMessagePayload.class)) {
            CephMessagePayload messagePayload = element.getAnnotation(CephMessagePayload.class);
            messageTypeClasses.put(messagePayload.value(), element.asType().toString());
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
                if (elementType instanceof DeclaredType) {
                    List<? extends TypeMirror> interfaces = typeUtils.directSupertypes(elementType);
                    if (interfaces != null) {
                        encodableField.setInterfaces(
                                interfaces
                                        .stream()
                                        .filter(t -> t instanceof DeclaredType)
                                        .map(TypeMirror::toString)
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
            encodableField.setSizeProperty(field.sizeProperty());

            CephEncodingSize encodingSize = element.getAnnotation(CephEncodingSize.class);
            if (encodingSize != null) {
                encodableField.setEncodingSize(encodingSize.value());
            }

            CephTypeSize typeSize = element.getAnnotation(CephTypeSize.class);
            if (typeSize != null) {
                encodableField.setIncludeSize(true);
            }

            CephCondition cephCondition = element.getAnnotation(CephCondition.class);
            if (cephCondition != null) {
                encodableField.setCondition(new EncodableField.Condition(
                        cephCondition.operator(),
                        cephCondition.property(),
                        cephCondition.values()
                ));
            }

            CephParentTypeValue parentTypeValue = element.getAnnotation(CephParentTypeValue.class);
            if (parentTypeValue != null) {
                encodableField.setParentTypeValue(parentTypeValue.value());
            }

            encodableClass.getFields().add(encodableField);
        }

        CodeGenerator codeGenerator = new CodeGenerator(filer, messager);
        codeGenerator.setEncodableClasses(encodableClasses);
        codeGenerator.setMessageTypeClasses(messageTypeClasses);

        try {
            codeGenerator.generateEncodingSources();
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

        /* In some cases polymorphism is used in an encoded type. In those cases, there is a parent type
           and one or more child types. Look and model these relationships.
         */
        CephParentType parentType = element.getAnnotation(CephParentType.class);
        if (parentType != null) {
            encodableClass.setParentType(parentType);
            encodableClass.setUseTypeCodeParameter(parentType.useParameter());
        }
        CephChildTypes childTypes = element.getAnnotation(CephChildTypes.class);
        if (childTypes != null) {
            List<ChildTypeSimple> childTypesList = new ArrayList<>(childTypes.value().length);
            for (CephChildType childType : childTypes.value()) {
                ChildTypeSimple childTypeSimple = new ChildTypeSimple();
                childTypeSimple.setTypeCode(childType.typeValue());
                childTypeSimple.setDefault(childType.isDefault());
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

        CephZeroPadToSizeOf zeroPadToSizeOf = element.getAnnotation(CephZeroPadToSizeOf.class);
        if (zeroPadToSizeOf != null) {
            Pattern pattern = Pattern.compile(".* value=(.*)\\.class\\)$");
            Matcher matcher = pattern.matcher(zeroPadToSizeOf.toString());
            if (matcher.matches()) {
                encodableClass.setPadToSizeOfClass(matcher.group(1));
            }
        }

        return encodableClass;
    }
}
