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
import ca.venom.ceph.protocol.messages.CephMsgHeader2;
import ca.venom.ceph.protocol.messages.MMonGetMap;
import ca.venom.ceph.protocol.messages.MMonMap;
import ca.venom.ceph.protocol.messages.MessagePayload;
import ca.venom.ceph.types.MessageType;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

public class MessageFrame extends ControlFrame {
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
    private CephMsgHeader2 head;

    @Getter
    @Setter
    private MessagePayload payload;

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.MESSAGE;
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) throws EncodingException {
        if (payload != null) {
            payload.prepareForEncode();

            head.setType(getMessageTypeCode());
            head.setVersion(payload.getHeadVersion());
            head.setCompatVersion(payload.getHeadCompatVersion());
        }
        CephEncoder.encode(head, byteBuf, le);
    }

    @Override
    public void encodeSegment2(ByteBuf byteBuf, boolean le) throws EncodingException {
        if (payload != null) {
            CephEncoder.encode(payload, byteBuf, le);
        }
    }

    @Override
    public void encodeSegment3(ByteBuf byteBuf, boolean le) throws EncodingException {
        if (payload != null) {
            payload.encodeMiddle(byteBuf, le);
        }
    }

    @Override
    public void encodeSegment4(ByteBuf byteBuf, boolean le) throws EncodingException {
        if (payload != null) {
            payload.encodeData(byteBuf, le);
        }
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) throws DecodingException {
        head = CephDecoder.decode(byteBuf, le, CephMsgHeader2.class);
    }

    @Override
    public void decodeSegment2(ByteBuf byteBuf, boolean le) throws DecodingException {
        if (byteBuf.readableBytes() > 0) {
            payload = CephDecoder.decode(byteBuf, le, MessagePayload.class, head.getType());
        }
    }

    @Override
    public void decodeSegment3(ByteBuf byteBuf, boolean le) throws DecodingException {
        if (payload != null) {
            payload.decodeMiddle(byteBuf, le);
        }
    }

    @Override
    public void decodeSegment4(ByteBuf byteBuf, boolean le) throws DecodingException {
        if (payload != null) {
            payload.decodeData(byteBuf, le);
            payload.finishDecode();
        }
    }

    private short getMessageTypeCode() {
        if (payload != null) {
            if (payload instanceof MMonMap) {
                return (short) MessageType.MSG_MON_MAP.getValueInt();
            } else if (payload instanceof MMonGetMap) {
                return (short) MessageType.MSG_MON_GET_MAP.getValueInt();
            }
        }

        return (short) 0;
    }
}
