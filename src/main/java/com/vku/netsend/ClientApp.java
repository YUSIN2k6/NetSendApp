package com.vku.netsend;

import com.vku.netsend.controller.ClientController;
import com.vku.netsend.view.LoginGUI;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ClientApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            ClientController controller = new ClientController();
            LoginGUI login = new LoginGUI(controller);
            login.setVisible(true);
        });
    }
}