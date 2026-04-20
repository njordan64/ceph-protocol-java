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
import ca.venom.ceph.protocol.messages.CephMsgHeader2;
import ca.venom.ceph.protocol.messages.MessagePayload;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CephDecoder {
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

    public static short decodeShort(ByteBuf byteBuf, boolean le) {
        if (le) {
            return byteBuf.readShortLE();
        } else {
            return byteBuf.readShort();
        }
    }

    public static int decodeInt(ByteBuf byteBuf, boolean le) {
        if (le) {
            return byteBuf.readIntLE();
        } else {
            return byteBuf.readInt();
        }
    }

    public static long decodeLong(ByteBuf byteBuf, boolean le) {
        if (le) {
            return byteBuf.readLongLE();
        } else {
            return byteBuf.readLong();
        }
    }

    public static String decodeString(ByteBuf byteBuf, boolean le) {
        int length = decodeInt(byteBuf, le);

        if (length == 0) {
            return "";
        }

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static byte[] decodeBytes(ByteBuf byteBuf, boolean le) {
        int length = decodeInt(byteBuf, le);

        if (length == 0) {
            return new byte[0];
        }

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    public static <T> List<T> decodeList(ByteBuf byteBuf, boolean le, BitSet features, Class<T> valueClass) throws DecodingException {
        final int length = decodeInt(byteBuf, le);

        if (length == 0) {
            return Collections.emptyList();
        }

        if (valueClass.equals(Byte.class)) {
            final List<Byte> byteList = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                byteList.add(byteBuf.readByte());
            }
            return (List<T>) byteList;
        } else if (valueClass.equals(Short.class)) {
            final List<Short> shortList = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                shortList.add(decodeShort(byteBuf, le));
            }
            return (List<T>) shortList;
        } else if (valueClass.equals(Integer.class)) {
            final List<Integer> intList = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                intList.add(decodeInt(byteBuf, le));
            }
            return (List<T>) intList;
        } else if (valueClass.equals(Long.class)) {
            final List<Long> longList = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                longList.add(decodeLong(byteBuf, le));
            }
            return (List<T>) longList;
        } else if (valueClass.equals(String.class)) {
            final List<String> stringList = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                stringList.add(decodeString(byteBuf, le));
            }
            return (List<T>) stringList;
        } else {
            final List<T> valueList = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                valueList.add(decode(byteBuf, le, features, valueClass));
            }
            return valueList;
        }
    }

    public static <T> Set<T> decodeSet(ByteBuf byteBuf, boolean le, BitSet features, Class<T> valueClass) throws DecodingException {
        final int length = decodeInt(byteBuf, le);

        if (length == 0) {
            return Collections.emptySet();
        }

        if (valueClass.equals(Byte.class)) {
            final Set<Byte> byteSet = new HashSet<>(length);
            for (int i = 0; i < length; i++) {
                byteSet.add(byteBuf.readByte());
            }
            return (Set<T>) byteSet;
        } else if (valueClass.equals(Short.class)) {
            final Set<Short> shortSet = new HashSet<>(length);
            for (int i = 0; i < length; i++) {
                shortSet.add(decodeShort(byteBuf, le));
            }
            return (Set<T>) shortSet;
        } else if (valueClass.equals(Integer.class)) {
            final Set<Integer> intSet = new HashSet<>(length);
            for (int i = 0; i < length; i++) {
                intSet.add(decodeInt(byteBuf, le));
            }
            return (Set<T>) intSet;
        } else if (valueClass.equals(Long.class)) {
            final Set<Long> longSet = new HashSet<>(length);
            for (int i = 0; i < length; i++) {
                longSet.add(decodeLong(byteBuf, le));
            }
            return (Set<T>) longSet;
        } else if (valueClass.equals(String.class)) {
            final Set<String> stringSet = new HashSet<>(length);
            for (int i = 0; i < length; i++) {
                stringSet.add(decodeString(byteBuf, le));
            }
            return (Set<T>) stringSet;
        } else {
            final Set<T> valueSet = new HashSet<>(length);
            for (int i = 0; i < length; i++) {
                valueSet.add(decode(byteBuf, le, features, valueClass));
            }
            return valueSet;
        }
    }

    public static <K, V> Map<K, V> decodeMap(
            ByteBuf byteBuf,
            boolean le,
            BitSet features,
            Class<K> keyClass,
            Class<V> valueClass) throws DecodingException {
        final int length = decodeInt(byteBuf, le);

        if (length == 0) {
            return Collections.emptyMap();
        }

        final ParameterType keyType = ParameterType.fromClass(keyClass);
        final ParameterType valueType = ParameterType.fromClass(valueClass);

        try {
            final Method keyDecodeMethod;
            if (keyType == ParameterType.CEPH_TYPE) {
                final ClassNameSplitter parsedClassName = new ClassNameSplitter(keyClass.getName());
                final String packageName = parsedClassName.getPackageName();
                final String className = parsedClassName.getEncoderClassName();
                final Class<?> keyDecodingClass = valueClass.getClassLoader().loadClass(packageName + "." + className);
                keyDecodeMethod = keyDecodingClass.getMethod("decode", ByteBuf.class, Boolean.TYPE, BitSet.class);
            } else {
                keyDecodeMethod = null;
            }

            final Method valueDecodeMethod;
            if (keyType == ParameterType.CEPH_TYPE) {
                final ClassNameSplitter parsedClassName = new ClassNameSplitter(valueClass.getName());
                final String packageName = parsedClassName.getPackageName();
                final String className = parsedClassName.getEncoderClassName();
                final Class<?> valueDecodingClass = valueClass.getClassLoader().loadClass(packageName + "." + className);
                valueDecodeMethod = valueDecodingClass.getMethod("decode", ByteBuf.class, Boolean.TYPE, BitSet.class);
            } else {
                valueDecodeMethod = null;
            }

            final Map<Object, Object> decodedMap = new HashMap<>();
            for (int i = 0; i < length; i++) {
                final Object key;
                switch (keyType) {
                    case BYTE:
                        key = byteBuf.readByte();
                        break;
                    case SHORT:
                        key = decodeShort(byteBuf, le);
                        break;
                    case INT:
                        key = decodeInt(byteBuf, le);
                        break;
                    case LONG:
                        key = decodeLong(byteBuf, le);
                        break;
                    case STRING:
                        key = decodeString(byteBuf, le);
                        break;
                    default:
                        key = decode(byteBuf, le, features, keyClass);
                        break;
                }

                final Object value;
                switch (valueType) {
                    case BYTE:
                        value = byteBuf.readByte();
                        break;
                    case SHORT:
                        value = decodeShort(byteBuf, le);
                        break;
                    case INT:
                        value = decodeInt(byteBuf, le);
                        break;
                    case LONG:
                        value = decodeLong(byteBuf, le);
                        break;
                    case STRING:
                        value = decodeString(byteBuf, le);
                        break;
                    default:
                        value = decode(byteBuf, le, features, valueClass);
                        break;
                }

                decodedMap.put(key, value);
            }

            return (Map<K, V>) decodedMap;
        } catch (DecodingException de) {
            throw de;
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getCause() instanceof DecodingException decodingException) {
                throw decodingException;
            }

            return null;
        }
    }

    public static <T> T decode(ByteBuf byteBuf, boolean le, BitSet features, Class<T> valueClass) throws DecodingException {
        final ClassNameSplitter parsedClassName = new ClassNameSplitter(valueClass.getName());
        final String packageName = parsedClassName.getPackageName();
        final String className = parsedClassName.getEncoderClassName();

        try {
            final Class<?> decodingClass = valueClass.getClassLoader().loadClass(packageName + "." + className);
            final Method decodeMethod = decodingClass.getMethod("decode", ByteBuf.class, Boolean.TYPE, BitSet.class);
            return (T) decodeMethod.invoke(null, byteBuf, le, features);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getCause() instanceof DecodingException decodingException) {
                throw decodingException;
            }

            return null;
        }
    }

    public static <T> T decode(ByteBuf byteBuf, boolean le, BitSet features, Class<T> valueClass, int typeCode) throws DecodingException {
        final ClassNameSplitter parsedClassName = new ClassNameSplitter(valueClass.getName());
        final String packageName = parsedClassName.getPackageName();
        final String className = parsedClassName.getEncoderClassName();

        try {
            final Class<?> decodingClass = valueClass.getClassLoader().loadClass(packageName + "." + className);
            final Method decodeMethod = decodingClass.getMethod("decode", ByteBuf.class, Boolean.TYPE, BitSet.class, Integer.TYPE);
            return (T) decodeMethod.invoke(null, byteBuf, le, features, typeCode);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getCause() instanceof DecodingException decodingException) {
                throw decodingException;
            }

            return null;
        }
    }

    public static MessagePayload decodeMessagePayload(ByteBuf byteBuf, boolean le, BitSet features, CephMsgHeader2 header) throws DecodingException {
        final String packageName = "ca.venom.ceph.protocol.messages";
        final String className = "MessagePayloadEncoder";

        try {
            final Class<?> decodingClass = MessagePayload.class.getClassLoader().loadClass(packageName + "." + className);
            final Method decodeMethod = decodingClass.getMethod(
                    "decode",
                    ByteBuf.class,
                    Boolean.TYPE,
                    BitSet.class,
                    CephMsgHeader2.class
            );
            return (MessagePayload) decodeMethod.invoke(null, byteBuf, le, features, header);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getCause() instanceof DecodingException decodingException) {
                throw decodingException;
            }

            return null;
        }
    }
}
