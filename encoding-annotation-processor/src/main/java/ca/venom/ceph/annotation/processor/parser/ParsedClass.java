/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.annotation.processor.parser;

import ca.venom.ceph.encoding.annotations.*;
import ca.venom.ceph.types.MessageType;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsedClass {
    private final String className;
    private boolean isMessagePayload;
    private final Byte marker;
    private final Byte version;
    private final Byte compatVersion;
    private String versionWithCompatGenerator;
    private String compatVersionDecider;
    private final boolean includeSize;
    private final Integer typeOffset;
    private final Integer typeSize;
    private final Boolean useParameter;
    private final String zeroPadToSizeOfClass;
    private final MessageType messageType;
    private final Map<VersionGroup, List<VersionField>> fields = new HashMap<>();
    private final Map<VersionGroup, List<ParsedFieldMethod>> encodeMethods = new HashMap<>();
    private final Map<VersionGroup, List<ParsedFieldMethod>> decodeMethods = new HashMap<>();
    private String preEncodeMethod;
    private String postDecodeMethod;
    private final TypeElement classElement;

    public ParsedClass(TypeElement classElement) {
        this.classElement = classElement;
        this.className = classElement.getQualifiedName().toString();

        CephMarker markerAnnotation = classElement.getAnnotation(CephMarker.class);
        if (markerAnnotation != null) {
            marker = markerAnnotation.value();
        } else {
            marker = null;
        }

        CephTypeVersionConstant typeVersionAnnotation = classElement.getAnnotation(CephTypeVersionConstant.class);
        if (typeVersionAnnotation != null) {
            version = typeVersionAnnotation.version();
            if (typeVersionAnnotation.compatVersion() != 0) {
                compatVersion = typeVersionAnnotation.compatVersion();
            } else {
                compatVersion = null;
            }
        } else {
            version = null;
            compatVersion = null;
        }

        CephTypeSize typeSizeAnnotation = classElement.getAnnotation(CephTypeSize.class);
        if (typeSizeAnnotation != null) {
            includeSize = true;
        } else {
            includeSize = false;
        }

        CephParentType parentTypeAnnotation = classElement.getAnnotation(CephParentType.class);
        if (parentTypeAnnotation != null) {
            typeOffset = parentTypeAnnotation.typeOffset();
            typeSize = parentTypeAnnotation.typeSize();
            useParameter = parentTypeAnnotation.useParameter();
        } else {
            typeOffset = null;
            typeSize = null;
            useParameter = null;
        }

        CephMessagePayload messagePayload = classElement.getAnnotation(CephMessagePayload.class);
        if (messagePayload != null) {
            messageType = messagePayload.value();
        } else {
            messageType = null;
        }

        CephZeroPadToSizeOf zeroPadToSizeOf = classElement.getAnnotation(CephZeroPadToSizeOf.class);
        if (zeroPadToSizeOf != null) {
            Pattern pattern = Pattern.compile(".*, value=(.*)\\.class\\)$");
            Matcher matcher = pattern.matcher(zeroPadToSizeOf.toString());
            if (matcher.matches()) {
                zeroPadToSizeOfClass = matcher.group(1);
            } else {
                zeroPadToSizeOfClass = null;
            }
        } else {
            zeroPadToSizeOfClass = null;
        }
    }

    public void processFields(Map<String, ParsedClass> parsedClassMap) {
        processFields(classElement, parsedClassMap);
    }

    private void processFields(TypeElement typeElement, Map<String, ParsedClass> parsedClassMap) {
        final String currentClassName = typeElement.getQualifiedName().toString();
        if ("ca.venom.ceph.protocol.messages.MessagePayload".equals(currentClassName)) {
            isMessagePayload = true;
        }

        for (Element childElement : typeElement.getEnclosedElements()) {
            if (!(childElement instanceof VariableElement fieldElement)) {
                continue;
            }

            if (fieldElement.getAnnotation(CephFields.class) != null ||
                    fieldElement.getAnnotation(CephField.class) != null) {
                processField(fieldElement, parsedClassMap);
            }
        }

        final TypeMirror parentTypeMirror = typeElement.getSuperclass();
        if (parentTypeMirror instanceof DeclaredType declaredType) {
            final TypeElement parentElement = (TypeElement) declaredType.asElement();
            processFields(parentElement, parsedClassMap);
        }
    }

    private void processField(VariableElement fieldElement, Map<String, ParsedClass> parsedClassMap) {
        final CephFieldParser fieldParser = new CephFieldParser(parsedClassMap.keySet());

        final CephFields fieldsAnnotation = fieldElement.getAnnotation(CephFields.class);
        if (fieldsAnnotation != null) {
            for (CephField fieldAnnotation : fieldsAnnotation.value()) {
                addField(fieldElement.accept(fieldParser, fieldAnnotation));
            }
        } else {
            final CephField fieldAnnotation = fieldElement.getAnnotation(CephField.class);
            if (fieldAnnotation != null) {
                addField(fieldElement.accept(fieldParser, fieldAnnotation));
            }
        }
    }

    public String getClassName() {
        return className;
    }

    public boolean isMessagePayload() {
        return isMessagePayload;
    }

    public Byte getMarker() {
        return marker;
    }

    public Byte getVersion() {
        return version;
    }

    public Byte getCompatVersion() {
        return compatVersion;
    }

    public String getVersionWithCompatGenerator() {
        return versionWithCompatGenerator;
    }

    public void setVersionWithCompatGenerator(String versionWithCompatGenerator) {
        this.versionWithCompatGenerator = versionWithCompatGenerator;
    }

    public String getCompatVersionDecider() {
        return compatVersionDecider;
    }

    public void setCompatVersionDecider(String compatVersionDecider) {
        this.compatVersionDecider = compatVersionDecider;
    }

    public boolean isIncludeSize() {
        return includeSize;
    }

    public Integer getTypeOffset() {
        return typeOffset;
    }

    public Integer getTypeSize() {
        return typeSize;
    }

    public Boolean getUseParameter() {
        return useParameter;
    }

    public String getZeroPadToSizeOfClass() {
        return zeroPadToSizeOfClass;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public Map<VersionGroup, List<VersionField>> getFields() {
        return fields;
    }

    public void addField(VersionField versionField) {
        final VersionGroup versionGroup = new VersionGroup(
                versionField.getMinVersion(),
                versionField.getMaxVersion()
        );

        if (!fields.containsKey(versionGroup)) {
            fields.put(versionGroup, new ArrayList<>());
        }
        final List<VersionField> fieldGroup = fields.get(versionGroup);
        final int index = versionField.getOrder() - 1;
        while (index >= fieldGroup.size()) {
            fieldGroup.add(null);
        }
        fieldGroup.set(index, versionField);

        if (!encodeMethods.containsKey(versionGroup)) {
            encodeMethods.put(versionGroup, new ArrayList<>());
        }
        final List<ParsedFieldMethod> encodeMethodsList = encodeMethods.get(versionGroup);
        while (index >= encodeMethodsList.size()) {
            encodeMethodsList.add(null);
        }

        if (!decodeMethods.containsKey(versionGroup)) {
            decodeMethods.put(versionGroup, new ArrayList<>());
        }
        final List<ParsedFieldMethod> decodeMethodsList = decodeMethods.get(versionGroup);
        while (index >= decodeMethodsList.size()) {
            decodeMethodsList.add(null);
        }
    }

    public Map<VersionGroup, List<ParsedFieldMethod>> getEncodeMethods() {
        return encodeMethods;
    }

    public Map<VersionGroup, List<ParsedFieldMethod>> getDecodeMethods() {
        return decodeMethods;
    }

    public void addFieldMethod(
            byte minVersion,
            byte maxVersion,
            int order,
            ParsedFieldMethod fieldMethod,
            boolean isEncode) {
        final VersionGroup versionGroup = new VersionGroup(minVersion, maxVersion);
        if (!fields.containsKey(versionGroup)) {
            fields.put(versionGroup, new ArrayList<>());
        }
        final List<VersionField> fieldGroup = fields.get(versionGroup);
        int index = order - 1;
        while (index >= fieldGroup.size()) {
            fieldGroup.add(null);
        }

        final Map<VersionGroup, List<ParsedFieldMethod>> targetMap;
        final Map<VersionGroup, List<ParsedFieldMethod>> otherMap;
        if (isEncode) {
            targetMap = encodeMethods;
            otherMap = decodeMethods;
        } else {
            targetMap = decodeMethods;
            otherMap = encodeMethods;
        }

        if (!targetMap.containsKey(versionGroup)) {
            targetMap.put(versionGroup, new ArrayList<>());
        }
        final List<ParsedFieldMethod> encodeMethodsList = targetMap.get(versionGroup);
        while (index >= encodeMethodsList.size()) {
            encodeMethodsList.add(null);
        }
        encodeMethodsList.set(index, fieldMethod);

        if (!otherMap.containsKey(versionGroup)) {
            otherMap.put(versionGroup, new ArrayList<>());
        }
        final List<ParsedFieldMethod> decodeMethodsList = otherMap.get(versionGroup);
        while (index >= decodeMethodsList.size()) {
            decodeMethodsList.add(null);
        }
    }

    public String getPreEncodeMethod() {
        return preEncodeMethod;
    }

    public void setPreEncodeMethod(String preEncodeMethod) {
        this.preEncodeMethod = preEncodeMethod;
    }

    public String getPostDecodeMethod() {
        return postDecodeMethod;
    }

    public void setPostDecodeMethod(String postDecodeMethod) {
        this.postDecodeMethod = postDecodeMethod;
    }
}
