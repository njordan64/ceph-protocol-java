/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.DecodingException;
import ca.venom.ceph.protocol.EncodingException;
import ca.venom.ceph.protocol.messages.MessagePayload;
import ca.venom.ceph.protocol.messages.MonGetMap;
import ca.venom.ceph.protocol.messages.MonMap;
import ca.venom.ceph.types.MessageType;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

public class MessageFrame extends ControlFrame {
    @CephType
    public static class Header {
        @Getter
        @Setter
        @CephField
        private long seq;

        @Getter
        @Setter
        @CephField(order = 2)
        private long tid;

        @Getter
        @Setter
        @CephField(order = 3)
        private short type;

        @Getter
        @Setter
        @CephField(order = 4)
        private short priority;

        @Getter
        @Setter
        @CephField(order = 5)
        private short version;

        @Getter
        @Setter
        @CephField(order = 6)
        private int dataPrePaddingLen;

        @Getter
        @Setter
        @CephField(order = 7)
        private short dataOff;

        @Getter
        @Setter
        @CephField(order = 8)
        private long ackSeq;

        @Getter
        @Setter
        @CephField(order = 9)
        private byte flags;

        @Getter
        @Setter
        @CephField(order = 10)
        private short compatVersion;

        @Getter
        @Setter
        @CephField(order = 11)
        private short reserved;
    }

    @CephType
    public static class Segment {
        @Getter
        @Setter
        @CephField(includeSize = true)
        private byte[] encodedBytes;

        @Getter
        @Setter
        private boolean le;
    }

    @Getter
    @Setter
    private Header head;

    @Getter
    @Setter
    private MessagePayload front;

    @Getter
    @Setter
    private Segment middle;

    @Getter
    @Setter
    private Segment data;

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.MESSAGE;
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) throws EncodingException {
        if (front != null) {
            head.setType(getMessageTypeCode());
        }
        CephEncoder.encode(head, byteBuf, le);
    }

    @Override
    public void encodeSegment2(ByteBuf byteBuf, boolean le) throws EncodingException {
        if (front != null) {
            CephEncoder.encode(front, byteBuf, le);
        }
    }

    @Override
    public void encodeSegment3(ByteBuf byteBuf, boolean le) throws EncodingException {
        if (middle != null && middle.encodedBytes.length > 0) {
            CephEncoder.encode(middle, byteBuf, le);
        }
    }

    @Override
    public void encodeSegment4(ByteBuf byteBuf, boolean le) throws EncodingException {
        if (data != null && data.encodedBytes.length > 0) {
            CephEncoder.encode(data, byteBuf, le);
        }
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) throws DecodingException {
        head = CephDecoder.decode(byteBuf, le, Header.class);
    }

    @Override
    public void decodeSegment2(ByteBuf byteBuf, boolean le) throws DecodingException {
        if (byteBuf.readableBytes() > 0) {
            // Skip over the length
            byteBuf.readerIndex(byteBuf.readerIndex() + 4);

            front = CephDecoder.decode(byteBuf, le, MessagePayload.class, head.getType());
            head.setType(getMessageTypeCode());
        }
    }

    @Override
    public void decodeSegment3(ByteBuf byteBuf, boolean le) throws DecodingException {
        if (byteBuf.readableBytes() > 0) {
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.getBytes(0, bytes);
            middle = new Segment();
            middle.setEncodedBytes(bytes);
            middle.setLe(le);
        }
    }

    @Override
    public void decodeSegment4(ByteBuf byteBuf, boolean le) throws DecodingException {
        if (byteBuf.readableBytes() > 0) {
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.getBytes(0, bytes);
            data = new Segment();
            data.setEncodedBytes(bytes);
            data.setLe(le);
        }
    }

    private short getMessageTypeCode() {
        if (front != null) {
            if (front instanceof MonMap) {
                return (short) MessageType.CEPH_MSG_MON_MAP.getValueInt();
            } else if (front instanceof MonGetMap) {
                return (short) MessageType.CEPH_MSG_MON_GET_MAP.getValueInt();
            }
        }

        return (short) 0;
    }
}
