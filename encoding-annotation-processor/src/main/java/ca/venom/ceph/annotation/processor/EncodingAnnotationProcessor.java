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
import ca.venom.ceph.encoding.annotations.CephFieldDecode;
import ca.venom.ceph.encoding.annotations.CephFieldEncode;
import ca.venom.ceph.encoding.annotations.CephPostDecode;
import ca.venom.ceph.encoding.annotations.CephPreEncode;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephTypeVersionGenerator;
import ca.venom.ceph.encoding.annotations.CephTypeVersionReceiver;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private enum TypeVersionReceiverValidity {
        INVALID,
        ONLY_VERSION,
        VERSION_AND_COMPAT
    }

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

        Map<String, Map<Integer, String>> encodeMethods = getFieldMethods(
                roundEnv.getElementsAnnotatedWith(CephFieldEncode.class),
                true
        );
        Map<String, Map<Integer, String>> decodeMethods = getFieldMethods(
                roundEnv.getElementsAnnotatedWith(CephFieldDecode.class),
                false
        );
        for (Map.Entry<String, ParsedClass> entry : parsedClasses.entrySet()) {
            addFieldMethodsToClass(
                    entry.getValue(),
                    encodeMethods.get(entry.getKey()),
                    decodeMethods.get(entry.getKey())
            );
        }

        addPreEncodeMethods(parsedClasses, roundEnv.getElementsAnnotatedWith(CephPreEncode.class));
        addPostDecodeMethods(parsedClasses, roundEnv.getElementsAnnotatedWith(CephPostDecode.class));

        for (Element element : roundEnv.getElementsAnnotatedWith(CephTypeVersionGenerator.class)) {
            ExecutableElement executableElement = (ExecutableElement) element;
            String methodName = executableElement.getSimpleName().toString();

            Element classElement = executableElement.getEnclosingElement();
            String className = "<UNKNOWN>";
            if (classElement instanceof TypeElement typeElement) {
                className = typeElement.getQualifiedName().toString();
            }

            if (className.equals("<UNKNOWN>") || !parsedClasses.containsKey(className)) {
                continue;
            }

            if (executableElement.getParameters().size() != 1) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Method \"" + className + "." + methodName + "\" must have the signature (java.util.BitSet).");
                continue;
            } else {
                if (executableElement.getParameters().get(0).asType() instanceof DeclaredType declaredType1) {
                    if (declaredType1.getKind() == TypeKind.DECLARED) {
                        TypeElement paramType = (TypeElement) declaredType1.asElement();
                        if (!paramType.getQualifiedName().toString().equals("java.util.BitSet")) {
                            messager.printMessage(Diagnostic.Kind.ERROR, "Method \"" + className + "." + methodName + "\" must have the signature (java.util.BitSet).");
                            continue;
                        }
                    } else {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Method \"" + className + "." + methodName + "\" must have the signature (java.util.BitSet).");
                        continue;
                    }
                } else {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Method \"" + className + "." + methodName + "\" must have the signature (java.util.BitSet).");
                    continue;
                }
            }

            parsedClasses.get(className).setVersionWithCompatGenerator(methodName);
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(CephTypeVersionReceiver.class)) {
            ExecutableElement executableElement = (ExecutableElement) element;
            TypeVersionReceiverValidity validity = validateTypeVersionReceiver(executableElement);
            String methodName = executableElement.getSimpleName().toString();
            Element classElement = executableElement.getEnclosingElement();
            String className = "<UNKNOWN>";

            if (classElement instanceof TypeElement typeElement) {
                className = typeElement.getQualifiedName().toString();
            }

            if (validity == TypeVersionReceiverValidity.INVALID) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Method \"" + className + "." + methodName + "\" must have the signature (byte) or (byte, byte).");
            }

            if (parsedClasses.containsKey(className)) {
                parsedClasses.get(className).setVersionWithCompatReceiver(methodName);
                if (validity == TypeVersionReceiverValidity.VERSION_AND_COMPAT) {
                    parsedClasses.get(className).setReceiveCompat(true);
                }
            }
        }

        try {
            writeJavaCode(parsedClasses);
        } catch (IOException ioe) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Unable to create Java source: " + ioe.getMessage());
        }

        return false;
    }

    private TypeVersionReceiverValidity validateTypeVersionReceiver(ExecutableElement executableElement) {
        if (executableElement.isVarArgs()) {
            return TypeVersionReceiverValidity.INVALID;
        }

        List<? extends VariableElement> parameters = executableElement.getParameters();
        if (parameters.isEmpty() || parameters.size() > 2) {
            return TypeVersionReceiverValidity.INVALID;
        }

        if (parameters.get(0).asType() instanceof PrimitiveType primitiveType1) {
            if (primitiveType1.getKind() != TypeKind.BYTE) {
                return TypeVersionReceiverValidity.INVALID;
            }
        } else {
            return TypeVersionReceiverValidity.INVALID;
        }

        if (parameters.size() == 2) {
            if (parameters.get(1).asType() instanceof PrimitiveType primitiveType2) {
                if (primitiveType2.getKind() != TypeKind.BYTE) {
                    return TypeVersionReceiverValidity.INVALID;
                } else {
                    return TypeVersionReceiverValidity.VERSION_AND_COMPAT;
                }
            } else {
                return TypeVersionReceiverValidity.INVALID;
            }
        } else {
            return TypeVersionReceiverValidity.ONLY_VERSION;
        }
    }

    private Map<String, Map<Integer, String>> getFieldMethods(Set<? extends Element> elements, boolean encode) {
        Map<String, Map<Integer, String>> methods = new HashMap<>();
        for (Element element : elements) {
            ExecutableElement executableElement = (ExecutableElement) element;
            String methodName = executableElement.getSimpleName().toString();

            Element classElement = executableElement.getEnclosingElement();
            if (classElement instanceof TypeElement typeElement) {
                String className = typeElement.getQualifiedName().toString();
                if (!isMethodSignatureValid(executableElement)) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Method \"" + className + "." + methodName + "\" must have the signature (ByteBuf, boolean, BitSet).");
                }

                int order;
                if (encode) {
                    CephFieldEncode encodeAnnotation = executableElement.getAnnotation(CephFieldEncode.class);
                    order = encodeAnnotation.order();
                } else {
                    CephFieldDecode decodeAnnotation = executableElement.getAnnotation(CephFieldDecode.class);
                    order = decodeAnnotation.order();
                }

                if (!methods.containsKey(className)) {
                    methods.put(className, new HashMap<>());
                }
                methods.get(className).put(order, methodName);
            }
        }

        return methods;
    }

    private boolean isMethodSignatureValid(ExecutableElement executableElement) {
        List<? extends VariableElement> parameters = executableElement.getParameters();

        if (executableElement.isVarArgs()) {
            return false;
        }

        if (parameters.size() != 3) {
            return false;
        }

        if (parameters.get(0).asType() instanceof DeclaredType declaredType1) {
            if (declaredType1.getKind() == TypeKind.DECLARED) {
                TypeElement paramType = (TypeElement) declaredType1.asElement();
                if (!paramType.getQualifiedName().toString().equals("io.netty.buffer.ByteBuf")) {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }

        if (parameters.get(1).asType() instanceof PrimitiveType primitiveType2) {
            if (primitiveType2.getKind() != TypeKind.BOOLEAN) {
                return false;
            }
        } else {
            return false;
        }

        if (parameters.get(2).asType() instanceof DeclaredType declaredType3) {
            if (declaredType3.getKind() == TypeKind.DECLARED) {
                TypeElement paramType = (TypeElement) declaredType3.asElement();
                if (!paramType.getQualifiedName().toString().equals("java.util.BitSet")) {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    private void addFieldMethodsToClass(
            ParsedClass parsedClass,
            Map<Integer, String> encodeMethods,
            Map<Integer, String> decodeMethods) {
        if (encodeMethods != null) {
            Optional<Integer> maxOrder = encodeMethods.keySet().stream().max(Comparator.comparingInt(x -> x));
            if (maxOrder.isPresent()) {
                for (int i = 0; i < maxOrder.get(); i++) {
                    parsedClass.getEncodeMethods().add(encodeMethods.get(i + 1));
                }
            }
        }

        if (decodeMethods != null) {
            Optional<Integer> maxOrder = decodeMethods.keySet().stream().max(Comparator.comparingInt(x -> x));
            if (maxOrder.isPresent()) {
                for (int i = 0; i < maxOrder.get(); i++) {
                    parsedClass.getDecodeMethods().add(decodeMethods.get(i + 1));
                }
            }
        }
    }

    private void addPreEncodeMethods(Map<String, ParsedClass> parsedClassMap, Set<? extends Element> elements) {
        for (Element element : elements) {
            ExecutableElement executableElement = (ExecutableElement) element;
            String methodName = executableElement.getSimpleName().toString();

            Element classElement = executableElement.getEnclosingElement();
            if (classElement instanceof TypeElement typeElement) {
                String className = typeElement.getQualifiedName().toString();
                if (parsedClassMap.containsKey(className)) {
                    parsedClassMap.get(className).setPreEncodeMethod(methodName);
                }
            }
        }
    }

    private void addPostDecodeMethods(Map<String, ParsedClass> parsedClassMap, Set<? extends Element> elements) {
        for (Element element : elements) {
            ExecutableElement executableElement = (ExecutableElement) element;
            String methodName = executableElement.getSimpleName().toString();

            Element classElement = executableElement.getEnclosingElement();
            if (classElement instanceof TypeElement typeElement) {
                String className = typeElement.getQualifiedName().toString();
                if (parsedClassMap.containsKey(className)) {
                    parsedClassMap.get(className).setPostDecodeMethod(methodName);
                }
            }
        }
    }

    private void addFieldsToClasses(Map<String, Map<String, ParsedField>> classFields,
                                    Map<String, ParsedClass> parsedClasses,
                                    Map<String, TypeElement> classElements) {
        for (Map.Entry<String, ParsedClass> entry : parsedClasses.entrySet()) {
            TypeElement typeElement = classElements.get(entry.getKey());

            Map<Integer, ParsedField> fieldsForClass = new HashMap<>();
            addFieldsToClass(classFields, typeElement, fieldsForClass);

            Optional<Integer> lastFieldOrder = fieldsForClass.keySet().stream().max(Comparator.comparingInt(x -> x));
            if (lastFieldOrder.isPresent()) {
                for (int i = 0; i < lastFieldOrder.get(); i++) {
                    entry.getValue().getFields().add(fieldsForClass.get(i + 1));
                }
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
