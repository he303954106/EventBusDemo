package com.hk.eventbusdemo;

/**
 * Created by hk on 2019/5/18.
 */
public class EventBean {

    private String one;
    private String two;

    public EventBean(String one, String two) {
        this.one = one;
        this.two = two;
    }

    @Override
    public String toString() {
        return "EventBean{" +
                "one='" + one + '\'' +
                ", two='" + two + '\'' +
                '}';
    }
}
