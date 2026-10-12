package com.vku.netsend.network;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

/**
 * Lớp xử lý truyền thông mạng Multicast (Lab 6: Lập trình Multicast)
 * Sử dụng MulticastSocket và DatagramPacket trên dải IP Class D (224.0.0.0 - 239.255.255.255).
 */
public class MulticastService {
    public static final int MULTICAST_PORT = 9877;
    public static final String GLOBAL_MULTICAST_IP = "230.0.0.1";

    private MulticastSocket multicastSocket;
    private InetAddress globalGroup;
    private InetAddress currentRoomGroup;
    private String currentRoomName = null;
    private boolean isRunning = false;
    private Thread listenThread;
    private Consumer<String> messageConsumer;
    private String localUsername;
    private String localIP;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    public MulticastService() {
    }

    /**
     * Khởi động dịch vụ Multicast cho Client
     */
    @SuppressWarnings("deprecation")
    public void start(String localIP, String username, Consumer<String> messageConsumer) {
        this.localIP = localIP;
        this.localUsername = username;
        this.messageConsumer = messageConsumer;

        try {
            // Cho phép nhiều tiến trình trên cùng 1 máy bind chung cổng 9877
            multicastSocket = new MulticastSocket(MULTICAST_PORT);
            multicastSocket.setReuseAddress(true);

            // Tham gia nhóm Multicast toàn mạng (Tất cả - *)
            globalGroup = InetAddress.getByName(GLOBAL_MULTICAST_IP);
            multicastSocket.joinGroup(globalGroup);

            isRunning = true;
            listenThread = new Thread(this::listenLoop, "Multicast-Listen-Thread");
            listenThread.setDaemon(true);
            listenThread.start();

            if (messageConsumer != null) {
                messageConsumer.accept("[Hệ thống Multicast]: Đã khởi động Multicast tại cổng " + MULTICAST_PORT 
                        + ", tham gia nhóm Tất cả (*) tại IP: " + GLOBAL_MULTICAST_IP + "\n");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo MulticastSocket: " + e.getMessage());
            if (messageConsumer != null) {
                messageConsumer.accept("[Hệ thống Multicast]: Không thể mở cổng Multicast " + MULTICAST_PORT + ": " + e.getMessage() + "\n");
            }
        }
    }

    /**
     * Tính toán địa chỉ IP Multicast (Class D) dựa trên tên nhóm
     * Ví dụ: "A101" -> "230.0.1.X"
     */
    public static String getGroupMulticastIP(String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            return GLOBAL_MULTICAST_IP;
        }
        int hash = Math.abs(groupName.trim().toLowerCase().hashCode());
        int lastOctet = (hash % 250) + 1; // 1 -> 250
        return "230.0.1." + lastOctet;
    }

    /**
     * Tham gia vào một nhóm Multicast (Group)
     */
    @SuppressWarnings("deprecation")
    public synchronized void joinRoom(String roomName) {
        if (multicastSocket == null || multicastSocket.isClosed() || roomName == null || roomName.trim().isEmpty()) {
            return;
        }
        try {
            // Rời nhóm cũ nếu đã tham gia
            if (currentRoomGroup != null) {
                multicastSocket.leaveGroup(currentRoomGroup);
                currentRoomGroup = null;
                currentRoomName = null;
            }

            // Tham gia nhóm mới
            String groupIP = getGroupMulticastIP(roomName);
            currentRoomGroup = InetAddress.getByName(groupIP);
            currentRoomName = roomName;
            multicastSocket.joinGroup(currentRoomGroup);

            if (messageConsumer != null) {
                messageConsumer.accept("[Hệ thống Multicast]: Đã tham gia Multicast phòng [" + roomName 
                        + "] tại địa chỉ IP: " + groupIP + "\n");
            }
        } catch (Exception e) {
            System.err.println("Lỗi tham gia nhóm Multicast: " + e.getMessage());
        }
    }

    /**
     * Gửi tin nhắn Multicast đến Toàn mạng (*)
     */
    public void sendToAll(String message) {
        sendPacket(GLOBAL_MULTICAST_IP, "ALL", "*", message);
    }

