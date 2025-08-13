package com.example.demo;

import java.util.List;

public class ConsumerOfFoo {

    private final List<Foo> foo;

    public ConsumerOfFoo(List<Foo> foo) {
        this.foo = foo;
    }

    public String sayHello() {
        return String.join(",", foo.stream().map(Foo::sayHello).toList());
    }
}
