package com.vku.netsend.model;

import com.vku.netsend.network.TCPConnection;
import java.util.ArrayList;
import java.util.List;

public class ClientInfo {
    private String username;
    private String email;
    private String password;
    private String ipClient;
    private String group;
    private long timeConnect;
    private long timeDisconnect;
    private List<String> messages;

    private transient TCPConnection connection;

    public ClientInfo(String username, String email, String password, String ipClient, TCPConnection connection) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.ipClient = ipClient;
        this.connection = connection;
        this.group = "";
        this.timeConnect = System.currentTimeMillis();
        this.timeDisconnect = 0;
        this.messages = new ArrayList<>();
    }

    // --- GETTER & SETTER ---
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIpClient() {
        return ipClient;
    }

    public void setIpClient(String ipClient) {
        this.ipClient = ipClient;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public long getTimeConnect() {
        return timeConnect;
    }

    public long getTimeDisconnect() {
        return timeDisconnect;
    }

    public void setTimeDisconnect(long timeDisconnect) {
        this.timeDisconnect = timeDisconnect;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public void addMessage(String msg) {
        this.messages.add(msg);
    }

    public TCPConnection getConnection() {
        return connection;
    }

    public void setConnection(TCPConnection connection) {
        this.connection = connection;
    }
}