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

import ca.venom.ceph.encoding.annotations.CephChildType;
import ca.venom.ceph.encoding.annotations.CephChildTypes;

import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsedAbstractClass extends ParsedClass {
    public static class ImplementationClass {
        private final int typeCode;
        private final String className;
        private final boolean isDefault;

        public ImplementationClass(int typeCode, String className, boolean isDefault) {
            this.typeCode = typeCode;
            this.className = className;
            this.isDefault = isDefault;
        }

        public int getTypeCode() {
            return typeCode;
        }

        public String getClassName() {
            return className;
        }

        public boolean isDefault() {
            return isDefault;
        }
    }

    private final Set<ImplementationClass> implementations;

    public ParsedAbstractClass(TypeElement element) {
        super(element);

        CephChildTypes childTypes = element.getAnnotation(CephChildTypes.class);
        Set<ImplementationClass> implementationClasses = new HashSet<>();
        for (CephChildType childType : childTypes.value()) {
            Pattern pattern = Pattern.compile(".*, typeClass=(.*)\\.class\\)$");
            Matcher matcher = pattern.matcher(childType.toString());
            if (matcher.matches()) {
                implementationClasses.add(new ImplementationClass(
                        childType.typeValue(),
                        matcher.group(1),
                        childType.isDefault()
                ));
            }
        }

        implementations = Collections.unmodifiableSet(implementationClasses);
    }

    public Set<ImplementationClass> getImplementations() {
        return implementations;
    }
}
