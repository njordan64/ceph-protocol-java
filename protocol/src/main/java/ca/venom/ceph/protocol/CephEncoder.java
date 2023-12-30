/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.Method;

public class CephEncoder {
    private static String getEncodingPackageName(String className) {
        if (className.contains("$")) {
            String packageName = className.substring(0, className.lastIndexOf('$'));
            String base = packageName.substring(0, packageName.lastIndexOf('.'));
            String outClassName = packageName.substring(packageName.lastIndexOf('.') + 1);
            return base + "._generated." + outClassName;
        } else {
            String packageName = className.substring(0, className.lastIndexOf('.'));
            return packageName + "._generated";
        }
    }

    public static void encode(Object toEncode, ByteBuf byteBuf, boolean le) {
        Class<?> toEncodeClass = toEncode.getClass();
        String packageName = getEncodingPackageName(toEncodeClass.getName());
        String className = toEncodeClass.getSimpleName() + "Encodable";

        try {
            Class<?> encodingClass = toEncodeClass.getClassLoader().loadClass(packageName + "." + className);
            Method encodeMethod = encodingClass.getMethod("encode", toEncodeClass, ByteBuf.class, Boolean.TYPE);
            encodeMethod.invoke(null, toEncode, byteBuf, le);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
