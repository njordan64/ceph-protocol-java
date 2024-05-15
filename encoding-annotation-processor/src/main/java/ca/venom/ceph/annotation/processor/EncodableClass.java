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

import ca.venom.ceph.encoding.annotations.CephParentType;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata for a class that was annotated with @CephType. The metadata can be used for generating code
 * to encode or decode the class.
 */
public class EncodableClass {
    private String packageName;
    private String className;
    private boolean includeSize;
    private Byte marker;
    private Byte version;
    private Byte compatVersion;
    private CephParentType parentType;
    private List<ChildTypeSimple> childTypes;
    private List<EncodableField> fields = new ArrayList<>();

    /**
     * Get the package that the class is in
     * @return package that the class is in
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Update the package name that the class is in
     * @param packageName package name that the class is in
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Get the name of the encodable/decodable class
     * @return name of the encodable/decodable class
     */
    public String getClassName() {
        return className;
    }

    /**
     * Update the name of the encodable/decodable class
     * @param className name of the encodable/decodable class
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Get whether to prepend the encoded size
     * @return whether to prepend the encoded size
     */
    public Boolean isIncludeSize() {
        return includeSize;
    }

    /**
     * Update whether to prepend the encoded size
     * @param includeSize whether to prepend the encoded size
     */
    public void setIncludeSize(Boolean includeSize) {
        this.includeSize = includeSize;
    }

    /**
     * Get the marker for the class
     * @return marker for the class
     */
    public Byte getMarker() {
        return marker;
    }

    /**
     * Update the marker for the class
     * @param marker marker for the class
     */
    public void setMarker(Byte marker) {
        this.marker = marker;
    }

    /**
     * Get the version for the class
     * @return version for the class
     */
    public Byte getVersion() {
        return version;
    }

    /**
     * Update the version for the class
     * @param version version for the class
     */
    public void setVersion(Byte version) {
        this.version = version;
    }

    /**
     * Get the compat version for the class
     * @return compat version for the class
     */
    public Byte getCompatVersion() {
        return compatVersion;
    }

    /**
     * Update the compat version for the class
     * @param compatVersion compat version for the class
     */
    public void setCompatVersion(Byte compatVersion) {
        this.compatVersion = compatVersion;
    }

    /**
     * Get the parent type of the class
     * @return parent type of the class
     */
    public CephParentType getParentType() {
        return parentType;
    }

    /**
     * Update the parent type of the class if the class was annotated with @CephChildType
     * @param parentType parent type of the class
     */
    public void setParentType(CephParentType parentType) {
        this.parentType = parentType;
    }

    /**
     * Get the child types of the class if the class was annotated with @CephParentType
     * @return child types of the class if the class
     */
    public List<ChildTypeSimple> getChildTypes() {
        return childTypes;
    }

    /**
     * Update the child types of the class
     * @param childTypes child types of the class
     */
    public void setChildTypes(List<ChildTypeSimple> childTypes) {
        this.childTypes = childTypes;
    }

    /**
     * Get the list of fields that were annotated with @CephField
     * @return list of fields
     */
    public List<EncodableField> getFields() {
        return fields;
    }

    /**
     * Update the list of fields that were annotated with @CephField
     * @param fields
     */
    public void setFields(List<EncodableField> fields) {
        this.fields = fields;
    }
}
