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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.AbstractElementVisitor14;
import java.util.Set;

public class CephFieldParser extends AbstractElementVisitor14<ParsedField, ParserContext> {
    private Set<String> parsedClassNames;

    public CephFieldParser(Set<String> parsedClassNames) {
        this.parsedClassNames = parsedClassNames;
    }

    @Override
    public ParsedField visitRecordComponent(RecordComponentElement recordComponentElement, ParserContext context) {
        return null;
    }

    @Override
    public ParsedField visitModule(ModuleElement moduleElement, ParserContext context) {
        return null;
    }

    @Override
    public ParsedField visitPackage(PackageElement packageElement, ParserContext context) {
        return null;
    }

    @Override
    public ParsedField visitType(TypeElement typeElement, ParserContext context) {
        return null;
    }

    @Override
    public ParsedField visitVariable(VariableElement variableElement, ParserContext context) {
        return new ParsedField(variableElement, parsedClassNames);
    }

    @Override
    public ParsedField visitExecutable(ExecutableElement executableElement, ParserContext context) {
        return null;
    }

    @Override
    public ParsedField visitTypeParameter(TypeParameterElement typeParameterElement, ParserContext context) {
        return null;
    }
}
