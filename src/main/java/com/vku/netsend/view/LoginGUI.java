package com.vku.netsend.view;

import com.vku.netsend.controller.ClientController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginGUI extends JFrame {
    private JTextField txtIP, txtUsername, txtPassword;
    private JButton btnLogin;
    private JLabel lblRegister;
    private ClientController controller;

    public LoginGUI(ClientController controller) {
        this.controller = controller;
        setTitle("Net Send - Đăng Nhập");
        setSize(400, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 247, 250));
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(245, 247, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(41, 128, 185));
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 30, 0);
        mainPanel.add(lblTitle, gbc);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 15);

        gbc.insets = new Insets(0, 30, 2, 30);
        JLabel lblIP = new JLabel("IP Server:");
        lblIP.setFont(labelFont);
        lblIP.setForeground(Color.GRAY);
        gbc.gridy = 1;
        mainPanel.add(lblIP, gbc);
        txtIP = createTextField(inputFont, "localhost");
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 30, 15, 30);
        mainPanel.add(txtIP, gbc);

        gbc.insets = new Insets(0, 30, 2, 30);
        JLabel lblUsername = new JLabel("Tên đăng nhập (ID):");
        lblUsername.setFont(labelFont);
        lblUsername.setForeground(Color.GRAY);
        gbc.gridy = 3;
        mainPanel.add(lblUsername, gbc);
        txtUsername = createTextField(inputFont, "");
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 30, 15, 30);
        mainPanel.add(txtUsername, gbc);

        gbc.insets = new Insets(0, 30, 2, 30);
        JLabel lblPassword = new JLabel("Mật khẩu:");
        lblPassword.setFont(labelFont);
        lblPassword.setForeground(Color.GRAY);
        gbc.gridy = 5;
        mainPanel.add(lblPassword, gbc);
        txtPassword = createTextField(inputFont, ""); // Hiện rõ mật khẩu
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 30, 30, 30);
        mainPanel.add(txtPassword, gbc);

        btnLogin = new JButton("VÀO HỆ THỐNG");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(300, 45));
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setOpaque(true);
        btnLogin.setContentAreaFilled(true);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 30, 15, 30);
        mainPanel.add(btnLogin, gbc);

        lblRegister = new JLabel("Chưa có tài khoản? Đăng ký ngay", SwingConstants.CENTER);
        lblRegister.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRegister.setForeground(new Color(41, 128, 185));
        lblRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 8;
        mainPanel.add(lblRegister, gbc);

        add(mainPanel, BorderLayout.CENTER);

        btnLogin.addActionListener(e -> {
            String ip = txtIP.getText().trim();
            String username = txtUsername.getText().trim();
            String pass = txtPassword.getText().trim();
            if (ip.isEmpty() || username.isEmpty() || pass.isEmpty()) {
                showErrorMessage("Vui lòng nhập đủ IP, ID và Mật khẩu!");
                return;
            }
            controller.attemptLogin(ip, username, pass, this);
        });

        lblRegister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new RegisterGUI(LoginGUI.this, controller, txtIP.getText().trim()).setVisible(true);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lblRegister.setText("<html><u>Chưa có tài khoản? Đăng ký ngay</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lblRegister.setText("Chưa có tài khoản? Đăng ký ngay");
            }
        });
    }

    private JTextField createTextField(Font font, String text) {
        JTextField tf = new JTextField(text);
        tf.setFont(font);
        tf.setPreferredSize(new Dimension(300, 40));
        tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(0, 10, 0, 10)));
        return tf;
    }

    public void setLoadingState(boolean isLoading) {
        btnLogin.setEnabled(!isLoading);
        btnLogin.setText(isLoading ? "ĐANG KẾT NỐI..." : "VÀO HỆ THỐNG");
    }

    public void showErrorMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
    }
}