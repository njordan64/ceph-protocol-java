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

import ca.venom.ceph.annotation.processor.parser.CephFieldParser;
import ca.venom.ceph.annotation.processor.parser.CephTypeParser;
import ca.venom.ceph.annotation.processor.parser.ParsedClass;
import ca.venom.ceph.annotation.processor.parser.ParsedField;
import ca.venom.ceph.annotation.processor.parser.ParserContext;
import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephType;

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
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        ParserContext context = new ParserContext(messager);
        CephTypeParser typeParser = new CephTypeParser();
        Map<String, ParsedClass> parsedClasses = new HashMap<>();
        Map<String, TypeElement> classElements = new HashMap<>();
        for (Element classElement : roundEnv.getElementsAnnotatedWith(CephType.class)) {
            ParsedClass parsedClass = classElement.accept(typeParser, context);
            if (parsedClass != null && classElement instanceof TypeElement typeElement) {
                parsedClasses.put(typeElement.getQualifiedName().toString(), parsedClass);
                classElements.put(typeElement.getQualifiedName().toString(), typeElement);
            }
        }

        CephFieldParser fieldParser = new CephFieldParser(parsedClasses.keySet());
        Map<String, Map<String, ParsedField>> classFields = new HashMap<>();
        for (Element fieldElement : roundEnv.getElementsAnnotatedWith(CephField.class)) {
            ParsedField field = fieldElement.accept(fieldParser, context);
            if (field != null && fieldElement instanceof VariableElement variableElement) {
                Element classElement = fieldElement.getEnclosingElement();
                if (classElement instanceof TypeElement typeElement) {
                    String className = typeElement.getQualifiedName().toString();
                    if (!classFields.containsKey(className)) {
                        classFields.put(className, new HashMap<>());
                    }

                    classFields.get(className).put(variableElement.getSimpleName().toString(), field);
                }
            }
        }

        addFieldsToClasses(classFields, parsedClasses, classElements);

        try {
            writeJavaCode(parsedClasses);
        } catch (IOException ioe) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Unable to create Java source: " + ioe.getMessage());
        }

        return false;
    }

    private void addFieldsToClasses(Map<String, Map<String, ParsedField>> classFields,
                                    Map<String, ParsedClass> parsedClasses,
                                    Map<String, TypeElement> classElements) {
        for (Map.Entry<String, ParsedClass> entry : parsedClasses.entrySet()) {
            TypeElement typeElement = classElements.get(entry.getKey());

            Map<Integer, ParsedField> fieldsForClass = new HashMap<>();
            addFieldsToClass(classFields, typeElement, fieldsForClass);

            List<Integer> sortedOrders = new ArrayList<>(fieldsForClass.keySet());
            Collections.sort(sortedOrders);

            for (Integer order : sortedOrders) {
                entry.getValue().getFields().add(fieldsForClass.get(order));
            }
        }
    }

    private void addFieldsToClass(Map<String, Map<String, ParsedField>> classFields,
                                  TypeElement typeElement,
                                  Map<Integer, ParsedField> fieldsForClass) {
        TypeMirror superType = typeElement.getSuperclass();
        if (superType instanceof DeclaredType superDeclaredType) {
            if (superDeclaredType.asElement() instanceof TypeElement superTypeElement) {
                addFieldsToClass(classFields, superTypeElement, fieldsForClass);
            }
        }

        String className = typeElement.getQualifiedName().toString();
        if (classFields.containsKey(className)) {
            for (ParsedField parsedField : classFields.get(className).values()) {
                fieldsForClass.put(parsedField.getOrder(), parsedField);
            }
        }
    }

    private void writeJavaCode(Map<String, ParsedClass> parsedClasses) throws IOException {
        for (ParsedClass parsedClass : parsedClasses.values()) {
            EncoderJavaCodeGenerator codeGenerator = new EncoderJavaCodeGenerator(
                    parsedClass,
                    filer,
                    parsedClasses,
                    messager
            );
            codeGenerator.generateJavaCode(parsedClasses.values());
        }
    }
}
