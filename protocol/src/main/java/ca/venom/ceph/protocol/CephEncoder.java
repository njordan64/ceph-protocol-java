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
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

public class CephEncoder {
    public static void encode(short toEncode, ByteBuf byteBuf, boolean le) {
        if (le) {
            byteBuf.writeShortLE(toEncode);
        } else {
            byteBuf.writeShort(toEncode);
        }
    }

    public static void encode(int toEncode, ByteBuf byteBuf, boolean le) {
        if (le) {
            byteBuf.writeIntLE(toEncode);
        } else {
            byteBuf.writeInt(toEncode);
        }
    }

    public static void encode(long toEncode, ByteBuf byteBuf, boolean le) {
        if (le) {
            byteBuf.writeLongLE(toEncode);
        } else {
            byteBuf.writeLong(toEncode);
        }
    }

    public static void encode(String toEncode, ByteBuf byteBuf, boolean le) {
        if (toEncode == null) {
            byteBuf.writeZero(4);
            return;
        }

        byte[] bytes = toEncode.getBytes(StandardCharsets.UTF_8);
        if (le) {
            byteBuf.writeIntLE(bytes.length);
        } else {
            byteBuf.writeInt(bytes.length);
        }
        byteBuf.writeBytes(bytes);
    }

    public static void encode(byte[] toEncode, ByteBuf byteBuf, boolean le) {
        if (toEncode == null) {
            byteBuf.writeZero(4);
            return;
        }

        if (le) {
            byteBuf.writeIntLE(toEncode.length);
        } else {
            byteBuf.writeInt(toEncode.length);
        }
        byteBuf.writeBytes(toEncode);
    }

    public static void encode(Object toEncode, ByteBuf byteBuf, boolean le, BitSet features) {
        Class<?> toEncodeClass = toEncode.getClass();
        ClassNameSplitter classNameParser = new ClassNameSplitter(toEncodeClass.getName());

        try {
            Class<?> encodingClass = toEncodeClass.getClassLoader().loadClass(
                    classNameParser.getPackageName() + "." + classNameParser.getEncoderClassName());
            Method encodeMethod = encodingClass.getMethod("encode", toEncodeClass, ByteBuf.class, Boolean.TYPE, BitSet.class);
            encodeMethod.invoke(null, toEncode, byteBuf, le, features);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
