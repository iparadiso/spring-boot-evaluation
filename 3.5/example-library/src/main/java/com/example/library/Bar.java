package com.example.library;

public class Bar {
    
    public String sayHello() {
        return "Bar says " + getGreeting();
    }

    public String getGreeting() {
        return "Hello";
    }
}