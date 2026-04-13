/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.encoding.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(CephFields.class)
public @interface CephField {
    int order() default 1;

    byte minVersion() default (byte) -1;

    byte maxVersion() default (byte) -1;

    ByteOrderPreference byteOrderPreference() default ByteOrderPreference.NONE;

    boolean includeSize() default false;

    int sizeLength() default 4;

    String sizeProperty() default "";
}
