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
import ca.venom.ceph.annotation.processor.parser.ParsedFieldMethod;
import ca.venom.ceph.annotation.processor.parser.ParserContext;
import ca.venom.ceph.annotation.processor.parser.VersionField;
import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephFieldDecode;
import ca.venom.ceph.encoding.annotations.CephFieldDecodes;
import ca.venom.ceph.encoding.annotations.CephFieldEncode;
import ca.venom.ceph.encoding.annotations.CephFieldEncodes;
import ca.venom.ceph.encoding.annotations.CephFields;
import ca.venom.ceph.encoding.annotations.CephPostDecode;
import ca.venom.ceph.encoding.annotations.CephPreEncode;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephTypeCompatVersionDecider;
import ca.venom.ceph.encoding.annotations.CephTypeVersionGenerator;

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
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
        final ParserContext context = new ParserContext(messager);
        final CephTypeParser typeParser = new CephTypeParser();
        final Map<String, ParsedClass> parsedClasses = new HashMap<>();
        for (Element classElement : roundEnv.getElementsAnnotatedWith(CephType.class)) {
            ParsedClass parsedClass = classElement.accept(typeParser, context);
            if (parsedClass != null && classElement instanceof TypeElement typeElement) {
                parsedClasses.put(typeElement.getQualifiedName().toString(), parsedClass);
            }
        }

        for (ParsedClass parsedClass : parsedClasses.values()) {
            parsedClass.processFields(parsedClasses);
        }

        addEncodeMethods(parsedClasses, roundEnv);
        addDecodeMethods(parsedClasses, roundEnv);

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
                messager.printMessage(Diagnostic.Kind.ERROR, "Method \"" + className + "." + methodName + "\" must have the signature (java.util.BitSet) -> ca.venom.ceph.types.VersionWithCompat.");
                continue;
            } else {
                if (executableElement.getParameters().get(0).asType() instanceof DeclaredType declaredType1) {
                    if (declaredType1.getKind() == TypeKind.DECLARED) {
                        TypeElement paramType = (TypeElement) declaredType1.asElement();
                        if (!paramType.getQualifiedName().toString().equals("java.util.BitSet")) {
                            messager.printMessage(Diagnostic.Kind.ERROR, "Method \"" + className + "." + methodName + "\" must have the signature (java.util.BitSet) -> ca.venom.ceph.types.VersionWithCompat.");
                            continue;
                        }
                    } else {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Method \"" + className + "." + methodName + "\" must have the signature (java.util.BitSet) -> ca.venom.ceph.types.VersionWithCompat.");
                        continue;
                    }
                } else {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Method \"" + className + "." + methodName + "\" must have the signature (java.util.BitSet) -> ca.venom.ceph.types.VersionWithCompat.");
                    continue;
                }
            }

            if (executableElement.getReturnType() instanceof DeclaredType returnType) {
                if (returnType.getKind() == TypeKind.DECLARED) {
                    TypeElement paramType = (TypeElement) returnType.asElement();
                    if (!paramType.getQualifiedName().toString().equals("ca.venom.ceph.types.VersionWithCompat")) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Method \"" + className + "." + methodName + "\" must have the signature (java.util.BitSet) -> ca.venom.ceph.types.VersionWithCompat.");
                        continue;
                    }
                } else {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Method \"" + className + "." + methodName + "\" must have the signature (java.util.BitSet) -> ca.venom.ceph.types.VersionWithCompat.");
                    continue;
                }
            } else {
                messager.printMessage(Diagnostic.Kind.ERROR, "Method \"" + className + "." + methodName + "\" must have the signature (java.util.BitSet) -> ca.venom.ceph.types.VersionWithCompat.");
                continue;
            }

            parsedClasses.get(className).setVersionWithCompatGenerator(methodName);
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(CephTypeCompatVersionDecider.class)) {
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
                messager.printMessage(Diagnostic.Kind.ERROR, "Method \"" + className + "." + methodName + "\" must have the signature (byte) -> boolean.");
                continue;
            } else {
                if (executableElement.getParameters().get(0).asType() instanceof PrimitiveType primitiveType) {
                    if (primitiveType.getKind() != TypeKind.BYTE) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Method \"" + className + "." + methodName + "\" must have the signature (byte) - boolean.");
                        continue;
                    }
                } else {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Method \"" + className + "." + methodName + "\" must have the signature (byte) -> boolean.");
                    continue;
                }
            }

            if (executableElement.getReturnType() instanceof PrimitiveType primitiveType) {
                if (primitiveType.getKind() != TypeKind.BOOLEAN) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Method \"" + className + "." + methodName + "\" must have the signature (byte) -> boolean.");
                    continue;
                }
            } else {
                messager.printMessage(Diagnostic.Kind.ERROR, "Method \"" + className + "." + methodName + "\" must have the signature (byte) -> boolean.");
                continue;
            }

            parsedClasses.get(className).setCompatVersionDecider(methodName);
        }

        try {
            writeJavaCode(parsedClasses);
        } catch (IOException ioe) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Unable to create Java source: " + ioe.getMessage());
        }

        return false;
    }

    private boolean doesMethodRequireVersion(
            String className,
            String methodName,
            ExecutableElement executableElement) throws InvalidMethodSignatureException {
        List<? extends VariableElement> parameters = executableElement.getParameters();

        final String[] validSignatures = {
                "(io.netty.buffer.ByteBuf, boolean, java.util.BitSet)",
                "(io.netty.buffer.ByteBuf, boolean, java.util.BitSet, byte)"
        };

        if (executableElement.isVarArgs()) {
            throw new InvalidMethodSignatureException(className, methodName, validSignatures);
        }

        if (parameters.size() < 3 || parameters.size() > 4) {
            throw new InvalidMethodSignatureException(className, methodName, validSignatures);
        }

        if (parameters.get(0).asType() instanceof DeclaredType declaredType1) {
            if (declaredType1.getKind() == TypeKind.DECLARED) {
                TypeElement paramType = (TypeElement) declaredType1.asElement();
                if (!paramType.getQualifiedName().toString().equals("io.netty.buffer.ByteBuf")) {
                    throw new InvalidMethodSignatureException(className, methodName, validSignatures);
                }
            } else {
                throw new InvalidMethodSignatureException(className, methodName, validSignatures);
            }
        } else {
            throw new InvalidMethodSignatureException(className, methodName, validSignatures);
        }

        if (parameters.get(1).asType() instanceof PrimitiveType primitiveType2) {
            if (primitiveType2.getKind() != TypeKind.BOOLEAN) {
                throw new InvalidMethodSignatureException(className, methodName, validSignatures);
            }
        } else {
            throw new InvalidMethodSignatureException(className, methodName, validSignatures);
        }

        if (parameters.get(2).asType() instanceof DeclaredType declaredType3) {
            if (declaredType3.getKind() == TypeKind.DECLARED) {
                TypeElement paramType = (TypeElement) declaredType3.asElement();
                if (!paramType.getQualifiedName().toString().equals("java.util.BitSet")) {
                    throw new InvalidMethodSignatureException(className, methodName, validSignatures);
                }
            } else {
                throw new InvalidMethodSignatureException(className, methodName, validSignatures);
            }
        } else {
            throw new InvalidMethodSignatureException(className, methodName, validSignatures);
        }

        if (parameters.size() == 4) {
            if (parameters.get(3).asType() instanceof PrimitiveType primitiveType4) {
                if (primitiveType4.getKind() != TypeKind.BYTE) {
                    throw new InvalidMethodSignatureException(className, methodName, validSignatures);
                }
            } else {
                throw new InvalidMethodSignatureException(className, methodName, validSignatures);
            }
        }

        return parameters.size() == 4;
    }

    private void addEncodeMethods(Map<String, ParsedClass> parsedClasses, RoundEnvironment roundEnv) {
        final Set<String> processedMethods = new HashSet<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(CephFieldEncodes.class)) {
            if (!(element instanceof ExecutableElement executableElement)) {
                continue;
            }

            final Element classElement = executableElement.getEnclosingElement();
            if (!(classElement instanceof TypeElement typeElement)) {
                continue;
            }
            final String className = typeElement.getQualifiedName().toString();

            if (!parsedClasses.containsKey(className)) {
                continue;
            }

            final String methodName = executableElement.getSimpleName().toString();

            processedMethods.add(className + "." + methodName);

            final CephFieldEncodes fieldEncodes = element.getAnnotation(CephFieldEncodes.class);
            for (CephFieldEncode fieldEncode : fieldEncodes.value()) {
                try {
                    final boolean requireVersion = doesMethodRequireVersion(className, methodName, executableElement);
                    final ParsedFieldMethod parsedFieldMethod = new ParsedFieldMethod(
                            methodName,
                            requireVersion
                    );
                    parsedClasses.get(className).addFieldMethod(
                            fieldEncode.minVersion(),
                            fieldEncode.maxVersion(),
                            fieldEncode.order(),
                            parsedFieldMethod,
                            true
                    );
                } catch (InvalidMethodSignatureException imse) {
                    messager.printMessage(Diagnostic.Kind.ERROR, imse.getMessage());
                }
            }
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(CephFieldEncode.class)) {
            if (!(element instanceof ExecutableElement executableElement)) {
                continue;
            }

            if (element.getAnnotation(CephFieldEncodes.class) != null) {
                continue;
            }

            final Element classElement = executableElement.getEnclosingElement();
            if (!(classElement instanceof TypeElement typeElement)) {
                continue;
            }
            final String className = typeElement.getQualifiedName().toString();

            if (!parsedClasses.containsKey(className)) {
                continue;
            }

            final String methodName = executableElement.getSimpleName().toString();

            processedMethods.add(className + "." + methodName);

            final CephFieldEncode fieldEncode = element.getAnnotation(CephFieldEncode.class);
            try {
                final boolean requireVersion = doesMethodRequireVersion(className, methodName, executableElement);
                final ParsedFieldMethod parsedFieldMethod = new ParsedFieldMethod(
                        methodName,
                        requireVersion
                );
                parsedClasses.get(className).addFieldMethod(
                        fieldEncode.minVersion(),
                        fieldEncode.maxVersion(),
                        fieldEncode.order(),
                        parsedFieldMethod,
                        true
                );
            } catch (InvalidMethodSignatureException imse) {
                messager.printMessage(Diagnostic.Kind.ERROR, imse.getMessage());
            }
        }
    }

    private void addDecodeMethods(Map<String, ParsedClass> parsedClasses, RoundEnvironment roundEnv) {
        final Set<String> processedMethods = new HashSet<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(CephFieldDecodes.class)) {
            if (!(element instanceof ExecutableElement executableElement)) {
                continue;
            }

            final Element classElement = executableElement.getEnclosingElement();
            if (!(classElement instanceof TypeElement typeElement)) {
                continue;
            }
            final String className = typeElement.getQualifiedName().toString();

            if (!parsedClasses.containsKey(className)) {
                continue;
            }

            final String methodName = executableElement.getSimpleName().toString();

            processedMethods.add(className + "." + methodName);

            final CephFieldDecodes fieldDecodes = element.getAnnotation(CephFieldDecodes.class);
            for (CephFieldDecode fieldDecode : fieldDecodes.value()) {
                try {
                    final boolean requireVersion = doesMethodRequireVersion(className, methodName, executableElement);
                    final ParsedFieldMethod parsedFieldMethod = new ParsedFieldMethod(
                            methodName,
                            requireVersion
                    );
                    parsedClasses.get(className).addFieldMethod(
                            fieldDecode.minVersion(),
                            fieldDecode.maxVersion(),
                            fieldDecode.order(),
                            parsedFieldMethod,
                            false
                    );
                } catch (InvalidMethodSignatureException imse) {
                    messager.printMessage(Diagnostic.Kind.ERROR, imse.getMessage());
                }
            }
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(CephFieldDecode.class)) {
            if (!(element instanceof ExecutableElement executableElement)) {
                continue;
            }

            if (element.getAnnotation(CephFieldDecodes.class) != null) {
                continue;
            }

            final Element classElement = executableElement.getEnclosingElement();
            if (!(classElement instanceof TypeElement typeElement)) {
                continue;
            }
            final String className = typeElement.getQualifiedName().toString();

            if (!parsedClasses.containsKey(className)) {
                continue;
            }

            final String methodName = executableElement.getSimpleName().toString();

            processedMethods.add(className + "." + methodName);

            final CephFieldDecode fieldDecode = element.getAnnotation(CephFieldDecode.class);
            try {
                final boolean requireVersion = doesMethodRequireVersion(className, methodName, executableElement);
                final ParsedFieldMethod parsedFieldMethod = new ParsedFieldMethod(
                        methodName,
                        requireVersion
                );
                parsedClasses.get(className).addFieldMethod(
                        fieldDecode.minVersion(),
                        fieldDecode.maxVersion(),
                        fieldDecode.order(),
                        parsedFieldMethod,
                        false
                );
            } catch (InvalidMethodSignatureException imse) {
                messager.printMessage(Diagnostic.Kind.ERROR, imse.getMessage());
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
