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

import ca.venom.ceph.annotation.processor.fields.FieldCodeGenerator;
import ca.venom.ceph.types.MessageType;

import javax.annotation.processing.Messager;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Contains some context information for generating source code files.
 */
public class CodeGenerationContext {
    private Messager messager;
    private Map<String, FieldCodeGenerator> fieldCodeGenerators;
    private Map<String, EncodableClass> encodableClasses;
    private Map<MessageType, String> messageTypeClasses;
    private Set<String> imports = new HashSet<>();
    private int mapNesting = 1;

    /**
     * Creates a new CodeGenerationContext object.
     *
     * @param messager used for reporting errors/warnings
     * @param fieldCodeGenerators map of code generators for field types
     * @param encodableClasses map of classes annotated with @CephType
     */
    public CodeGenerationContext(Messager messager,
                                 Map<String, FieldCodeGenerator> fieldCodeGenerators,
                                 Map<String, EncodableClass> encodableClasses) {
        this.messager = messager;
        this.fieldCodeGenerators = fieldCodeGenerators;
        this.encodableClasses = encodableClasses;
    }

    /**
     * Get the messager
     * @return messager
     */
    public Messager getMessager() {
        return messager;
    }

    /**
     * Get the map of code generators for field types
     * @return map of code generators for field types
     */
    public Map<String, FieldCodeGenerator> getFieldCodeGenerators() {
        return fieldCodeGenerators;
    }

    /**
     * Get the map of classes annotated with @CephType
     * @return map of classes annotated with @CephType
     */
    public Map<String, EncodableClass> getEncodableClasses() {
        return encodableClasses;
    }

    /**
     * Update the map of classes annotated with @CephType
     * @param encodableClasses map of classes annotated with @CephType
     */
    public void setEncodableClasses(Map<String, EncodableClass> encodableClasses) {
        this.encodableClasses = encodableClasses;
    }

    public Map<MessageType, String> getMessageTypeClasses() {
        return messageTypeClasses;
    }

    public void setMessageTypeClasses(Map<MessageType, String> messageTypeClasses) {
        this.messageTypeClasses = messageTypeClasses;
    }

    /**
     * Get the set of classes that should be added as import statements in the source code file
     * @return set of classes that should be added as import statements in the source code file
     */
    public Set<String> getImports() {
        return imports;
    }

    public int incrementMapNesting() {
        int valueToReturn = mapNesting;
        mapNesting++;
        return valueToReturn;
    }

    public void decrementMapNesting() {
        mapNesting--;
    }
}
