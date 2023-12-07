package ca.venom.ceph.protocol.types.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CephField {
    int order() default 1;
    ByteOrderPreference byteOrderPreference() default ByteOrderPreference.NONE;
    boolean includeSize() default false;

    int sizeLength() default 4;
}
