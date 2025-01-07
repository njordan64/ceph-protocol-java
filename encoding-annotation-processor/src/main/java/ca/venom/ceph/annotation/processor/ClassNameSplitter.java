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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassNameSplitter {
    private final String packageName;
    private final String actualClassName;
    private final String encoderClassName;

    public ClassNameSplitter(String originalClassName) {
        String[] parts = originalClassName.split("\\.");
        StringBuilder packageNameBuilder = new StringBuilder();
        StringBuilder actualClassNameBuilder = new StringBuilder();
        StringBuilder encoderClassNameBuilder = new StringBuilder();

        Pattern pattern = Pattern.compile("^[0-9A-Z]+.*");
        boolean foundClassName = false;
        for (String part : parts) {
            if (foundClassName) {
                if (!actualClassNameBuilder.isEmpty()) {
                    actualClassNameBuilder.append('.');
                    encoderClassNameBuilder.append('_');
                }
                actualClassNameBuilder.append(part);
                encoderClassNameBuilder.append(part);
            } else {
                Matcher matcher = pattern.matcher(part);
                if (matcher.matches()) {
                    foundClassName = true;
                    actualClassNameBuilder.append(part);
                    encoderClassNameBuilder.append(part);
                } else {
                    if (!packageNameBuilder.isEmpty()) {
                        packageNameBuilder.append('.');
                    }
                    packageNameBuilder.append(part);
                }
            }
        }

        this.packageName = packageNameBuilder.toString();
        this.actualClassName = actualClassNameBuilder.toString();

        int index = encoderClassNameBuilder.indexOf("$");
        while (index > -1) {
            encoderClassNameBuilder.setCharAt(index, '_');
            index = encoderClassNameBuilder.indexOf("$");
        }
        encoderClassNameBuilder.append("Encoder");
        this.encoderClassName = encoderClassNameBuilder.toString();
    }

    public String getPackageName() {
        return packageName;
    }

    public String getActualClassName() {
        return actualClassName;
    }

    public String getEncoderClassName() {
        return encoderClassName;
    }
}
