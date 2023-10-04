package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.CephCRC32C;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.BitSet;

public abstract class ControlFrame {
    private static class SegmentMetadata {
        public int size;
        public int alignment;
    }

    private static final int MAX_SEGMENTS = 4;

    private CephBitSet flags = new CephBitSet(new BitSet(8), 1);
    private CephBitSet lateStatus = new CephBitSet(new BitSet(8), 1);

    protected abstract int encodeSegmentBody(int index, ByteArrayOutputStream outputStream);

    protected abstract void decodeSegmentBody(int index, ByteBuffer byteBuffer, int alignment);

    public abstract MessageType getTag();

    public BitSet getFlags() {
        return flags.getValue();
    }

    public BitSet getLateStatus() {
        return lateStatus.getValue();
    }

    public byte[] encode() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        SegmentMetadata[] segmentMetadatas = new SegmentMetadata[MAX_SEGMENTS];
        writeHeaderPlaceholder(outputStream);

        byte[] bytes = new byte[4];
        segmentMetadatas[0] = encodeSegment(0, outputStream);
        outputStream.write(bytes); // placeholder for segment 1 CRC32C

        for (int i = 1; i < MAX_SEGMENTS; i++) {
            segmentMetadatas[i] = encodeSegment(i, outputStream);
        }

        int segmentsCount = 0;
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            if (segmentMetadatas[i].size > 0) {
                segmentsCount++;
            }
        }

        if (segmentsCount > 1) {
            lateStatus.encode(outputStream);
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
        decodeSegmentBody(0, bodyByteBuffer, segmentMetadatas[0].alignment);
        bodyByteBuffer.position(bodyByteBuffer.position() + 4); // Skip over CRC32

        for (int i = 1; i < MAX_SEGMENTS; i++) {
            decodeSegmentBody(i, bodyByteBuffer, segmentMetadatas[i].alignment);
        }

        if (segmentsCount > 1) {
            lateStatus = CephBitSet.read(bodyByteBuffer, 1);
        }
    }

    private SegmentMetadata[] readHeader(InputStream inputStream) throws IOException {
        byte[] headerBytes = new byte[32];
        headerBytes[0] = (byte) getTag().getTagNum().getValue();
        readBytes(inputStream, headerBytes, 1, 31);
        ByteBuffer byteBuffer = ByteBuffer.wrap(headerBytes);

        byteBuffer.position(1); // Skip over tag
        int segmentsCount = UInt8.read(byteBuffer).getValue();

        SegmentMetadata[] segmentMetadatas = new SegmentMetadata[4];
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            segmentMetadatas[i] = new SegmentMetadata();
            segmentMetadatas[i].size = (int) UInt32.read(byteBuffer).getValue();
            segmentMetadatas[i].alignment = UInt16.read(byteBuffer).getValue();
        }

        flags = CephBitSet.read(byteBuffer, 1);
        byteBuffer.get(); // Skip over reserved byte

        UInt32 crc32 = UInt32.read(byteBuffer);
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
        getTag().getTagNum().encode(outputStream);
        outputStream.write(0); // placeholder for number of sections

        byte[] bytes = new byte[6];
        SegmentMetadata[] segmentMetadatas = new SegmentMetadata[MAX_SEGMENTS];
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            outputStream.writeBytes(bytes); // placeholder for segment i size and alignment
        }

        flags.encode(outputStream);
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

    private SegmentMetadata encodeSegment(int segmentIndex, ByteArrayOutputStream outputStream) throws IOException {
        int position = outputStream.size();
        int alignment = encodeSegmentBody(segmentIndex, outputStream);

        SegmentMetadata metadata = new SegmentMetadata();
        metadata.size = outputStream.size() - position;
        if (metadata.size > 0) {
            metadata.alignment = alignment;
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
}
