module todo.ui {
    exports todo.ui;
    requires transitive todo.dtos;
    requires kotlin.stdlib;
    requires javafx.graphics;
    requires javafx.controls;
    requires kotlinx.serialization.core;
    requires kotlin.stdlib.jdk8;
    requires io.ktor.client.core;
    requires io.ktor.client.cio;
    requires io.ktor.client.content.negotiation;
    requires io.ktor.serialization.kotlinx.json;
    requires kotlinx.serialization.json;
    requires io.ktor.http;
}