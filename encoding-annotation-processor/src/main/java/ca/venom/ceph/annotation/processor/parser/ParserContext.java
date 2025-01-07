/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.annotation.processor.parser;

import javax.annotation.processing.Messager;

public class ParserContext {
    private Messager messager;

    public ParserContext(Messager messager) {
        this.messager = messager;
    }

    public Messager getMessager() {
        return messager;
    }
}
