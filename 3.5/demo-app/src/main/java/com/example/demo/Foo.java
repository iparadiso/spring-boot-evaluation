package com.example.demo;

import com.example.library.Bar;

public class Foo {

    private final Bar bar;

    public Foo(Bar bar) {
        this.bar = bar;
    }

    public String sayHello() {
        return "Foo says Hello, " + bar.sayHello();
    }
}
