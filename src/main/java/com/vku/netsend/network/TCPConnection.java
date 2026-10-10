package com.vku.netsend.network;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class TCPConnection {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    /**
     * Dùng cho CLIENT: Khởi tạo kết nối chủ động đến Server
     */
    public TCPConnection(String ip, int port) throws IOException {
        this.socket = new Socket(ip, port);
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
    }

    /**
     * Dùng cho SERVER: Bọc lại Socket của Client sau khi Server gọi lệnh accept()
     */
    public TCPConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
    }

    /**
     * Hàm gửi tin nhắn qua ống TCP
     * Gửi độ dài chuỗi trước, sau đó mới gửi nội dung để chống dính gói tin (TCP
     * Sticky)
     */
    public void send(String data) throws IOException {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length); // Gửi kích thước (4 byte)
        out.write(bytes); // Gửi nội dung
        out.flush(); // Đẩy nước đi ngay lập tức
    }

    /**
     * Hàm nhận tin nhắn từ ống TCP (Hàm này sẽ đứng chờ/block cho đến khi có tin
     * đến)
     */
    public String receive() throws IOException {
        // Đọc 4 byte đầu tiên để biết tin nhắn sắp tới dài bao nhiêu
        int length = in.readInt();
        if (length > 0) {
            byte[] bytes = new byte[length];
            in.readFully(bytes, 0, length); // Hứng đúng số lượng byte đó
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return "";
    }

    /**
     * Lấy IP thực của đầu dây bên kia
     */
    public String getRemoteIP() {
        if (socket != null && socket.getInetAddress() != null) {
            return socket.getInetAddress().getHostAddress();
        }
        return "Unknown";
    }

    /**
     * Lấy Port của đầu dây bên kia
     */
    public int getRemotePort() {
        if (socket != null) {
            return socket.getPort();
        }
        return -1;
    }

    /**
     * Kiểm tra xem đường ống đã bị đóng chưa
     */
    public boolean isClosed() {
        return socket == null || socket.isClosed();
    }

    /**
     * Đóng hoàn toàn kết nối và các luồng đọc/ghi
     */
    public void close() {
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            System.err.println("Lỗi đóng TCP Connection: " + e.getMessage());
        }
    }
}