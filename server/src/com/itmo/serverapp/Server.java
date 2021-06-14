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
    private CopyOnWriteArraySet<Connection> clients;


    public Server(int port) {
        this.port = port;
        this.messages = new LinkedTransferQueue<>();
        this.clients = new CopyOnWriteArraySet<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен");

            new Thread(new SenderMessage()).start();
            while (true) {
                Socket newClient = serverSocket.accept();
                new Thread(new ThreadClientHandler(newClient)).start();

            }
        } catch (IOException e) {
            System.out.println(e.getMessage());

        }
    }


    public class ThreadClientHandler implements Runnable {
        private Socket socket;
        private Connection connection;


        public ThreadClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.connection = new Connection(socket);

        }


        @Override
        public void run() {
            try {
                SimpleMessage message = connection.readMessage();

                messages.put(message);//добавления пепрвого сообщения в блокирующую очередь
                clients.add(connection);//добавленяи подключений
                messages.transfer(message);
                System.out.println(clients);

                while (true) {
                    SimpleMessage message1 = connection.readMessage();
                    messages.put(message1);//добавления сообщений в блокирующую очередь
                    messages.transfer(message1);
                }
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                System.out.println(e.getMessage());
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
                    for (Connection cl : clients) {
                        if (!cl.getNameSender().equals(message.getSender())) {
                            try {
                                cl.sendMessage(SimpleMessage.getMessage(message.getSender(), message.getText()));

                            } catch (IOException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }
    }
}