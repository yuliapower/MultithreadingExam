package com.itmo.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection implements AutoCloseable {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String nameSender;


    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        output = new ObjectOutputStream(socket.getOutputStream());
        input = new ObjectInputStream(socket.getInputStream());
    }

    public String getNameSender() {
        return nameSender;
    }

    public void sendMessage(SimpleMessage message) throws IOException {
        message.setDateTime();
        output.writeObject(message);
        output.flush();
    }

    public SimpleMessage readMessage() throws IOException, ClassNotFoundException {
       SimpleMessage message = (SimpleMessage) input.readObject();
       nameSender = message.getSender();
        return message;
    }

    @Override
    public String toString() {
        return "Connection{" +
                "socket=" + socket +
                ", input=" + input +
                ", output=" + output +
                '}';
    }

    @Override
    public void close() throws Exception {
        input.close();
        output.close();
        socket.close();
    }
}