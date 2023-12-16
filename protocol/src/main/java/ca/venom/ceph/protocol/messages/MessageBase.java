/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.DecodingException;
import ca.venom.ceph.protocol.EncodingException;
import ca.venom.ceph.protocol.frames.MessageFrame;
import ca.venom.ceph.protocol.types.annotations.CephEncodingSize;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;

import java.util.BitSet;

public abstract class MessageBase {
    @CephType
    public static class MessageHead {
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
        private short priority;

        @Getter
        @Setter
        @CephField(order = 4)
        private short version;

        @Getter
        @Setter
        @CephField(order = 5)
        private int prePaddingLen;

        @Getter
        @Setter
        @CephField(order = 6)
        private short dataOffset;

        @Getter
        @Setter
        @CephField(order = 7)
        private long ackSeq;

        @Getter
        @Setter
        @CephField(order = 8)
        @CephEncodingSize(1)
        private BitSet flags;

        @Getter
        @Setter
        @CephField(order = 9)
        private short compatVersion;

        @Getter
        @Setter
        @CephField(order = 10)
        private short reserved;
    }

    @Getter
    @Setter
    private MessageHead messageHead;

    protected abstract MessageType getType();

    public void encode(MessageFrame messageFrame) throws EncodingException {
        ByteBuf byteBuf = Unpooled.buffer();
        CephEncoder.encode(messageHead, byteBuf, true);

        MessageFrame.Segment segment = new MessageFrame.Segment();
        byte[] bytes = new byte[byteBuf.writerIndex()];
        byteBuf.readBytes(bytes);
        segment.setEncodedBytes(bytes);
        segment.setLe(true);

        messageFrame.setHead(segment);
    }

    public void decode(MessageFrame messageFrame) throws DecodingException {
        messageHead = CephDecoder.decode(
                Unpooled.wrappedBuffer(messageFrame.getHead().getEncodedBytes()),
                messageFrame.getHead().isLe(),
                MessageHead.class);
    }

    protected void encodePayload(MessageFrame messageFrame) throws EncodingException {
    }

    protected void decodePayload(MessageFrame messageFrame) throws DecodingException {
    }
}
