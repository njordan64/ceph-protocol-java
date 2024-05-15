/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.client.codecs;

import ca.venom.ceph.protocol.frames.ControlFrame;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;

public class RequestWithFuture {
    @Getter
    private final ControlFrame request;
    @Getter
    private final CompletableFuture<ControlFrame> responseFuture;

    public RequestWithFuture(ControlFrame request, CompletableFuture<ControlFrame> responseFuture) {
        this.request = request;
        this.responseFuture = responseFuture;
    }
}
