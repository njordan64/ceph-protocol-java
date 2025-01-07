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

import ca.venom.ceph.encoding.annotations.CephParentType;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.AbstractElementVisitor14;
import javax.tools.Diagnostic;

public class CephTypeParser extends AbstractElementVisitor14<ParsedClass, ParserContext> {
    @Override
    public ParsedClass visitRecordComponent(RecordComponentElement recordComponentElement, ParserContext context) {
        context.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                "Unexpected type: RecordComponent"
        );
        return null;
    }

    @Override
    public ParsedClass visitModule(ModuleElement moduleElement, ParserContext context) {
        context.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                "Unexpected type: Module"
        );
        return null;
    }

    @Override
    public ParsedClass visitPackage(PackageElement packageElement, ParserContext context) {
        context.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                "Unexpected type: Package"
        );
        return null;
    }

    @Override
    public ParsedClass visitType(TypeElement typeElement, ParserContext context) {
        CephParentType parentType = typeElement.getAnnotation(CephParentType.class);
        if (parentType != null) {
            return new ParsedAbstractClass(typeElement);
        }

        return new ParsedClass(typeElement);
    }

    @Override
    public ParsedClass visitVariable(VariableElement variableElement, ParserContext context) {
        context.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                "Unexpected type: Variable"
        );
        return null;
    }

    @Override
    public ParsedClass visitExecutable(ExecutableElement executableElement, ParserContext context) {
        context.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                "Unexpected type: Executable"
        );
        return null;
    }

    @Override
    public ParsedClass visitTypeParameter(TypeParameterElement typeParameterElement, ParserContext context) {
        context.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                "Unexpected type: TypeParameter"
        );
        return null;
    }
}
