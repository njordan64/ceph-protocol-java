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

public class InvalidMethodSignatureException extends Exception {
    public InvalidMethodSignatureException(String className, String methodName, String[] validSignatures) {
        super(
                "The method \"" + className + "." + methodName + "\" has an invalid signature. Valid signatures are:\n" +
                        String.join("\n", validSignatures)
        );
    }
}
