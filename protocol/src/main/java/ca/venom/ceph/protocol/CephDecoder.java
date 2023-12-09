package ca.venom.ceph.protocol;

import ca.venom.ceph.protocol.types.annotations.ByteOrderPreference;
import ca.venom.ceph.protocol.types.annotations.CephChildType;
import ca.venom.ceph.protocol.types.annotations.CephChildTypes;
import ca.venom.ceph.protocol.types.annotations.CephEncodingSize;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephMarker;
import ca.venom.ceph.protocol.types.annotations.CephParentType;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.annotations.CephTypeSize;
import ca.venom.ceph.protocol.types.annotations.CephTypeVersion;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

    public static <T> T decode(ByteBuf byteBuf, boolean le, Class<T> valueClass) throws DecodingException {
        try {
            valueClass = getValueClass(valueClass, byteBuf, le);
        } catch (Exception e) {
            throw new DecodingException("Unable to determine actual class");
        }

        CephType cephType = valueClass.getAnnotation(CephType.class);
        if (cephType == null) {
            return null;
        }

        CephMarker cephMarker = valueClass.getAnnotation(CephMarker.class);
        if (cephMarker != null && byteBuf.readByte() != cephMarker.value()) {
            throw new DecodingException("Invalid marker value");
        }

        CephTypeVersion typeVersion = valueClass.getAnnotation(CephTypeVersion.class);
        if (typeVersion != null) {
            if (byteBuf.readByte() != typeVersion.version()) {
                throw new DecodingException("Unsupported version");
            }

            if (typeVersion.compatVersion() > 0 && byteBuf.readByte() != typeVersion.compatVersion()) {
                throw new DecodingException("Unsupported compat version");
            }
        }

        CephTypeSize typeSize = valueClass.getAnnotation(CephTypeSize.class);
        if (typeSize != null) {
            int size = le ? byteBuf.readIntLE() : byteBuf.readInt();
            if (byteBuf.writerIndex() - byteBuf.readerIndex() < size) {
                throw new DecodingException("Not enough bytes available");
            }
        }

        List<FieldWithOrder> fieldsToDecode = new ArrayList<>();
        for (Field field : valueClass.getDeclaredFields()) {
            CephField annotation = field.getAnnotation(CephField.class);
            if (annotation != null) {
                fieldsToDecode.add(new FieldWithOrder(annotation.order(), field, annotation));
            }
        }
        Collections.sort(fieldsToDecode);

        T decodedValue;
        try {
            decodedValue = valueClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new DecodingException(e.getMessage());
        }

        for (FieldWithOrder fieldWithOrder : fieldsToDecode) {
            try {
                String setterName = "set" + fieldWithOrder.field.getName().substring(0, 1).toUpperCase() +
                        fieldWithOrder.field.getName().substring(1);
                Method setter = valueClass.getMethod(
                        setterName,
                        fieldWithOrder.field.getType());
                Object value = decodeField(
                        fieldWithOrder.field.getGenericType(),
                        byteBuf,
                        le,
                        fieldWithOrder.annotation.byteOrderPreference(),
                        fieldWithOrder.annotation.includeSize(),
                        fieldWithOrder.annotation.sizeLength(),
                        fieldWithOrder.field.getAnnotation(CephEncodingSize.class));

                setter.invoke(decodedValue, value);
            } catch (Exception e) {
                throw new DecodingException("No setter found for " + fieldWithOrder.field.getName());
            }
        }

        return decodedValue;
    }

    private static <T> Class<T> getValueClass(Class<T> valueClass, ByteBuf byteBuf, boolean le) throws Exception {
        CephParentType parentType = valueClass.getAnnotation(CephParentType.class);
        if (parentType != null) {
            int offset = byteBuf.readerIndex() + parentType.typeOffset();
            int type;
            switch (parentType.typeSize()) {
                case 1 -> type = byteBuf.getByte(offset);
                case 2 -> type = le ? byteBuf.getShortLE(offset) : byteBuf.getShort(offset);
                case 4 -> type = le ? byteBuf.getIntLE(offset) : byteBuf.getInt(offset);
                default -> throw new DecodingException("Invalid type size");
            }

            CephChildTypes childTypes = valueClass.getAnnotation(CephChildTypes.class);
            for (CephChildType childType : childTypes.value()) {
                if (childType.typeValue() == type) {
                    return (Class<T>) childType.typeClass();
                }
            }

            throw new DecodingException("Could not find type for data structure");
        } else {
            return valueClass;
        }
    }

    private static Boolean decodeBoolean(ByteBuf byteBuf, boolean le) {
        return byteBuf.readByte() != 0;
    }

    private static Byte decodeByte(ByteBuf byteBuf, boolean le) {
        return byteBuf.readByte();
    }

    private static Short decodeShort(ByteBuf byteBuf, boolean le) {
        if (le) {
            return byteBuf.readShortLE();
        } else {
            return byteBuf.readShort();
        }
    }

    private static Integer decodeInt(ByteBuf byteBuf, boolean le) {
        if (le) {
            return byteBuf.readIntLE();
        } else {
            return byteBuf.readInt();
        }
    }

    private static Long decodeLong(ByteBuf byteBuf, boolean le) {
        if (le) {
            return byteBuf.readLongLE();
        } else {
            return byteBuf.readLong();
        }
    }

    private static String decodeString(ByteBuf byteBuf, boolean le) {
        int size = le ? byteBuf.readIntLE() : byteBuf.readInt();
        byte[] bytes = new byte[size];
        byteBuf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static byte[] decodeBytes(ByteBuf byteBuf,
                               boolean le,
                               ByteOrderPreference byteOrderPreference,
                               boolean includeSize,
                               int sizeLength,
                               CephEncodingSize encodingSize) throws DecodingException {
        int size;
        if (includeSize) {
            if (le) {
                switch (sizeLength) {
                    case 1 -> size = byteBuf.readByte();
                    case 2 -> size = byteBuf.readShortLE();
                    case 4 -> size = byteBuf.readIntLE();
                    default -> throw new DecodingException("Invalid size length");
                }
            } else {
                switch (sizeLength) {
                    case 1 -> size = byteBuf.readByte();
                    case 2 -> size = byteBuf.readShort();
                    case 4 -> size = byteBuf.readInt();
                    default -> throw new DecodingException("Invalid size length");
                }
            }
        } else if (encodingSize != null) {
            size = encodingSize.value();
        } else {
            throw new DecodingException("Cannot determine byte string length");
        }

        byte[] bytes = new byte[size];
        byteBuf.readBytes(bytes);

        if ((le && byteOrderPreference == ByteOrderPreference.BE) ||
                (!le && byteOrderPreference == ByteOrderPreference.LE)) {
            for (int i = 0; i < size / 2; i++) {
                byte b = bytes[i];
                bytes[i] = bytes[size - i - 1];
                bytes[size - i - 1] = b;
            }
        }

        return bytes;
    }

    private static EnumWithIntValue decodeEnum(ByteBuf byteBuf,
                                               boolean le,
                                               CephEncodingSize size,
                                               Class<EnumWithIntValue> valueClass) throws DecodingException {
        int value;
        if (size != null) {
            switch (size.value()) {
                case 1:
                    value = byteBuf.readByte();
                    break;
                case 2:
                    value = le ? byteBuf.readShortLE() : byteBuf.readShort();
                    break;
                case 4:
                    value = le ? byteBuf.readIntLE() : byteBuf.readInt();
                    break;
                default:
                    return null;
            }
        } else {
            throw new DecodingException("No size for enum value");
        }

        for (EnumWithIntValue e : valueClass.getEnumConstants()) {
            if (e.getValueInt() == value) {
                return e;
            }
        }

        return null;
    }

    private static BitSet decodeBitSet(ByteBuf byteBuf,
                                       boolean le,
                                       CephEncodingSize encodingSize) throws DecodingException {
        if (encodingSize == null) {
            throw new DecodingException("No size for bitset value");
        }

        int size = encodingSize.value();
        byte[] bytes = new byte[size];
        byteBuf.readBytes(bytes);

        if (!le) {
            for (int i = 0; i < size / 2; i++) {
                byte b = bytes[size - i - 1];
                bytes[size - i - 1] = bytes[i];
                bytes[i] = b;
            }
        }

        return BitSet.valueOf(bytes);
    }

    private static Set<Object> decodeSet(ByteBuf byteBuf,
                                         boolean le,
                                         Type valueType,
                                         ByteOrderPreference byteOrderPreference) throws DecodingException {
        int size = le ? byteBuf.readIntLE() : byteBuf.readInt();
        Set<Object> decodedSet = new HashSet<>();
        for (int i = 0; i < size; i++) {
            decodedSet.add(decodeField(
                    valueType,
                    byteBuf,
                    le,
                    byteOrderPreference,
                    true,
                    4,
                    null));
        }

        return decodedSet;
    }

    private static List<Object> decodeList(ByteBuf byteBuf,
                                           boolean le,
                                           Type valueType,
                                           ByteOrderPreference byteOrderPreference) throws DecodingException {
        int size = le ? byteBuf.readIntLE() : byteBuf.readInt();
        List<Object> decodedSet = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            decodedSet.add(decodeField(
                    valueType,
                    byteBuf,
                    le,
                    byteOrderPreference,
                    true,
                    4,
                    null));
        }

        return decodedSet;
    }

    private static Map<Object, Object> decodeMap(ByteBuf byteBuf,
                                                 boolean le,
                                                 Type keyType,
                                                 Type valueType,
                                                 ByteOrderPreference byteOrderPreference) throws DecodingException {
        int size = le ? byteBuf.readIntLE() : byteBuf.readInt();
        Map<Object, Object> decodedMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            Object key = decodeField(
                    keyType,
                    byteBuf,
                    le,
                    byteOrderPreference,
                    true,
                    4,
                    null);
            Object value = decodeField(
                    valueType,
                    byteBuf,
                    le,
                    byteOrderPreference,
                    true,
                    4,
                    null);

            decodedMap.put(key, value);
        }

        return decodedMap;
    }

    private static Object decodeField(Type valueType,
                                      ByteBuf byteBuf,
                                      boolean le,
                                      ByteOrderPreference byteOrderPreference,
                                      boolean includeSize,
                                      int sizeLength,
                                      CephEncodingSize encodingSize) throws DecodingException {
        if (valueType instanceof ParameterizedType parameterizedType) {
            if (Set.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                return decodeSet(byteBuf,
                        le,
                        parameterizedType.getActualTypeArguments()[0],
                        byteOrderPreference);
            } else if (List.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                return decodeList(
                        byteBuf,
                        le,
                        parameterizedType.getActualTypeArguments()[0],
                        byteOrderPreference);
            } else if (Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                return decodeMap(
                        byteBuf,
                        le,
                        parameterizedType.getActualTypeArguments()[0],
                        parameterizedType.getActualTypeArguments()[1],
                        byteOrderPreference
                );
            } else {
                Class<?> valueClass = (Class<?>) parameterizedType.getRawType();
                CephType cephType = valueClass.getAnnotation(CephType.class);
                if (cephType != null) {
                    return decode(byteBuf, le, valueClass);
                } else {
                    throw new DecodingException("Type not decodable: " + valueType);
                }
            }
        } else if (valueType == Boolean.TYPE || valueType == Boolean.class) {
            return decodeBoolean(byteBuf, le);
        } else if (valueType == Byte.TYPE || valueType == Byte.class) {
            return decodeByte(byteBuf, le);
        } else if (valueType == Short.TYPE || valueType == Short.class) {
            if (byteOrderPreference == ByteOrderPreference.LE && !le) {
                le = true;
            } else if (byteOrderPreference == ByteOrderPreference.BE && le) {
                le = false;
            }
            return decodeShort(byteBuf, le);
        } else if (valueType == Integer.TYPE || valueType == Integer.class) {
            if (byteOrderPreference == ByteOrderPreference.LE && !le) {
                le = true;
            } else if (byteOrderPreference == ByteOrderPreference.BE && le) {
                le = false;
            }
            return decodeInt(byteBuf, le);
        } else if (valueType == Long.TYPE || valueType == Long.class) {
            if (byteOrderPreference == ByteOrderPreference.LE && !le) {
                le = true;
            } else if (byteOrderPreference == ByteOrderPreference.BE && le) {
                le = false;
            }
            return decodeLong(byteBuf, le);
        } else if (valueType == String.class) {
            return decodeString(byteBuf, le);
        } else if (valueType == byte[].class) {
            return decodeBytes(byteBuf, le, byteOrderPreference, includeSize, sizeLength, encodingSize);
        } else if (EnumWithIntValue.class.isAssignableFrom((Class<?>) valueType)) {
            return decodeEnum(byteBuf, le, encodingSize, (Class<EnumWithIntValue>) valueType);
        } else if (BitSet.class == valueType) {
            return decodeBitSet(byteBuf, le, encodingSize);
        } else {
            final Class<?> valueClass;
            valueClass = (Class<?>) valueType;
            CephType cephType = valueClass.getAnnotation(CephType.class);
            if (cephType != null) {
                return decode(byteBuf, le, valueClass);
            } else {
                throw new DecodingException("Type not decodable: " + valueType);
            }
        }
    }
}
