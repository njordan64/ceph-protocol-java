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

import ca.venom.ceph.protocol.frames.*;

public enum ControlFrameType {
    HELLO(1, HelloFrame.class),
    AUTH_REQUEST(2, AuthRequestFrame.class),
    AUTH_BAD_METHOD(3, AuthBadMethodFrame.class),
    AUTH_REPLY_MORE(4, AuthReplyMoreFrame.class),
    AUTH_REQUEST_MORE(5, AuthRequestMoreFrame.class),
    AUTH_DONE(6, AuthDoneFrame.class),
    AUTH_SIGNATURE(7, AuthSignatureFrame.class),
    CLIENT_IDENT(8, ClientIdentFrame.class),
    SERVER_IDENT(9, ServerIdentFrame.class),
    IDENT_MISSING_FEATURES(10, IdentMissingFeaturesFrame.class),
    SESSION_RECONNECT(11, ReconnectFrame.class),
    SESSION_RESET(12, ResetFrame.class),
    SESSION_RETRY(13, RetryFrame.class),
    SESSION_RETRY_GLOBAL(14, RetryGlobalFrame.class),
    SESSION_RECONNECT_OK(15, ReconnectOkFrame.class),
    WAIT(16, WaitFrame.class),
    MESSAGE(17, MessageFrame.class),
    KEEPALIVE2(18, KeepAliveFrame.class),
    KEEPALIVE2_ACK(19, KeepAliveAck.class),
    ACK(20, AckFrame.class),
    COMPRESSION_REQUEST(21, CompressionRequestFrame.class),
    COMPRESSION_DONE(22, CompressionDoneFrame.class);

    private final int tagNum;
    private final Class<? extends ControlFrame> clazz;

    private ControlFrameType(int tagNum, Class<? extends ControlFrame> clazz) {
        this.tagNum = tagNum;
        this.clazz = clazz;
    }

    public static ControlFrameType getFromTagNum(int tagNum) {
        for (ControlFrameType controlFrameType : ControlFrameType.values()) {
            if (controlFrameType.tagNum == tagNum) {
                return controlFrameType;
            }
        }

        return null;
    }

    public int getTagNum() {
        return tagNum;
    }

    public ControlFrame getInstance() {
        if (clazz == null) {
            return null;
        }

        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception ex) {
            return null;
        }
    }
}
