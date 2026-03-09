package ca.venom.ceph.encoding.annotations;

public @interface CephFieldDecode {
    int order() default 1;
}
