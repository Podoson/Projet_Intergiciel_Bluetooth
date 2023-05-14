package com.example.bluetoothapp.Models;

import java.util.Date;

public class Message {
    private int id;

    private String sender;

    private String message;

    private Date createdAt;

    public Message() {
    }

    public Message(int id, String sender, String message, Date createdAt) {
        this.id = id;
        this.sender = sender;
        this.message = message;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
