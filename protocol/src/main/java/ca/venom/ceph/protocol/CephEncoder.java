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
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CephEncoder {
    private enum ParameterType {
        BYTE,
        SHORT,
        INT,
        LONG,
        STRING,
        CEPH_TYPE;

        static ParameterType fromClass(Class<?> clazz) {
            if (clazz.equals(Byte.class)) {
                return BYTE;
            } else if (clazz.equals(Short.class)) {
                return SHORT;
            } else if (clazz.equals(Integer.class)) {
                return INT;
            } else if (clazz.equals(Long.class)) {
                return LONG;
            } else if (clazz.equals(String.class)) {
                return STRING;
            } else {
                return CEPH_TYPE;
            }
        }
    }

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

    public static void encodeString(String toEncode, ByteBuf byteBuf, boolean le) {
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

    public static <T> void encodeList(List<T> toEncode, ByteBuf byteBuf, boolean le, BitSet features, Class<T> itemClass) {
        if (toEncode.isEmpty()) {
            encode(0, byteBuf, le);
            return;
        } else {
            encode(toEncode.size(), byteBuf, le);
        }

        if (itemClass.equals(Byte.class)) {
            final List<Byte> byteList = (List<Byte>) toEncode;
            for (Byte value : byteList) {
                byteBuf.writeByte(value == null ? 0 : value);
            }
        } else if (itemClass.equals(Short.class)) {
            final List<Short> shortList = (List<Short>) toEncode;
            for (Short value : shortList) {
                encode(value == null ? (short) 0 : value, byteBuf, le);
            }
        } else if (itemClass.equals(Integer.class)) {
            final List<Integer> intList = (List<Integer>) toEncode;
            for (Integer value : intList) {
                encode(value == null ? 0 : value, byteBuf, le);
            }
        } else if (itemClass.equals(Long.class)) {
            final List<Long> longList = (List<Long>) toEncode;
            for (Long value : longList) {
                encode(value == null ? 0L : value, byteBuf, le);
            }
        } else if (itemClass.equals(String.class)) {
            final List<String> stringList = (List<String>) toEncode;
            for (String value : stringList) {
                encodeString(value, byteBuf, le);
            }
        } else {
            final ClassNameSplitter classNameParser = new ClassNameSplitter(itemClass.getName());

            try {
                Class<?> encodingClass = itemClass.getClassLoader().loadClass(
                        classNameParser.getPackageName() + "." + classNameParser.getEncoderClassName());
                Method encodeMethod = encodingClass.getMethod("encode", itemClass, ByteBuf.class, Boolean.TYPE, BitSet.class);

                for (Object obj : toEncode) {
                    encodeMethod.invoke(null, obj, byteBuf, le, features);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static <T> void encodeSet(Set<T> toEncode, ByteBuf byteBuf, boolean le, BitSet features, Class<T> itemClass) {
        if (toEncode.isEmpty()) {
            encode(0, byteBuf, le);
            return;
        } else {
            encode(toEncode.size(), byteBuf, le);
        }

        if (itemClass.equals(Byte.class)) {
            final List<Byte> byteList = (List<Byte>) toEncode;
            for (Byte value : byteList) {
                byteBuf.writeByte(value == null ? 0 : value);
            }
        } else if (itemClass.equals(Short.class)) {
            final List<Short> shortList = (List<Short>) toEncode;
            for (Short value : shortList) {
                encode(value == null ? (short) 0 : value, byteBuf, le);
            }
        } else if (itemClass.equals(Integer.class)) {
            final List<Integer> intList = (List<Integer>) toEncode;
            for (Integer value : intList) {
                encode(value == null ? 0 : value, byteBuf, le);
            }
        } else if (itemClass.equals(Long.class)) {
            final List<Long> longList = (List<Long>) toEncode;
            for (Long value : longList) {
                encode(value == null ? 0L : value, byteBuf, le);
            }
        } else if (itemClass.equals(String.class)) {
            final List<String> stringList = (List<String>) toEncode;
            for (String value : stringList) {
                encodeString(value, byteBuf, le);
            }
        } else if (itemClass.equals(String.class)) {
            final List<String> stringList = (List<String>) toEncode;
            if (le) {
                for (String value : stringList) {
                    if (value == null) {
                        byteBuf.writeIntLE(0);
                    } else {
                        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                        byteBuf.writeIntLE(bytes.length);
                        byteBuf.writeBytes(bytes);
                    }
                }
            } else {
                for (String value : stringList) {
                    if (value == null) {
                        byteBuf.writeInt(0);
                    } else {
                        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                        byteBuf.writeInt(bytes.length);
                        byteBuf.writeBytes(bytes);
                    }
                }
            }
        } else {
            final ClassNameSplitter classNameParser = new ClassNameSplitter(itemClass.getName());

            try {
                Class<?> encodingClass = itemClass.getClassLoader().loadClass(
                        classNameParser.getPackageName() + "." + classNameParser.getEncoderClassName());
                Method encodeMethod = encodingClass.getMethod("encode", itemClass, ByteBuf.class, Boolean.TYPE, BitSet.class);

                for (Object obj : toEncode) {
                    encodeMethod.invoke(null, obj, byteBuf, le, features);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static <K, V> void encodeMap(
            Map<K, V> toEncode,
            ByteBuf byteBuf,
            boolean le,
            BitSet features,
            Class<K> keyClass,
            Class<V> valueClass) {
        if (toEncode.isEmpty()) {
            encode(0, byteBuf, le);
            return;
        } else {
            encode(toEncode.size(), byteBuf, le);
        }

        final ParameterType keyType = ParameterType.fromClass(keyClass);
        final ParameterType valueType = ParameterType.fromClass(valueClass);

        try {
            final Method keyEncodeMethod;
            if (keyType == ParameterType.CEPH_TYPE) {
                final ClassNameSplitter keyClassNameParser = new ClassNameSplitter(keyClass.getName());
                final Class<?> encodingKeyClass = keyClass.getClassLoader().loadClass(
                        keyClassNameParser.getPackageName() + "." + keyClassNameParser.getEncoderClassName());
                keyEncodeMethod = encodingKeyClass.getMethod("encode", keyClass, ByteBuf.class, Boolean.TYPE, BitSet.class);
            } else {
                keyEncodeMethod = null;
            }

            final Method valueEncodeMethod;
            if (valueType == ParameterType.CEPH_TYPE) {
                final ClassNameSplitter valueClassNameParser = new ClassNameSplitter(valueClass.getName());
                final Class<?> encodingValueClass = valueClass.getClassLoader().loadClass(
                        valueClassNameParser.getPackageName() + "." + valueClassNameParser.getEncoderClassName());
                valueEncodeMethod = encodingValueClass.getMethod("encode", valueClass, ByteBuf.class, Boolean.TYPE, BitSet.class);
            } else {
                valueEncodeMethod = null;
            }

            for (Map.Entry<K, V> entry : toEncode.entrySet()) {
                switch (keyType) {
                    case BYTE:
                        final Byte byteValue = (Byte) entry.getKey();
                        byteBuf.writeByte(byteValue == null ? 0 : byteValue);
                        break;
                    case SHORT:
                        final Short shortValue = (Short) entry.getKey();
                        encode(shortValue == null ? (short) 0 : shortValue, byteBuf, le);
                        break;
                    case INT:
                        final Integer intValue = (Integer) entry.getKey();
                        encode(intValue == null ? 0 : intValue, byteBuf, le);
                        break;
                    case LONG:
                        final Long longValue = (Long) entry.getKey();
                        encode(longValue == null ? 0 : longValue, byteBuf, le);
                        break;
                    case STRING:
                        final String stringValue = (String) entry.getKey();
                        encodeString(stringValue, byteBuf, le);
                        break;
                    default:
                        keyEncodeMethod.invoke(null, entry.getKey(), byteBuf, le, features);
                        break;
                }

                switch (valueType) {
                    case BYTE:
                        final Byte byteValue = (Byte) entry.getValue();
                        byteBuf.writeByte(byteValue == null ? 0 : byteValue);
                        break;
                    case SHORT:
                        final Short shortValue = (Short) entry.getValue();
                        encode(shortValue == null ? (short) 0 : shortValue, byteBuf, le);
                        break;
                    case INT:
                        final Integer intValue = (Integer) entry.getValue();
                        encode(intValue == null ? 0 : intValue, byteBuf, le);
                        break;
                    case LONG:
                        final Long longValue = (Long) entry.getValue();
                        encode(longValue == null ? 0L : longValue, byteBuf, le);
                        break;
                    case STRING:
                        final String stringValue = (String) entry.getValue();
                        encodeString(stringValue, byteBuf, le);
                        break;
                    default:
                        valueEncodeMethod.invoke(null, entry.getValue(), byteBuf, le, features);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
