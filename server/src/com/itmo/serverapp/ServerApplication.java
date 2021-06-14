package com.itmo.serverapp;

public class ServerApplication {
    public static void main(String[] args) {
        new Server(8080).start();
    }
}