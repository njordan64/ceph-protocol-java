package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.CephCRC32C;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt16;
import ca.venom.ceph.protocol.types.UInt32;
import ca.venom.ceph.protocol.types.UInt64;
import ca.venom.ceph.protocol.types.UInt8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public abstract class ControlFrame {
    private static class SegmentMetadata {
        public int size;
        public int alignment;
    }

    private static final int MAX_SEGMENTS = 4;

    private BitSet flags = new BitSet(8);
    private BitSet lateStatus = new BitSet(8);

    protected abstract Segment getSegment(int index);

    public abstract MessageType getTag();

    public BitSet getFlags() {
        return flags;
    }

    public BitSet getLateStatus() {
        return lateStatus;
    }

    public byte[] encode() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        SegmentMetadata[] segmentMetadatas = new SegmentMetadata[MAX_SEGMENTS];
        writeHeaderPlaceholder(outputStream);

        byte[] bytes = new byte[4];
        segmentMetadatas[0] = encodeSegment(getSegment(0), outputStream);
        outputStream.write(bytes); // placeholder for segment 1 CRC32C

        for (int i = 1; i < MAX_SEGMENTS; i++) {
            segmentMetadatas[i] = encodeSegment(getSegment(i), outputStream);
        }

        int segmentsCount = 0;
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            if (segmentMetadatas[i].size > 0) {
                segmentsCount++;
            }
        }

        if (segmentsCount > 1) {
            write(lateStatus, outputStream, 1);
            for (int i = 1; i < MAX_SEGMENTS; i++) {
                if (segmentMetadatas[i].size > 0) {
                    outputStream.write(bytes); // placeholder for segment i CRC32C
                }
            }
        }

        bytes = outputStream.toByteArray();
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

        updateHeader(segmentMetadatas, bytes, byteBuffer);
        updateSegmentCrcs(segmentMetadatas, bytes, byteBuffer);

        return bytes;
    }

    public void decode(InputStream inputStream) throws IOException {
        SegmentMetadata[] segmentMetadatas = readHeader(inputStream);
        int segmentsCount = 0;
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            if (segmentMetadatas[i].size > 0) {
                segmentsCount++;
            }
        }

        int bodyLength = 4;
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            bodyLength += segmentMetadatas[i].size;
        }
        int epilogueIndex = bodyLength;

        if (segmentsCount > 1) {
            bodyLength += 1 + 4 * (segmentsCount - 1);
        }

        byte[] bodyBytes = readBytes(inputStream, bodyLength);
        ByteBuffer bodyByteBuffer = ByteBuffer.wrap(bodyBytes);

        bodyByteBuffer.position(segmentMetadatas[0].size);
        checkSegmentCrc32(bodyByteBuffer, bodyBytes, segmentMetadatas[0], 0);

        int crcIndex = epilogueIndex + 1;
        int segmentIndex = segmentMetadatas[0].size;
        for (int i = 1; i < MAX_SEGMENTS; i++) {
            if (segmentMetadatas[i].size > 0) {
                bodyByteBuffer.position(crcIndex);
                checkSegmentCrc32(bodyByteBuffer, bodyBytes, segmentMetadatas[i], segmentIndex);

                crcIndex += 4;
                segmentIndex += segmentMetadatas[i].size;
            }
        }

        bodyByteBuffer.position(0);
        Segment segment = getSegment(0);
        if (segment != null) {
            segment.decode(bodyByteBuffer);
        }
        bodyByteBuffer.position(bodyByteBuffer.position() + 4); // Skip over CRC32

        for (int i = 1; i < MAX_SEGMENTS; i++) {
            segment = getSegment(i);
            if (segment != null) {
                segment.decode(bodyByteBuffer);
            }
        }

        if (segmentsCount > 1) {
            lateStatus = readBitSet(bodyByteBuffer, 1);
        }
    }

    private SegmentMetadata[] readHeader(InputStream inputStream) throws IOException {
        byte[] headerBytes = new byte[32];
        headerBytes[0] = (byte) getTag().getTagNum().getValue();
        readBytes(inputStream, headerBytes, 1, 31);
        ByteBuffer byteBuffer = ByteBuffer.wrap(headerBytes);

        byteBuffer.position(1); // Skip over tag
        int segmentsCount = readUInt8(byteBuffer).getValue();

        SegmentMetadata[] segmentMetadatas = new SegmentMetadata[4];
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            segmentMetadatas[i] = new SegmentMetadata();
            segmentMetadatas[i].size = (int) readUInt32(byteBuffer).getValue();
            segmentMetadatas[i].alignment = readUInt16(byteBuffer).getValue();
        }

        flags = readBitSet(byteBuffer, 1);
        byteBuffer.get(); // Skip over reserved byte

        UInt32 crc32 = readUInt32(byteBuffer);
        CephCRC32C cephCRC32C = new CephCRC32C();
        cephCRC32C.update(headerBytes, 0, 28);
        if (cephCRC32C.getValue() != crc32.getValue()) {
            throw new IOException("Header checksum validation failed");
        }

        return segmentMetadatas;
    }

    private void checkSegmentCrc32(ByteBuffer byteBuffer, byte[] bytes, SegmentMetadata segmentMetadata, int offset) throws IOException {
        UInt32 crc32 = UInt32.read(byteBuffer);
        CephCRC32C cephCRC32C = new CephCRC32C(-1L);
        cephCRC32C.update(bytes, offset, segmentMetadata.size);
        if (cephCRC32C.getValue() != crc32.getValue()) {
            throw new IOException("Segment checksum validation failed");
        }
    }

    private void writeHeaderPlaceholder(ByteArrayOutputStream outputStream) {
        write(getTag().getTagNum(), outputStream);
        outputStream.write(0); // placeholder for number of sections

        byte[] bytes = new byte[6];
        SegmentMetadata[] segmentMetadatas = new SegmentMetadata[MAX_SEGMENTS];
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            outputStream.writeBytes(bytes); // placeholder for segment i size and alignment
        }

        write(flags, outputStream, 1);
        outputStream.write((byte) 0); // reserved

        bytes = new byte[4];
        outputStream.writeBytes(bytes); // placeholder for CRC32C
    }

    private void updateHeader(SegmentMetadata[] segmentMetadatas, byte[] bytes, ByteBuffer byteBuffer) {
        byteBuffer.position(1);
        int segmentsCount = 0;
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            if (segmentMetadatas[i].size > 0) {
                segmentsCount++;
            }
        }
        new UInt8(segmentsCount).encode(byteBuffer);

        for (int i = 0; i < MAX_SEGMENTS; i++) {
            new UInt32(segmentMetadatas[i].size).encode(byteBuffer);
            new UInt16(segmentMetadatas[i].alignment).encode(byteBuffer);
        }

        byteBuffer.position(28);
        CephCRC32C cephCRC32C = new CephCRC32C();
        cephCRC32C.update(bytes, 0, 28);
        new UInt32(cephCRC32C.getValue()).encode(byteBuffer);
    }

    private void updateSegmentCrcs(SegmentMetadata[] segmentMetadatas, byte[] bytes, ByteBuffer byteBuffer) {
        byteBuffer.position(32 + segmentMetadatas[0].size);
        CephCRC32C cephCRC32C = new CephCRC32C(-1L);
        cephCRC32C.update(bytes, 32, segmentMetadatas[0].size);
        new UInt32(cephCRC32C.getValue()).encode(byteBuffer);

        int segmentOffset = 36 + segmentMetadatas[0].size;
        int crcOffset = segmentOffset;
        for (int i = 1; i < 4; i++) {
            crcOffset += segmentMetadatas[i].size;
        }

        for (int i = 1; i < MAX_SEGMENTS; i++) {
            if (segmentMetadatas[i].size > 0) {
                byteBuffer.position(crcOffset);
                cephCRC32C.reset(-1L);
                cephCRC32C.update(bytes, segmentOffset, segmentMetadatas[i].size);
                new UInt32(cephCRC32C.getValue()).encode(byteBuffer);
                segmentOffset += segmentMetadatas[i].size;
            }
        }
    }

    private SegmentMetadata encodeSegment(Segment segment, ByteArrayOutputStream outputStream) throws IOException {
        if (segment == null) {
            return new SegmentMetadata();
        }

        int position = outputStream.size();
        segment.encode(outputStream);

        SegmentMetadata metadata = new SegmentMetadata();
        metadata.size = outputStream.size() - position;
        if (metadata.size > 0) {
            metadata.alignment = segment.getAlignment();
        }

        return metadata;
    }

    protected byte[] readBytes(InputStream stream, int count) throws IOException {
        byte[] bytes = new byte[count];
        int offset = 0;
        while (offset < count) {
            int bytesRead = stream.read(bytes, offset, count - offset);
            if (bytesRead == -1) {
                throw new IOException("Unable to read message");
            }
            offset += bytesRead;
        }

        return bytes;
    }

    protected void readBytes(InputStream stream, byte[] bytes, int offset, int count) throws IOException {
        int position = offset;
        while (count > 0) {
            int bytesRead = stream.read(bytes, position, count);
            if (bytesRead == -1) {
                throw new IOException("Unable to read message");
            }
            position += bytesRead;
            count -= bytesRead;
        }
    }

    protected void write(byte value, ByteArrayOutputStream stream) {
        stream.write(value);
    }

    protected void write(UInt8 value, ByteArrayOutputStream stream) {
        value.encode(stream);
    }

    protected void write(short value, ByteArrayOutputStream stream) {
        stream.write(value & 0xff);
        stream.write((value >> 8) & 0xff);
    }

    protected void write(UInt16 value, ByteArrayOutputStream stream) {
        value.encode(stream);
    }

    protected void write(int value, ByteArrayOutputStream stream) {
        stream.write(value & 0xff);
        stream.write((value >> 8) & 0xff);
        stream.write((value >> 16) & 0xff);
        stream.write((value >> 25) & 0xff);
    }

    protected void write(UInt32 value, ByteArrayOutputStream stream) {
        value.encode(stream);
    }

    protected void write(long value, ByteArrayOutputStream stream) {
        stream.write((int) (value & 0xffL));
        stream.write((int) ((value >> 8) & 0xffL));
        stream.write((int) ((value >> 16) & 0xffL));
        stream.write((int) ((value >> 24) & 0xffL));
        stream.write((int) ((value >> 32) & 0xffL));
        stream.write((int) ((value >> 40) & 0xffL));
        stream.write((int) ((value >> 48) & 0xffL));
        stream.write((int) ((value >> 56) & 0xffL));
    }

    protected void write(UInt64 value, ByteArrayOutputStream stream) {
        value.encode(stream);
    }

    protected void write(String value, ByteArrayOutputStream stream) {
        stream.writeBytes(value.getBytes());
        stream.write(0);
    }

    protected void write(List<?> value, ByteArrayOutputStream stream, Class<?> elementClass) {
        new UInt32(value.size()).encode(stream);

        if (Byte.class.equals(elementClass)) {
            value.forEach(v -> write((byte)v, stream));
        } else if (UInt8.class.equals(elementClass)) {
            value.forEach(v -> ((UInt8) v).encode(stream));
        } else if (Short.class.equals(elementClass)) {
            value.forEach(v -> write((short) v, stream));
        } else if (UInt16.class.equals(elementClass)) {
            value.forEach(v -> ((UInt16) v).encode(stream));
        } else if (Integer.class.equals(elementClass)) {
            value.forEach(v -> write((int) v, stream));
        } else if (UInt32.class.equals(elementClass)) {
            value.forEach(v -> ((UInt32) v).encode(stream));
        } else if (Long.class.equals(elementClass)) {
            value.forEach(v -> write((long) v, stream));
        } else if (UInt64.class.equals(elementClass)) {
            value.forEach(v -> ((UInt64) v).encode(stream));
        } else if (String.class.equals(elementClass)) {
            value.forEach(v -> write((String) v, stream));
        } else {
            throw new IllegalArgumentException("List cannot be encoded: " + elementClass.getName());
        }
    }

    protected void write(byte[] value, ByteArrayOutputStream stream) {
        new UInt32(value.length).encode(stream);
        stream.writeBytes(value);
    }

    protected void write(BitSet value, ByteArrayOutputStream stream, int byteCount) {
        byte[] bytes = value.toByteArray();
        if (bytes.length < byteCount) {
            stream.writeBytes(bytes);
            stream.writeBytes(new byte[byteCount - bytes.length]);
        } else {
            stream.write(bytes, 0, byteCount);
        }
    }

    protected byte readByte(ByteBuffer byteBuffer) {
        return byteBuffer.get();
    }

    protected UInt8 readUInt8(ByteBuffer byteBuffer) {
        return UInt8.read(byteBuffer);
    }

    protected short readShort(ByteBuffer byteBuffer) {
        ByteOrder originalByteOrder = byteBuffer.order();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        short value = byteBuffer.getShort();
        byteBuffer.order(originalByteOrder);

        return value;
    }

    protected UInt16 readUInt16(ByteBuffer byteBuffer) {
        return UInt16.read(byteBuffer);
    }

    protected int readInt(ByteBuffer byteBuffer) {
        ByteOrder originalByteOrder = byteBuffer.order();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int value = byteBuffer.getInt();
        byteBuffer.order(originalByteOrder);

        return value;
    }

    protected UInt32 readUInt32(ByteBuffer byteBuffer) {
        return UInt32.read(byteBuffer);
    }

    protected long readLong(ByteBuffer byteBuffer) {
        ByteOrder originalByteOrder = byteBuffer.order();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        long value = byteBuffer.getLong();
        byteBuffer.order(originalByteOrder);

        return value;
    }

    protected UInt64 readUInt64(ByteBuffer byteBuffer) {
        return UInt64.read(byteBuffer);
    }

    protected String readString(ByteBuffer byteBuffer) {
        int startPosition = byteBuffer.position();
        while (byteBuffer.position() < byteBuffer.capacity()) {
            if (byteBuffer.get() == 0) {
                break;
            }
        }

        byte[] stringBytes = new byte[byteBuffer.position() - startPosition - 1];
        byteBuffer.get(startPosition, stringBytes);

        return new String(stringBytes);
    }

    protected <T> List<T> readList(ByteBuffer byteBuffer, Class<T> elementType) {
        int elementCount = (int) UInt32.read(byteBuffer).getValue();
        List<Object> list = new ArrayList<>(elementCount);

        ByteOrder originalByteOrder = byteBuffer.order();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        if (Byte.class.equals(elementType)) {
            for (int i = 0; i < elementCount; i++) {
                list.add(byteBuffer.get());
            }
        } else if (UInt8.class.equals(elementType)) {
            for (int i = 0; i < elementCount; i++) {
                list.add(UInt8.read(byteBuffer));
            }
        } else if (Short.class.equals(elementType)) {
            for (int i = 0; i < elementCount; i++) {
                list.add(byteBuffer.getShort());
            }
        } else if (UInt16.class.equals(elementType)) {
            for (int i = 0; i < elementCount; i++) {
                list.add(UInt16.read(byteBuffer));
            }
        } else if (Integer.class.equals(elementType)) {
            for (int i = 0; i < elementCount; i++) {
                list.add(byteBuffer.getInt());
            }
        } else if (UInt32.class.equals(elementType)) {
            for (int i = 0; i < elementCount; i++) {
                list.add(UInt32.read(byteBuffer));
            }
        } else if (Long.class.equals(elementType)) {
            for (int i = 0; i < elementCount; i++) {
                list.add(byteBuffer.getLong());
            }
        } else if (UInt64.class.equals(elementType)) {
            for (int i = 0; i < elementCount; i++) {
                list.add(UInt64.read(byteBuffer));
            }
        } else if (String.class.equals(elementType)) {
            for (int i = 0; i < elementCount; i++) {
                list.add(readString(byteBuffer));
            }
        } else {
            throw new IllegalArgumentException("List cannot be decoded: " + elementType.getName());
        }
        byteBuffer.order(originalByteOrder);

        return (List<T>) list;
    }

    protected byte[] readByteArray(ByteBuffer byteBuffer) {
        int bytesCount = (int) UInt32.read(byteBuffer).getValue();
        byte[] bytes = new byte[bytesCount];
        byteBuffer.get(bytes);

        return bytes;
    }

    protected BitSet readBitSet(ByteBuffer byteBuffer, int byteCount) {
        byte[] bytes = new byte[byteCount];
        byteBuffer.get(bytes);
        return BitSet.valueOf(bytes);
    }
}
