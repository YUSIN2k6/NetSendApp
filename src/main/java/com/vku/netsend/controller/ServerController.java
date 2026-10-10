package com.vku.netsend.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vku.netsend.model.ClientInfo;
import com.vku.netsend.network.TCPConnection;
import com.vku.netsend.view.ServerGUI;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerController {
    private ServerGUI view;
    private ServerSocket serverSocket;
    private boolean isRunning = true;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Map<String, ClientInfo> allAccounts = new ConcurrentHashMap<>();
    private Map<String, ClientInfo> activeClients = new ConcurrentHashMap<>();
    private java.util.Set<String> chatRooms = ConcurrentHashMap.newKeySet();

    public ServerController(ServerGUI view) {
        this.view = view;
        loadAllDataFromJson();
        loadRoomsFromJson();
    }

    public void start() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(9876);
                log("Server TCP (JSON Storage) đã sẵn sàng tại Port 9876...");
                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    TCPConnection conn = new TCPConnection(clientSocket);
                    new Thread(() -> handleClient(conn)).start();
                }
            } catch (Exception e) {
                log("Server đóng kết nối: " + e.getMessage());
            }
        }).start();
    }

    private void broadcastOnlineList() {
        StringBuilder list = new StringBuilder("ONLINE_LIST::");
        for (ClientInfo user : activeClients.values()) {
            list.append(user.getUsername()).append(" - IP: ").append(user.getIpClient()).append(",");
        }
        for (ClientInfo user : activeClients.values()) {
            try {
                user.getConnection().send(list.toString());
            } catch (Exception e) {
            }
        }
    }

    private void broadcastRoomList() {
        if (chatRooms.isEmpty())
            return;
        String roomData = "ROOM_LIST::" + String.join(",", chatRooms);
        for (ClientInfo user : activeClients.values()) {
            try {
                user.getConnection().send(roomData);
            } catch (Exception e) {
            }
        }
    }

    private void handleClient(TCPConnection conn) {
        String currentUsername = null;
        String clientIP = conn.getRemoteIP();

        try {
            while (!conn.isClosed()) {
                String request = conn.receive();
                if (request == null || request.isEmpty())
                    break;

                String[] parts = request.split("::", 5); // Tách thành 5 phần thay vì 6
                String cmd = parts[0];

                if (cmd.equals("PING"))
                    continue;

                if (cmd.equals("REGISTER")) {
                    String username = parts[1];
                    String email = parts[2];
                    String pass = parts[3];

                    if (allAccounts.containsKey(username)) {
                        conn.send("SYS::Tên đăng nhập này đã tồn tại!");
                    } else {
                        // Bỏ DisplayName, Username dùng cho cả hai
                        ClientInfo newUser = new ClientInfo(username, email, pass, clientIP, null);
                        allAccounts.put(username, newUser);
                        saveAccountToJson(newUser);
                        conn.send("SYS::Đăng ký thành công!");
                        log("Tài khoản mới: " + username + " (Email: " + email + ")");
                    }
                } else if (cmd.equals("LOGIN")) {
                    String username = parts[1];
                    String pass = parts[2];

                    ClientInfo user = allAccounts.get(username);
                    if (user == null || !user.getPassword().equals(pass)) {
                        conn.send("SYS::Sai Tên đăng nhập hoặc Mật khẩu!");
                    } else {
                        currentUsername = username;
                        user.setConnection(conn);
                        user.setTimeDisconnect(0);
                        user.setIpClient(clientIP);
                        user.setGroup(null);
                        activeClients.put(username, user);

                        log(user.getUsername() + " (" + clientIP + ") đã đăng nhập.");
                        conn.send("SYS::Đăng nhập thành công::" + user.getUsername());
                        broadcastOnlineList();
                        if (!chatRooms.isEmpty()) {
                            conn.send("ROOM_LIST::" + String.join(",", chatRooms));
                        }

                        if (user.getMessages() != null && !user.getMessages().isEmpty()) {
                            StringBuilder history = new StringBuilder("HISTORY::\n--- Lịch sử tin nhắn cá nhân ---\n");
                            for (String msg : user.getMessages())
                                history.append(msg).append("\n");
                            conn.send(history.toString());
                        }

                        java.util.List<String> allHist = loadHistory("global_all");
                        if (!allHist.isEmpty()) {
                            StringBuilder hist = new StringBuilder("HISTORY::\n--- Lịch sử Tất cả ---\n");
                            for (String m : allHist)
                                hist.append(m).append("\n");
                            conn.send(hist.toString());
                        }
                    }
                } else if (cmd.equals("LOGOUT")) {
                    if (currentUsername != null) {
                        ClientInfo user = activeClients.remove(currentUsername);
                        broadcastOnlineList();
                        if (user != null) {
                            user.setTimeDisconnect(System.currentTimeMillis());
                            user.setConnection(null);
                            saveAccountToJson(user);
                        }
                        log(currentUsername + " đã đăng xuất.");
                        currentUsername = null;
                    }
                } else if (cmd.equals("CREATE_ROOM")) {
                    String roomName = parts[1];
                    if (chatRooms.contains(roomName)) {
                        conn.send("SYS::Phòng đã tồn tại!");
                    } else {
                        chatRooms.add(roomName);
                        saveRoomsToJson();
                        broadcastRoomList();
                        conn.send("SYS::Đã tạo phòng thành công: " + roomName);
                        log("Phòng mới được tạo: " + roomName);
                    }
                } else if (cmd.equals("JOIN_GROUP")) {
                    String username = parts[1];
                    String groupName = parts[2];
                    if (activeClients.containsKey(username)) {
                        activeClients.get(username).setGroup(groupName);
                        conn.send("SYS::Bạn đã tham gia nhóm: " + groupName);
                        log(username + " tham gia nhóm [" + groupName + "]");

                        java.util.List<String> gHist = loadHistory("group_" + groupName);
                        if (!gHist.isEmpty()) {
                            StringBuilder hist = new StringBuilder(
                                    "HISTORY::\n--- Lịch sử Nhóm " + groupName + " ---\n");
                            for (String m : gHist)
                                hist.append(m).append("\n");
                            conn.send(hist.toString());
                        }
                    }
                } else if (cmd.equals("NET_SEND")) {
                    String senderUsername = parts[1];
                    String targetType = parts[2];
                    String targetValue = parts[3];
                    String message = parts[4];

                    String senderName = allAccounts.get(senderUsername).getUsername();
                    String timeStr = dateFormat.format(new Date());

                    if (targetType.equals("ALL")) {
                        String formattedMsg = String.format("[Tất cả] [%s] [Từ IP: %s - Tên: %s]: %s", timeStr,
                                clientIP, senderName, message);
                        log(senderName + " đã gửi Broadcast.");
                        saveHistory("global_all", formattedMsg);
                        for (String room : chatRooms) {
                            saveHistory("group_" + room, formattedMsg);
                        }
                        for (ClientInfo user : activeClients.values()) {
                            user.getConnection().send("MSG::\n" + formattedMsg);
                        }
                    } else if (targetType.equals("GROUP")) {
                        ClientInfo senderInfo = activeClients.get(senderUsername);
                        if (senderInfo.getGroup() == null || !senderInfo.getGroup().equalsIgnoreCase(targetValue)) {
                            conn.send("SYS::Cảnh báo: Bạn phải tham gia phòng " + targetValue
                                    + " trước khi gửi tin nhắn!");
                            continue;
                        }
                        String formattedMsg = String.format("[Nhóm %s] [%s] [Từ IP: %s - Tên: %s]: %s", targetValue,
                                timeStr, clientIP, senderName, message);
                        saveHistory("group_" + targetValue, formattedMsg);
                        for (ClientInfo user : activeClients.values()) {
                            if (user.getGroup() != null && user.getGroup().equalsIgnoreCase(targetValue)) {
                                user.getConnection().send("MSG::\n" + formattedMsg);
                            }
                        }
                    } else if (targetType.equals("IP_NAME")) {
                        String formattedMsg = String.format("[Cá nhân] [%s] [Từ IP: %s - Tên: %s]: %s", timeStr,
                                clientIP, senderName, message);
                        boolean isSent = false;
                        boolean senderReceived = false;
                        for (ClientInfo user : allAccounts.values()) {
                            boolean matchName = user.getUsername().equalsIgnoreCase(targetValue);
                            boolean matchIP = activeClients.containsKey(user.getUsername()) && activeClients
                                    .get(user.getUsername()).getConnection().getRemoteIP().equals(targetValue);

                            if (matchName || matchIP) {
                                user.addMessage(formattedMsg);
                                saveAccountToJson(user);
                                if (activeClients.containsKey(user.getUsername())) {
                                    activeClients.get(user.getUsername()).getConnection()
                                            .send("MSG::\n" + formattedMsg);
                                }
                                if (user.getUsername().equals(senderUsername)) {
                                    senderReceived = true;
                                }
                                isSent = true;
                            }
                        }
                        if (isSent) {
                            if (!senderReceived) {
                                ClientInfo sender = allAccounts.get(senderUsername);
                                sender.addMessage(formattedMsg);
                                saveAccountToJson(sender);
                                conn.send("MSG::\n" + formattedMsg);
                            }
                        } else {
                            conn.send("SYS::Lỗi: Không tìm thấy IP hoặc Tài khoản '" + targetValue + "'");
                        }
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (currentUsername != null) {
                ClientInfo user = activeClients.remove(currentUsername);
                broadcastOnlineList();
                if (user != null) {
                    user.setTimeDisconnect(System.currentTimeMillis());
                    saveAccountToJson(user);
                }
                log(currentUsername + " ngắt kết nối đột ngột.");
            }
            conn.close();
        }
    }

    private String extractPrefixFromEmail(String email) {
        if (email.contains("@"))
            return email.substring(0, email.indexOf("@"));
        return email;
    }

    private void loadAllDataFromJson() {
        try {
            File storageDir = new File("storage");
            if (!storageDir.exists())
                storageDir.mkdirs();
            File[] userDirs = storageDir.listFiles(File::isDirectory);
            if (userDirs != null) {
                for (File dir : userDirs) {
                    File jsonFile = new File(dir, dir.getName() + ".json");
                    if (jsonFile.exists()) {
                        Reader reader = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8);
                        ClientInfo info = gson.fromJson(reader, ClientInfo.class);
                        allAccounts.put(info.getUsername(), info);
                        reader.close();
                    }
                }
            }
            log("Đã nạp " + allAccounts.size() + " tài khoản từ JSON.");
        } catch (Exception e) {
            log("Lỗi nạp JSON: " + e.getMessage());
        }
    }

    private synchronized void saveAccountToJson(ClientInfo info) {
        try {
            String prefix = extractPrefixFromEmail(info.getEmail());
            File userDir = new File("storage", prefix);
            if (!userDir.exists())
                userDir.mkdirs();

            File jsonFile = new File(userDir, prefix + ".json");
            Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8);
            gson.toJson(info, writer);
            writer.close();
        } catch (Exception e) {
            log("Lỗi ghi JSON: " + e.getMessage());
        }
    }

    private void loadRoomsFromJson() {
        try {
            File storageDir = new File("storage");
            if (!storageDir.exists())
                storageDir.mkdirs();
            File roomFile = new File(storageDir, "rooms.json");
            if (roomFile.exists()) {
                Reader reader = new InputStreamReader(new FileInputStream(roomFile), StandardCharsets.UTF_8);
                java.lang.reflect.Type setType = new com.google.gson.reflect.TypeToken<java.util.Set<String>>() {
                }.getType();
                java.util.Set<String> loaded = gson.fromJson(reader, setType);
                if (loaded != null) {
                    chatRooms.addAll(loaded);
                }
                reader.close();
            }
            log("Đã nạp " + chatRooms.size() + " phòng chat.");
        } catch (Exception e) {
            log("Lỗi nạp rooms.json: " + e.getMessage());
        }
    }

    private synchronized void saveRoomsToJson() {
        try {
            File storageDir = new File("storage");
            if (!storageDir.exists())
                storageDir.mkdirs();
            File roomFile = new File(storageDir, "rooms.json");
            Writer writer = new OutputStreamWriter(new FileOutputStream(roomFile), StandardCharsets.UTF_8);
            gson.toJson(chatRooms, writer);
            writer.close();
        } catch (Exception e) {
            log("Lỗi ghi rooms.json: " + e.getMessage());
        }
    }

    private synchronized void saveHistory(String fileName, String msg) {
        try {
            File storageDir = new File("storage", "history");
            if (!storageDir.exists())
                storageDir.mkdirs();
            File histFile = new File(storageDir, fileName + ".json");

            java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<java.util.List<String>>() {
            }.getType();
            java.util.List<String> history = new java.util.ArrayList<>();
            if (histFile.exists()) {
                Reader reader = new InputStreamReader(new FileInputStream(histFile), StandardCharsets.UTF_8);
                java.util.List<String> loaded = gson.fromJson(reader, listType);
                if (loaded != null)
                    history.addAll(loaded);
                reader.close();
            }
            history.add(msg);
            Writer writer = new OutputStreamWriter(new FileOutputStream(histFile), StandardCharsets.UTF_8);
            gson.toJson(history, writer);
            writer.close();
        } catch (Exception e) {
            log("Lỗi ghi history: " + e.getMessage());
        }
    }

    private java.util.List<String> loadHistory(String fileName) {
        try {
            File histFile = new File("storage/history", fileName + ".json");
            if (histFile.exists()) {
                Reader reader = new InputStreamReader(new FileInputStream(histFile), StandardCharsets.UTF_8);
                java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<java.util.List<String>>() {
                }.getType();
                java.util.List<String> history = gson.fromJson(reader, listType);
                reader.close();
                return history == null ? new java.util.ArrayList<>() : history;
            }
        } catch (Exception e) {
        }
        return new java.util.ArrayList<>();
    }

    private void log(String msg) {
        if (view != null)
            view.appendLog(dateFormat.format(new Date()) + " | " + msg + "\n");
    }
}