package com.jd.bdp.datadev.domain;

/**
 * Created by zhangrui25 on 2017/7/21.
 */
public class HoldDoubleValue<A, B> {
    public A a;
    public B b;

    public HoldDoubleValue(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }

    @Override
    public String toString() {
        return "HoldDoubleValue{" +
                "a=" + a +
                ", b=" + b +
                '}';
    }
}


