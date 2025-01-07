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

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsedClass {
    private final String className;
    private final Byte marker;
    private final Byte version;
    private final Byte compatVersion;
    private final boolean includeSize;
    private final Integer typeOffset;
    private final Integer typeSize;
    private final Boolean useParameter;
    private final String zeroPadToSizeOfClass;
    private final MessageType messageType;
    private final List<ParsedField> fields = new ArrayList<>();

    public ParsedClass(TypeElement classElement) {
        this.className = classElement.getQualifiedName().toString();

        AtomicReference<Byte> marker = new AtomicReference<>();
        AtomicReference<Byte> version = new AtomicReference<>();
        AtomicReference<Byte> compatVersion = new AtomicReference<>();
        AtomicReference<Boolean> includeSize = new AtomicReference<>();
        AtomicReference<Integer> typeOffset = new AtomicReference<>();
        AtomicReference<Integer> typeSize = new AtomicReference<>();
        AtomicReference<Boolean> useParameter = new AtomicReference<>();
        AtomicReference<MessageType> messageType = new AtomicReference<>();

        loadPropertiesFromClassElement(
                classElement,
                marker,
                version,
                compatVersion,
                includeSize,
                typeOffset,
                typeSize,
                useParameter,
                messageType
        );

        if (marker.get() != null) {
            this.marker = marker.get();
        } else {
            this.marker = null;
        }

        if (version.get() != null) {
            this.version = version.get();
        } else {
            this.version = null;
        }

        if (compatVersion.get() != null) {
            this.compatVersion = compatVersion.get();
        } else {
            this.compatVersion = null;
        }

        if (includeSize.get() != null) {
            this.includeSize = includeSize.get();
        } else {
            this.includeSize = false;
        }

        if (typeOffset.get() != null) {
            this.typeOffset = typeOffset.get();
        } else {
            this.typeOffset = null;
        }

        if (typeSize.get() != null) {
            this.typeSize = typeSize.get();
        } else {
            this.typeSize = null;
        }

        if (useParameter.get() != null) {
            this.useParameter = useParameter.get();
        } else {
            this.useParameter = null;
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

        this.messageType = messageType.get();
    }

    private void loadPropertiesFromClassElement(TypeElement classElement,
                                                AtomicReference<Byte> marker,
                                                AtomicReference<Byte> version,
                                                AtomicReference<Byte> compatVersion,
                                                AtomicReference<Boolean> includeSize,
                                                AtomicReference<Integer> typeOffset,
                                                AtomicReference<Integer> typeSize,
                                                AtomicReference<Boolean> useParameter,
                                                AtomicReference<MessageType> messageType) {
        DeclaredType parentType = (DeclaredType) classElement.getSuperclass();
        TypeElement parentElement = (TypeElement) parentType.asElement();
        if (parentElement.getAnnotation(CephType.class) != null) {
            loadPropertiesFromClassElement(
                    parentElement,
                    marker,
                    version,
                    compatVersion,
                    includeSize,
                    typeOffset,
                    typeSize,
                    useParameter,
                    messageType
            );
        }

        CephMarker markerAnnotation = classElement.getAnnotation(CephMarker.class);
        if (markerAnnotation != null) {
            marker.set(markerAnnotation.value());
        }

        CephTypeVersion typeVersionAnnotation = classElement.getAnnotation(CephTypeVersion.class);
        if (typeVersionAnnotation != null) {
            version.set(typeVersionAnnotation.version());
            if (typeVersionAnnotation.compatVersion() != 0) {
                compatVersion.set(typeVersionAnnotation.compatVersion());
            }
        }

        if (classElement.getAnnotation(CephTypeSize.class) != null) {
            includeSize.set(true);
        }

        CephParentType parentTypeAnnotation = classElement.getAnnotation(CephParentType.class);
        if (parentTypeAnnotation != null) {
            typeOffset.set(parentTypeAnnotation.typeOffset());
            typeSize.set(parentTypeAnnotation.typeSize());
            useParameter.set(parentTypeAnnotation.useParameter());
        }

        CephMessagePayload messagePayload = classElement.getAnnotation(CephMessagePayload.class);
        if (messagePayload != null) {
            messageType.set(messagePayload.value());
        }
    }

    public String getClassName() {
        return className;
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

    public List<ParsedField> getFields() {
        return fields;
    }
}
