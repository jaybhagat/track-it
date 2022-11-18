module todo.app {
    requires transitive todo.dtos;
    requires kotlin.stdlib;
    requires javafx.graphics;
    requires javafx.controls;
    requires kotlinx.coroutines.android;
    requires kotlin.reflect;
    requires kotlin.stdlib.jdk8;
    requires java.sql;
    requires spring.boot.autoconfigure;
    requires spring.web;
    requires spring.boot;
    exports todo.app;
}