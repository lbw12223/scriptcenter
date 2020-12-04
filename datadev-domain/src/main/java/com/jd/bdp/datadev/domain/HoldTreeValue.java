package com.jd.bdp.datadev.domain;

/**
 * Created by zhangrui25 on 2017/7/21.
 */
public class HoldTreeValue<A, B, C> extends HoldDoubleValue<A, B> {
    public C c;


    public HoldTreeValue(A a, B b, C c) {
        super(a, b);
        this.c = c;
    }

    @Override
    public String toString() {
        return "HoldDoubleValue{" +
                "a=" + a +
                ", b=" + b +
                ", c=" + c +
                '}';
    }
}


