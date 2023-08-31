package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.CephCRC32C;
import ca.venom.ceph.protocol.HexFunctions;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt16;
import ca.venom.ceph.protocol.types.UInt32;
import ca.venom.ceph.protocol.types.UInt8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.BitSet;

public abstract class MessageBase {
    private static class SegmentMetadata {
        public int size;
        public int alignment;
    }

    private BitSet flags = new BitSet(8);
    private BitSet lateStatus = new BitSet(8);

    protected abstract Segment getSegment1();

    protected Segment getSegment2() {
        return null;
    }

    protected Segment getSegment3() {
        return null;
    }

    protected Segment getSegment4() {
        return null;
    }

    public abstract MessageType getTag();

    public BitSet getFlags() {
        return flags;
    }

    public BitSet getLateStatus() {
        return lateStatus;
    }

    public byte[] encode() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        SegmentMetadata[] segmentMetadatas = new SegmentMetadata[4];
        writeHeaderPlaceholder(outputStream);

        byte[] bytes = new byte[4];
        segmentMetadatas[0] = encodeSegment(getSegment1(), outputStream);
        outputStream.write(bytes); // placeholder for segment 1 CRC32C
        segmentMetadatas[1] = encodeSegment(getSegment2(), outputStream);
        segmentMetadatas[2] = encodeSegment(getSegment3(), outputStream);
        segmentMetadatas[3] = encodeSegment(getSegment4(), outputStream);

        int segmentsCount = 0;
        for (int i = 0; i < 4; i++) {
            if (segmentMetadatas[i].size > 0) {
                segmentsCount++;
            }
        }

        if (segmentsCount > 1) {
            outputStream.write(bitSetToByte(lateStatus));
            for (int i = 1; i < 4; i++) {
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
        byte[] headerBytes = new byte[32];
        headerBytes[0] = (byte) getTag().getTagNum().getValue();
        readBytes(inputStream, headerBytes, 1, 31);
        ByteBuffer byteBuffer = ByteBuffer.wrap(headerBytes);

        byteBuffer.position(1); // Skip over tag
        int segmentsCount = UInt8.read(byteBuffer).getValue();

        SegmentMetadata[] segmentMetadatas = new SegmentMetadata[4];
        for (int i = 0; i < 4; i++) {
            segmentMetadatas[i] = new SegmentMetadata();
            segmentMetadatas[i].size = (int) UInt32.read(byteBuffer).getValue();
            segmentMetadatas[i].alignment = UInt16.read(byteBuffer).getValue();
        }

        flags = BitSet.valueOf(new byte[] {byteBuffer.get()});
        byteBuffer.get(); // Skip over reserved byte

        UInt32 crc32 = UInt32.read(byteBuffer);
        CephCRC32C cephCRC32C = new CephCRC32C();
        cephCRC32C.update(headerBytes, 0, 28);
        if (cephCRC32C.getValue() != crc32.getValue()) {
            throw new IOException("Header checksum validation failed");
        }

        int bodyLength = 4;
        for (int i = 0; i < 4; i++) {
            bodyLength += segmentMetadatas[i].size;
        }
        int epilogueIndex = bodyLength;

        if (segmentsCount > 1) {
            bodyLength += 1 + 4 * (segmentsCount - 1);
        }

        byte[] bodyBytes = readBytes(inputStream, bodyLength);
        ByteBuffer bodyByteBuffer = ByteBuffer.wrap(bodyBytes);

        bodyByteBuffer.position(segmentMetadatas[0].size);
        crc32 = UInt32.read(bodyByteBuffer);
        cephCRC32C.reset(-1L);
        cephCRC32C.update(bodyBytes, 0, segmentMetadatas[0].size);
        if (cephCRC32C.getValue() != crc32.getValue()) {
            throw new IOException("Segment 1 checksum validation failed");
        }

        int crcIndex = epilogueIndex + 1;
        int segmentIndex = segmentMetadatas[0].size;
        for (int i = 1; i < 4; i++) {
            if (segmentMetadatas[i].size > 0) {
                bodyByteBuffer.position(crcIndex);
                crc32 = UInt32.read(bodyByteBuffer);
                cephCRC32C.reset(-1L);
                cephCRC32C.update(bodyBytes, segmentIndex, segmentMetadatas[i].size);
                if (cephCRC32C.getValue() != crc32.getValue()) {
                    throw new IOException("Segment " + (i + 1) + " checksum validation failed");
                }

                crcIndex += 4;
                segmentIndex += segmentMetadatas[i].size;
            }
        }

        bodyByteBuffer.position(0);
        Segment segment = getSegment1();
        if (segment != null) {
            segment.decode(bodyByteBuffer);
        }
        bodyByteBuffer.position(bodyByteBuffer.position() + 4); // Skip over CRC32

        segment = getSegment2();
        if (segment != null) {
            segment.decode(bodyByteBuffer);
        }

        segment = getSegment3();
        if (segment != null) {
            segment.decode(bodyByteBuffer);
        }

        segment = getSegment4();
        if (segment != null) {
            segment.decode(bodyByteBuffer);
        }

        if (segmentsCount > 1) {
            lateStatus = BitSet.valueOf(new byte[] {bodyBytes[epilogueIndex]});
        }
    }

    private void writeHeaderPlaceholder(ByteArrayOutputStream outputStream) {
        getTag().getTagNum().encode(outputStream);
        outputStream.write(0); // placeholder for number of sections

        byte[] bytes = new byte[6];
        SegmentMetadata[] segmentMetadatas = new SegmentMetadata[4];
        outputStream.writeBytes(bytes); // placeholder for segment 1 size and alignment
        outputStream.writeBytes(bytes); // placeholder for segment 2 size and alignment
        outputStream.writeBytes(bytes); // placeholder for segment 3 size and alignment
        outputStream.writeBytes(bytes); // placeholder for segment 4 size and alignment

        outputStream.write(bitSetToByte(flags));
        outputStream.write((byte) 0); // reserved

        bytes = new byte[4];
        outputStream.writeBytes(bytes); // placeholder for CRC32C
    }

    private void updateHeader(SegmentMetadata[] segmentMetadatas, byte[] bytes, ByteBuffer byteBuffer) {
        byteBuffer.position(1);
        int segmentsCount = 0;
        for (int i = 0; i < 4; i++) {
            if (segmentMetadatas[i].size > 0) {
                segmentsCount++;
            }
        }
        new UInt8(segmentsCount).encode(byteBuffer);

        for (int i = 0; i < 4; i++) {
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

        for (int i = 1; i < 4; i++) {
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

    private byte bitSetToByte(BitSet bitSet) {
        byte[] bytes = bitSet.toByteArray();
        if (bytes.length == 0) {
            return (byte) 0;
        } else {
            return bytes[0];
        }
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
