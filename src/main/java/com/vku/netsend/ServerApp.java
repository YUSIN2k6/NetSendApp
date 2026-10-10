package com.vku.netsend;

import com.vku.netsend.view.ServerGUI;
import javax.swing.SwingUtilities;

public class ServerApp {
    public static void main(String[] args) {
        // Chỉ làm một nhiệm vụ duy nhất là gọi Giao diện Server MVC lên
        SwingUtilities.invokeLater(() -> new ServerGUI().setVisible(true));
    }
}