    /**
     * Gửi tin nhắn Multicast đến một Nhóm (Group)
     */
    public void sendToGroup(String roomName, String message) {
        String groupIP = getGroupMulticastIP(roomName);
        sendPacket(groupIP, "GROUP", roomName, message);
    }

    /**
     * Gửi tin nhắn Unicast trực tiếp đến 1 địa chỉ IP (Cá nhân)
     */
    public void sendToIP(String targetIP, String message) {
        sendPacket(targetIP, "UNICAST", targetIP, message);
    }

    /**
     * Đóng gói và phát DatagramPacket
     */
    private void sendPacket(String destinationIP, String type, String target, String message) {
        if (multicastSocket == null || multicastSocket.isClosed()) {
            return;
        }
        new Thread(() -> {
            try {
                // Định dạng gói tin: MULTICAST::<TYPE>::<TARGET>::<SENDER_NAME>::<SENDER_IP>::<TIME>::<MESSAGE>
                String timeStr = dateFormat.format(new Date());
                String payload = String.format("MC_PKT::%s::%s::%s::%s::%s::%s", 
                        type, target, localUsername, localIP, timeStr, message);

                byte[] data = payload.getBytes(StandardCharsets.UTF_8);
                InetAddress destAddress = InetAddress.getByName(destinationIP);
                DatagramPacket packet = new DatagramPacket(data, data.length, destAddress, MULTICAST_PORT);

                multicastSocket.send(packet);
            } catch (Exception e) {
                System.err.println("Lỗi gửi gói tin Multicast tới " + destinationIP + ": " + e.getMessage());
            }
        }).start();
    }

    /**
     * Luồng lắng nghe các gói tin DatagramPacket từ MulticastSocket
     */
    private void listenLoop() {
        byte[] buffer = new byte[4096];
        while (isRunning && multicastSocket != null && !multicastSocket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);

                String senderPhysicalIP = packet.getAddress().getHostAddress();
                String rawData = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);

                if (rawData.startsWith("MC_PKT::")) {
                    String[] parts = rawData.split("::", 7);
                    if (parts.length >= 7) {
                        String type = parts[1];
                        String target = parts[2];
                        String senderName = parts[3];
                        String senderVirtualIP = parts[4];
                        String timeStr = parts[5];
                        String message = parts[6];

                        // Dùng IP nhận được từ gói tin nếu có
                        String displayIP = (senderPhysicalIP != null && !senderPhysicalIP.equals("127.0.0.1")) 
                                ? senderPhysicalIP : senderVirtualIP;

                        String formattedMsg;
                        if ("ALL".equals(type)) {
                            formattedMsg = String.format("[Tất cả] [%s] [Từ IP: %s - Tên: %s]: %s", 
                                    timeStr, displayIP, senderName, message);
                        } else if ("GROUP".equals(type)) {
                            // Chỉ hiển thị nếu máy mình đang ở trong nhóm đó
                            if (currentRoomName != null && currentRoomName.equalsIgnoreCase(target)) {
                                formattedMsg = String.format("[Nhóm %s] [%s] [Từ IP: %s - Tên: %s]: %s", 
                                        target, timeStr, displayIP, senderName, message);
                            } else {
                                continue;
                            }
                        } else {
                            formattedMsg = String.format("[Cá nhân] [%s] [Từ IP: %s - Tên: %s]: %s", 
                                    timeStr, displayIP, senderName, message);
                        }

                        if (messageConsumer != null) {
                            messageConsumer.accept(formattedMsg + "\n");
                        }
                    }
                }
            } catch (SocketException se) {
                // Socket closed khi logout
                break;
            } catch (Exception e) {
                if (isRunning) {
                    System.err.println("Lỗi nhận Multicast: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Dừng và đóng kết nối Multicast
     */
    @SuppressWarnings("deprecation")
    public void stop() {
        isRunning = false;
        if (multicastSocket != null && !multicastSocket.isClosed()) {
            try {
                if (globalGroup != null) {
                    multicastSocket.leaveGroup(globalGroup);
                }
                if (currentRoomGroup != null) {
                    multicastSocket.leaveGroup(currentRoomGroup);
                }
            } catch (Exception ignored) {
            }
            multicastSocket.close();
        }
    }

    public String getCurrentRoomName() {
        return currentRoomName;
    }
}
