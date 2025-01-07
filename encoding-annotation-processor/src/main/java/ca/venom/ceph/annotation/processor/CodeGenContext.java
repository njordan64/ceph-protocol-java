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

import ca.venom.ceph.annotation.processor.parser.ParsedClass;

import java.util.Map;

public abstract class CodeGenContext {
    private Map<String, ParsedClass> parsedClasses;

    protected CodeGenContext(Map<String, ParsedClass> parsedClasses) {
        this.parsedClasses = parsedClasses;
    }

    public Map<String, ParsedClass> getParsedClasses() {
        return parsedClasses;
    }
}
