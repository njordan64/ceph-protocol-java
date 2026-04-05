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

public class ParsedFieldMethod {
    private final String methodName;

    private final boolean includeVersion;

    public ParsedFieldMethod(String methodName, boolean includeVersion) {
        this.methodName = methodName;
        this.includeVersion = includeVersion;
    }

    public String getMethodName() {
        return methodName;
    }

    public boolean isIncludeVersion() {
        return includeVersion;
    }
}
