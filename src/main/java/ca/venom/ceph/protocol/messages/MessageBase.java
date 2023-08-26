package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.types.UInt8;
import ca.venom.ceph.CephCRC32C;
import ca.venom.ceph.protocol.Message;
import ca.venom.ceph.protocol.types.UInt16;
import ca.venom.ceph.protocol.types.UInt32;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class MessageBase implements Message {
    protected static class SectionMetadata {
        private final int length;
        private final int alignment;

        public SectionMetadata(int length, int alignment) {
            this.length = length;
            this.alignment = alignment;
        }

        public int getLength() {
            return length;
        }

        public int getAlignment() {
            return alignment;
        }
    }

    private UInt8 flags;

    public UInt8 getFlags() {
        return flags;
    }

    public void setFlags(UInt8 flags) {
        this.flags = flags;
    }

    protected abstract UInt8 getTag();

    protected abstract SectionMetadata[] getSectionMetadatas();

    protected abstract void encodeSection(int section, ByteBuffer byteBuffer) throws IOException;

    protected abstract void decodeSection(int section, ByteBuffer byteBuffer) throws IOException;

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

    public void decode(InputStream stream) throws IOException {
        byte[] bytes = readBytes(stream, 31);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        
        byteBuffer.get(); // Num segments

        int[] segmentsLengths = new int[4];
        for (int i = 0; i < 4; i++) {
            segmentsLengths[i] = (int) new UInt32(byteBuffer).getValue();
            byteBuffer.get();
            byteBuffer.get();
        }

        flags = new UInt8(byteBuffer);
        byteBuffer.get();

        UInt32 crc = new UInt32(byteBuffer);
        byte[] fullBytes = new byte[28];
        fullBytes[0] = (byte) getTag().getValue();
        System.arraycopy(bytes, 0, fullBytes, 1, 27);
        CephCRC32C crc32cGenerator = new CephCRC32C();
        crc32cGenerator.update(fullBytes);
        if (!crc.equals(UInt32.fromValue(crc32cGenerator.getValue()))) {
            throw new IOException("Header checksum validation failed");
        }

        List<UInt32> sectionCrcs = new ArrayList<>();
        for (int i = 0; i < segmentsLengths.length; i++) {
            byte[] segmentBytes;
            if (segmentsLengths[i] > 0) {
                segmentBytes = readBytes(stream, segmentsLengths[i]);
                decodeSection(i, ByteBuffer.wrap(segmentBytes));
            } else {
                continue;
            }

            byte[] crcBytes = readBytes(stream, 4);
            crc = new UInt32(ByteBuffer.wrap(crcBytes));
            crc32cGenerator.reset(-1L);
            crc32cGenerator.update(segmentBytes);

            if (i == 0) {
                if (!crc.equals(UInt32.fromValue(crc32cGenerator.getValue()))) {
                    throw new IOException("Section 1 checksum validation failed");
                }
            } else {
                sectionCrcs.add(crc);
            }
        }

        byte lateStatus = (byte) stream.read();
        byte[] crcBytes = readBytes(stream, 4 * sectionCrcs.size());
        ByteBuffer crcByteBuffer = ByteBuffer.wrap(crcBytes);

        int sectionNum = 2;
        for (UInt32 sectionCrc : sectionCrcs) {
            UInt32 receivedCrc = new UInt32(crcByteBuffer);
            if (!receivedCrc.equals(sectionCrc)) {
                throw new IOException("Section " + sectionNum + " checksum validation failed");
            }
            sectionNum++;
        }
    }

    public void encode(OutputStream stream) throws IOException {
        int length = 32;

        SectionMetadata[] sectionMetadatas = getSectionMetadatas();
        int sectionsCount = 0;
        for (SectionMetadata sectionMetadata : sectionMetadatas) {
            if (sectionMetadata.length > 0) {
                sectionsCount++;
                length += sectionMetadata.length + 4;
            }
        }

        if (sectionsCount > 1) {
            length++; // late status
        }

        byte[] msgBytes = new byte[length];
        ByteBuffer byteBuffer = ByteBuffer.wrap(msgBytes);
        getTag().encode(byteBuffer);
        byteBuffer.put((byte) sectionsCount);

        for (SectionMetadata sectionMetadata : sectionMetadatas) {
            UInt32.fromValue(sectionMetadata.length).encode(byteBuffer);
            if (sectionMetadata.length > 0) {
                UInt16.fromValue(sectionMetadata.alignment).encode(byteBuffer);
            } else {
                byteBuffer.put((byte) 0);
                byteBuffer.put((byte) 0);
            }
        }

        flags.encode(byteBuffer);
        byteBuffer.put((byte) 0);

        CephCRC32C crc32c = new CephCRC32C();
        crc32c.update(msgBytes, 0, 28);
        UInt32.fromValue(crc32c.getValue()).encode(byteBuffer);

        for (int i = 0; i < sectionMetadatas.length; i++) {
            if (sectionMetadatas[i].length > 0) {
                encodeSection(i, byteBuffer);
            }

            if (i == 0) {
                // Insert hole for CRC32 checksum
                byteBuffer.put(new byte[4]);
            }
        }

        int epiloguePosition = byteBuffer.position();

        // First section checksum
        crc32c.reset(-1L);
        crc32c.update(msgBytes, 32, sectionMetadatas[0].length);
        byteBuffer.position(32 + sectionMetadatas[0].length);
        UInt32.fromValue(crc32c.getValue()).encode(byteBuffer);

        if (sectionsCount > 1) {
            // Late status
            byteBuffer.put((byte) 0);

            byteBuffer.position(epiloguePosition);
            int offset = 36 + sectionMetadatas[0].length;
            for (int i = 1; i < 4; i++) {
                if (sectionMetadatas[i].length > 0) {
                    crc32c.reset(-1L);
                    crc32c.update(msgBytes, offset, sectionMetadatas[i].length);
                    UInt32.fromValue(crc32c.getValue()).encode(byteBuffer);
                    offset += sectionMetadatas[i].length;
                }
            }
        }

        stream.write(msgBytes);
    }
}
