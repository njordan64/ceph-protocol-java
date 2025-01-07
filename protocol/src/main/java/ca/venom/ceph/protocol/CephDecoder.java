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

import ca.venom.ceph.annotation.processor.ClassNameSplitter;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Method;

public class CephDecoder {
    public static <T> T decode(ByteBuf byteBuf, boolean le, Class<T> valueClass) throws DecodingException {
        ClassNameSplitter parsedClassName = new ClassNameSplitter(valueClass.getName());
        String packageName = parsedClassName.getPackageName();
        String className = parsedClassName.getEncoderClassName();

        try {
            Class<?> decodingClass = valueClass.getClassLoader().loadClass(packageName + "." + className);
            Method decodeMethod = decodingClass.getMethod("decode", ByteBuf.class, Boolean.TYPE);
            return (T) decodeMethod.invoke(null, byteBuf, le);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getCause() instanceof DecodingException decodingException) {
                throw decodingException;
            }

            return null;
        }
    }

    public static <T> T decode(ByteBuf byteBuf, boolean le, Class<T> valueClass, int typeCode) throws DecodingException {
        ClassNameSplitter parsedClassName = new ClassNameSplitter(valueClass.getName());
        String packageName = parsedClassName.getPackageName();
        String className = parsedClassName.getEncoderClassName();

        try {
            Class<?> decodingClass = valueClass.getClassLoader().loadClass(packageName + "." + className);
            Method decodeMethod = decodingClass.getMethod("decode", ByteBuf.class, Boolean.TYPE, Integer.TYPE);
            return (T) decodeMethod.invoke(null, byteBuf, le, typeCode);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getCause() instanceof DecodingException decodingException) {
                throw decodingException;
            }

            return null;
        }
    }
}
