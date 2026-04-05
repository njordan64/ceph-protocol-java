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

import ca.venom.ceph.encoding.annotations.CephField;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.AbstractElementVisitor14;
import java.util.Set;

public class CephFieldParser extends AbstractElementVisitor14<VersionField, CephField> {
    private Set<String> parsedClassNames;

    public CephFieldParser(Set<String> parsedClassNames) {
        this.parsedClassNames = parsedClassNames;
    }

    @Override
    public VersionField visitRecordComponent(RecordComponentElement recordComponentElement, CephField fieldAnnotation) {
        return null;
    }

    @Override
    public VersionField visitModule(ModuleElement moduleElement, CephField fieldAnnotation) {
        return null;
    }

    @Override
    public VersionField visitPackage(PackageElement packageElement, CephField fieldAnnotation) {
        return null;
    }

    @Override
    public VersionField visitType(TypeElement typeElement, CephField fieldAnnotation) {
        return null;
    }

    @Override
    public VersionField visitVariable(VariableElement variableElement, CephField fieldAnnotation) {
        return new VersionField(variableElement, fieldAnnotation, parsedClassNames);
    }

    @Override
    public VersionField visitExecutable(ExecutableElement executableElement, CephField fieldAnnotation) {
        return null;
    }

    @Override
    public VersionField visitTypeParameter(TypeParameterElement typeParameterElement, CephField fieldAnnotation) {
        return null;
    }
}
