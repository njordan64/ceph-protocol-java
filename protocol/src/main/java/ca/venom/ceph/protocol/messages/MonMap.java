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
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;

public class MonMap extends MessageBase {
    @CephType
    public static class Payload {
        @Getter
        @Setter
        @CephField(includeSize = true)
        private byte[] monmap;
    }

    @Getter
    @Setter
    private Payload payload;

    @Override
    protected MessageType getType() {
        return MessageType.CEPH_MSG_MON_MAP;
    }

    @Override
    protected void encodePayload(MessageFrame messageFrame) throws EncodingException {
        ByteBuf byteBuf = Unpooled.buffer();
        CephEncoder.encode(payload, byteBuf, true);

        byte[] bytes = new byte[byteBuf.writerIndex()];
        byteBuf.readBytes(bytes);

        MessageFrame.Segment segment = new MessageFrame.Segment();
        segment.setEncodedBytes(bytes);
        segment.setLe(true);
        messageFrame.setFront(segment);
    }

    @Override
    protected void decodePayload(MessageFrame messageFrame) throws DecodingException {
        payload = CephDecoder.decode(
                Unpooled.wrappedBuffer(messageFrame.getFront().getEncodedBytes()),
                messageFrame.getFront().isLe(),
                Payload.class
        );
    }
}
