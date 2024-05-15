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

/**
 * In some cases a field may be declared that is of a type that has multiple child implementations. This class
 * contains metadata for a child implementation class.
 */
public class ChildTypeSimple {
    private int typeCode;
    private String className;

    /**
     * Get the type code
     * @return type code
     */
    public int getTypeCode() {
        return typeCode;
    }

    /**
     * Update the type code
     * @param typeCode type code
     */
    public void setTypeCode(int typeCode) {
        this.typeCode = typeCode;
    }

    /**
     * Get the name of the class
     * @return name of the class
     */
    public String getClassName() {
        return className;
    }

    /**
     * Update the name of the class
     * @param className name of the class
     */
    public void setClassName(String className) {
        this.className = className;
    }
}
