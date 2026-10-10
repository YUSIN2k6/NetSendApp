package com.vku.netsend.view;

import com.vku.netsend.controller.ServerController;

import javax.swing.*;
import java.awt.*;

public class ServerGUI extends JFrame {
    private JTextArea logArea;
    private ServerController controller;

    public ServerGUI() {
        setTitle("Net Send TCP Server (MVC & JSON)");
        setSize(750, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 14));

        add(new JLabel(" Nhật ký hệ thống TCP Net Send:"), BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Khởi tạo Controller và bắt đầu chạy Server
        controller = new ServerController(this);
        controller.start();
    }

    // Hàm để ServerController gọi khi muốn in log ra màn hình
    public void appendLog(String log) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(log);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

}