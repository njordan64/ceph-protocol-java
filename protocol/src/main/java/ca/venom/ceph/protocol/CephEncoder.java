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

import ca.venom.ceph.protocol.types.annotations.ByteOrderPreference;
import ca.venom.ceph.protocol.types.annotations.CephEncodingSize;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephMarker;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.annotations.CephTypeSize;
import ca.venom.ceph.protocol.types.annotations.CephTypeVersion;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CephEncoder {
    private static class FieldWithOrder implements Comparable<FieldWithOrder> {
        private int order;
        private Field field;
        private CephField annotation;

        public FieldWithOrder(int order, Field field, CephField annotation) {
            this.order = order;
            this.field = field;
            this.annotation = annotation;
        }

        @Override
        public int compareTo(FieldWithOrder fieldWithOrder) {
            return order - fieldWithOrder.order;
        }
    }

    public static void encode(Object toEncode, ByteBuf byteBuf, boolean le) throws EncodingException {
        encode(toEncode, toEncode.getClass(), byteBuf, le);
    }

    public static void encode(Object toEncode, Class<?> valueClass, ByteBuf byteBuf, boolean le) throws EncodingException {
        if (valueClass.getAnnotation(CephType.class) == null) {
            return;
        }

        CephMarker marker = valueClass.getAnnotation(CephMarker.class);
        if (marker != null) {
            byteBuf.writeByte(marker.value());
        }

        CephTypeVersion typeVersion = valueClass.getAnnotation(CephTypeVersion.class);
        if (typeVersion != null) {
            byteBuf.writeByte(typeVersion.version());
            if (typeVersion.compatVersion() > 0) {
                byteBuf.writeByte(typeVersion.compatVersion());
            }
        }

        CephTypeSize typeSize = valueClass.getAnnotation(CephTypeSize.class);
        int sizeOffset = -1;
        if (typeSize != null) {
            sizeOffset = byteBuf.writerIndex();
            byteBuf.writeZero(4);
        }

        if (toEncode != null) {
            List<FieldWithOrder> fieldsToEncode = new ArrayList<>();
            for (Field field : toEncode.getClass().getDeclaredFields()) {
                CephField annotation = field.getAnnotation(CephField.class);
                if (annotation != null) {
                    fieldsToEncode.add(new FieldWithOrder(annotation.order(), field, annotation));
                }
            }
            Collections.sort(fieldsToEncode);

            for (FieldWithOrder fieldWithOrder : fieldsToEncode) {
                Object value = getFieldValue(fieldWithOrder.field, toEncode);

                encodeField(
                        value,
                        fieldWithOrder.field.getGenericType(),
                        byteBuf,
                        le,
                        fieldWithOrder.annotation.byteOrderPreference(),
                        fieldWithOrder.annotation.includeSize(),
                        fieldWithOrder.annotation.sizeLength(),
                        fieldWithOrder.field.getAnnotation(CephEncodingSize.class));
            }

            if (sizeOffset >= 0) {
                int size = byteBuf.writerIndex() - sizeOffset - 4;
                if (le) {
                    byteBuf.setIntLE(sizeOffset, size);
                } else {
                    byteBuf.setInt(sizeOffset, size);
                }
            }
        }
    }

    private static Object getFieldValue(Field field, Object obj) {
        String prefix;
        if (field.getType() == Boolean.TYPE || field.getType() == Boolean.class) {
            prefix = "is";
        } else {
            prefix = "get";
        }

        String name = field.getName();
        String getterName = prefix + name.substring(0, 1).toUpperCase() + name.substring(1);

        try {
            Method getter = obj.getClass().getMethod(getterName);
            if (getter != null) {
                return getter.invoke(obj);
            } else {
                return null;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    private static void encodeBoolean(Boolean value, ByteBuf byteBuf) {
        if (value == null) {
            byteBuf.writeByte(0);
        } else {
            byteBuf.writeByte(value == Boolean.TRUE ? 1 : 0);
        }
    }

    private static void encodeByte(Byte value, ByteBuf byteBuf) {
        if (value == null) {
            byteBuf.writeByte(0);
        } else {
            byteBuf.writeByte(value);
        }
    }

    private static void encodeShort(Short value, ByteBuf byteBuf, boolean le) {
        if (value == null) {
            byteBuf.writeZero(2);
        } else {
            if (le) {
                byteBuf.writeShortLE(value);
            } else {
                byteBuf.writeShort(value);
            }
        }
    }

    private static void encodeInt(Integer value, ByteBuf byteBuf, boolean le) {
        if (value == null) {
            byteBuf.writeZero(4);
        } else {
            if (le) {
                byteBuf.writeIntLE(value);
            } else {
                byteBuf.writeInt(value);
            }
        }
    }

    private static void encodeLong(Long value, ByteBuf byteBuf, boolean le) {
        if (value == null) {
            byteBuf.writeZero(8);
        } else {
            if (le) {
                byteBuf.writeLongLE(value);
            } else {
                byteBuf.writeLong(value);
            }
        }
    }

    private static void encodeString(String value, ByteBuf byteBuf, boolean le) {
        if (value == null) {
            byteBuf.writeZero(4);
        } else {
            byte[] strBytes = value.getBytes(StandardCharsets.UTF_8);
            if (le) {
                byteBuf.writeIntLE(strBytes.length);
            } else {
                byteBuf.writeInt(strBytes.length);
            }
            byteBuf.writeBytes(strBytes);
        }
    }

    private static void encodeBytes(byte[] value,
                                    ByteBuf byteBuf,
                                    boolean le,
                                    ByteOrderPreference byteOrderPreference,
                                    boolean includeSize,
                                    int sizeLength,
                                    CephEncodingSize encodingSizeAnnotation) throws EncodingException {
        if (value != null) {
            byte[] bytesToWrite = value;

            if ((byteOrderPreference == ByteOrderPreference.BE && le) ||
                    (byteOrderPreference == ByteOrderPreference.LE && !le)) {
                bytesToWrite = new byte[value.length];
                for (int i = 0; i < bytesToWrite.length / 2; i++) {
                    byte b = bytesToWrite[i];
                    bytesToWrite[i] = bytesToWrite[bytesToWrite.length - 1 - i];
                    bytesToWrite[bytesToWrite.length - 1 - i] = b;
                }
            }

            if (includeSize) {
                if (le) {
                    switch (sizeLength) {
                        case 1 -> byteBuf.writeByte((byte) bytesToWrite.length);
                        case 2 -> byteBuf.writeShortLE((short) bytesToWrite.length);
                        case 4 -> byteBuf.writeIntLE(bytesToWrite.length);
                        default -> throw new EncodingException("Invalid size length");
                    }
                } else {
                    switch (sizeLength) {
                        case 1 -> byteBuf.writeByte((byte) bytesToWrite.length);
                        case 2 -> byteBuf.writeShort((short) bytesToWrite.length);
                        case 4 -> byteBuf.writeInt(bytesToWrite.length);
                        default -> throw new EncodingException("Invalid size length");
                    }
                    byteBuf.writeInt(bytesToWrite.length);
                }
            }
            byteBuf.writeBytes(bytesToWrite);
        } else if (encodingSizeAnnotation != null) {
            if (includeSize) {
                if (le) {
                    byteBuf.writeIntLE(encodingSizeAnnotation.value());
                } else {
                    byteBuf.writeInt(encodingSizeAnnotation.value());
                }
            }
            byteBuf.writeZero(encodingSizeAnnotation.value());
        }
    }

    private static void encodeEnum(EnumWithIntValue value,
                                   ByteBuf byteBuf,
                                   boolean le,
                                   CephEncodingSize encodingSizeAnnotation) {
        if (encodingSizeAnnotation == null) {
            encodingSizeAnnotation = value.getClass().getAnnotation(CephEncodingSize.class);
        }

        int encodingSize = 1;
        if (encodingSizeAnnotation != null) {
            encodingSize = encodingSizeAnnotation.value();
        }

        int intValue = value.getValueInt();
        switch (encodingSize) {
            case 1 -> byteBuf.writeByte(intValue);
            case 2 -> {
                if (le) {
                    byteBuf.writeShortLE(intValue);
                } else {
                    byteBuf.writeShort(intValue);
                }
            }
            case 4 -> {
                if (le) {
                    byteBuf.writeIntLE(intValue);
                } else {
                    byteBuf.writeInt(intValue);
                }
            }
        }
    }

    private static void encodeBitSet(BitSet value,
                                     ByteBuf byteBuf,
                                     boolean le,
                                     CephEncodingSize encodingSizeAnnotation) {
        int encodingSize = 1;
        if (encodingSizeAnnotation != null) {
            encodingSize = encodingSizeAnnotation.value();
        }

        if (value == null) {
            byteBuf.writeZero(encodingSize);
        } else {
            byte[] bytes = new byte[encodingSize];
            byte[] bitsetBytes = value.toByteArray();

            if (le) {
                System.arraycopy(bitsetBytes, 0, bytes, 0, Math.min(encodingSize, bitsetBytes.length));
            } else {
                for (int i = 0; i < Math.min(encodingSize / 2, bitsetBytes.length); i++) {
                    bytes[encodingSize - i - 1] = bitsetBytes[i];
                }
            }

            byteBuf.writeBytes(bytes);
        }
    }

    private static void encodeSet(Set<?> value,
                                  ParameterizedType valueType,
                                  ByteBuf byteBuf,
                                  boolean le,
                                  ByteOrderPreference byteOrderPreference,
                                  boolean includeSize,
                                  int sizeLength,
                                  CephEncodingSize encodingSize) throws EncodingException {
        if (value == null) {
            byteBuf.writeZero(0);
        } else {
            if (le) {
                byteBuf.writeIntLE(value.size());
            } else {
                byteBuf.writeInt(value.size());
            }

            for (Object v : value) {
                encodeField(
                        v,
                        valueType.getActualTypeArguments()[0],
                        byteBuf,
                        le,
                        byteOrderPreference,
                        includeSize,
                        sizeLength,
                        encodingSize);
            }
        }
    }

    private static void encodeList(List<?> value,
                                   ParameterizedType valueType,
                                   ByteBuf byteBuf,
                                   boolean le,
                                   ByteOrderPreference byteOrderPreference,
                                   boolean includeSize,
                                   int sizeLength,
                                   CephEncodingSize encodingSize) throws EncodingException {
        if (value == null) {
            byteBuf.writeZero(0);
        } else {
            if (le) {
                byteBuf.writeIntLE(value.size());
            } else {
                byteBuf.writeInt(value.size());
            }

            for (Object v : value) {
                encodeField(
                        v,
                        valueType.getActualTypeArguments()[0],
                        byteBuf,
                        le,
                        byteOrderPreference,
                        includeSize,
                        sizeLength,
                        encodingSize);
            }
        }
    }

    private static void encodeMap(Map<?, ?> value,
                                  ParameterizedType valueType,
                                  ByteBuf byteBuf,
                                  boolean le,
                                  ByteOrderPreference byteOrderPreference,
                                  boolean includeSize,
                                  int sizeLength,
                                  CephEncodingSize encodingSize) throws EncodingException {
        if (value == null) {
            byteBuf.writeZero(0);
        } else {
            if (le) {
                byteBuf.writeIntLE(value.size());
            } else {
                byteBuf.writeInt(value.size());
            }

            for (Map.Entry entry : value.entrySet()) {
                encodeField(
                        entry.getKey(),
                        valueType.getActualTypeArguments()[0],
                        byteBuf,
                        le,
                        byteOrderPreference,
                        includeSize,
                        sizeLength,
                        encodingSize);
                encodeField(
                        entry.getValue(),
                        valueType.getActualTypeArguments()[1],
                        byteBuf,
                        le,
                        byteOrderPreference,
                        includeSize,
                        sizeLength,
                        encodingSize);
            }
        }
    }

    private static void encodeField(Object value,
                                    Type valueType,
                                    ByteBuf byteBuf,
                                    boolean le,
                                    ByteOrderPreference byteOrderPreference,
                                    boolean includeSize,
                                    int sizeLength,
                                    CephEncodingSize encodingSize) throws EncodingException {
        if (valueType instanceof ParameterizedType parameterizedType) {
            if (Set.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                encodeSet(
                        (Set<?>) value,
                        parameterizedType,
                        byteBuf,
                        le,
                        byteOrderPreference,
                        includeSize,
                        sizeLength,
                        encodingSize);
            } else if (List.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                encodeList(
                        (List<?>) value,
                        parameterizedType,
                        byteBuf,
                        le,
                        byteOrderPreference,
                        includeSize,
                        sizeLength,
                        encodingSize);
            } else if (Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                encodeMap(
                        (Map<?, ?>) value,
                        parameterizedType,
                        byteBuf,
                        le,
                        byteOrderPreference,
                        includeSize,
                        sizeLength,
                        encodingSize);
            } else {
                Class<?> valueClass = (Class<?>) parameterizedType.getRawType();
                CephType cephType = valueClass.getAnnotation(CephType.class);
                if (cephType != null) {
                    encode(value, valueClass, byteBuf, le);
                }
            }
        } else if (valueType == Boolean.TYPE || valueType == Boolean.class) {
            encodeBoolean((Boolean) value, byteBuf);
        } else if (valueType == Byte.TYPE || valueType == Byte.class) {
            encodeByte((Byte) value, byteBuf);
        } else if (valueType == Short.TYPE || valueType == Short.class) {
            if (byteOrderPreference == ByteOrderPreference.LE && !le) {
                le = true;
            } else if (byteOrderPreference == ByteOrderPreference.BE && le) {
                le = false;
            }
            encodeShort((Short) value, byteBuf, le);
        } else if (valueType == Integer.TYPE || valueType == Integer.class) {
            if (byteOrderPreference == ByteOrderPreference.LE && !le) {
                le = true;
            } else if (byteOrderPreference == ByteOrderPreference.BE && le) {
                le = false;
            }
            encodeInt((Integer) value, byteBuf, le);
        } else if (valueType == Long.TYPE || valueType == Long.class) {
            if (byteOrderPreference == ByteOrderPreference.LE && !le) {
                le = true;
            } else if (byteOrderPreference == ByteOrderPreference.BE && le) {
                le = false;
            }
            encodeLong((Long) value, byteBuf, le);
        } else if (valueType == String.class) {
            encodeString((String) value, byteBuf, le);
        } else if (valueType == byte[].class) {
            encodeBytes((byte[]) value, byteBuf, le, byteOrderPreference, includeSize, sizeLength, encodingSize);
        } else if (EnumWithIntValue.class.isAssignableFrom((Class<?>) valueType)) {
            encodeEnum((EnumWithIntValue) value, byteBuf, le, encodingSize);
        } else if (valueType == BitSet.class) {
            encodeBitSet((BitSet) value, byteBuf, le, encodingSize);
        } else {
            final Class<?> valueClass = value.getClass();
            CephType cephType = valueClass.getAnnotation(CephType.class);
            if (cephType != null) {
                encode(value, valueClass, byteBuf, le);
            }
        }
    }
}
