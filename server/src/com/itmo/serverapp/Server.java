package com.itmo.serverapp;

import com.itmo.common.Connection;
import com.itmo.common.SimpleMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedTransferQueue;

public class Server {
    private int port;
    private LinkedTransferQueue<SimpleMessage> messages;
    private CopyOnWriteArraySet<Connection> connections;

    public Server(int port) {
        this.port = port;
        this.messages = new LinkedTransferQueue<>();
        this.connections = new CopyOnWriteArraySet<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен");
            new Thread(new SenderMessage()).start();
            while (true) {
                Socket newClient = serverSocket.accept();
                new Thread(new ReceiverMessage(newClient)).start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public class ReceiverMessage implements Runnable {
        private Socket socket;
        private Connection connection;

        public ReceiverMessage(Socket socket) throws IOException {
            this.socket = socket;
            this.connection = new Connection(socket);
        }

        @Override
        public void run() {

            while (true) {
                try {
                    SimpleMessage message = connection.readMessage();
                    if (message.getText().equalsIgnoreCase("exit")) {
                        connections.remove(connection);
                        Thread.currentThread().interrupt();
                    }
                    messages.put(message);
                    connections.add(connection);
                    messages.transfer(message);
                    System.out.println(Thread.currentThread() + " client name:" + message.getSender()+" text:"+message.getText());

                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    System.out.println(e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public class SenderMessage implements Runnable {

        @Override
        public void run() {
            System.out.println("ok");
            while (true) {
                try {
                    SimpleMessage message = messages.take();
                    for (Connection client : connections) {
                        if (!client.getNameSender().equals(message.getSender())) {
                            try {
                                client.sendMessage(message);
                            } catch (IOException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}