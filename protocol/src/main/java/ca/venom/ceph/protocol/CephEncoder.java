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

public class CephEncoder {
    public static void encode(Object toEncode, ByteBuf byteBuf, boolean le) {
        Class<?> toEncodeClass = toEncode.getClass();
        ClassNameSplitter classNameParser = new ClassNameSplitter(toEncodeClass.getName());

        try {
            Class<?> encodingClass = toEncodeClass.getClassLoader().loadClass(
                    classNameParser.getPackageName() + "." + classNameParser.getEncoderClassName());
            Method encodeMethod = encodingClass.getMethod("encode", toEncodeClass, ByteBuf.class, Boolean.TYPE);
            encodeMethod.invoke(null, toEncode, byteBuf, le);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
