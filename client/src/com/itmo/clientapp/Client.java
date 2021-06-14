package com.itmo.clientapp;

import com.itmo.common.Connection;
import com.itmo.common.SimpleMessage;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;


public class Client {
    private String ip;
    private int port;
    private Scanner scanner;
    private Connection connection;


    public Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.scanner = new Scanner(System.in);
        try {
            this.connection = new Connection(new Socket(ip, port));
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new ClientSender(connection)).start();
        new Thread(new ClientReader(connection)).start();

    }

    class ClientReader implements Runnable {
        private Connection connection;


        public ClientReader(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    SimpleMessage fromServer = connection.readMessage();
                    System.out.println("от клиента " + fromServer.getSender() +  ": " + fromServer.getText());
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    class ClientSender implements Runnable {
        private Connection connection;


        public ClientSender(Connection connection) {
            this.connection = connection;
        }


        @Override
        public void run() {
            try {
                System.out.println("Введите имя");
                String userName = scanner.nextLine();
                String text;



                while (true) {

                    System.out.println("Введите сообщение");
                    text = scanner.nextLine();
                    if ("exit".equals(text)) break;
                    connection.sendMessage(SimpleMessage.getMessage(userName, text));
                }

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}