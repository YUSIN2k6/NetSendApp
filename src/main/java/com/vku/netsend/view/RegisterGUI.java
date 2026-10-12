package com.vku.netsend.view;

import com.vku.netsend.controller.ClientController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RegisterGUI extends JDialog {
    private JTextField txtIP, txtUsername, txtEmail, txtPassword;
    private JButton btnRegister;
    private JLabel lblCancel;
    private ClientController controller;

    public RegisterGUI(JFrame parent, ClientController controller, String currentIP) {
        super(parent, "Đăng ký tài khoản", true);
        this.controller = controller;
        setSize(400, 500); // Đã giảm chiều cao cho vừa vặn
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 247, 250));
        initUI(currentIP);
    }

    private void initUI(String ip) {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(245, 247, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lblTitle = new JLabel("TẠO TÀI KHOẢN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(39, 174, 96));
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 20, 0);
        mainPanel.add(lblTitle, gbc);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 15);

        gbc.insets = new Insets(0, 30, 2, 30);
        JLabel lblIP = new JLabel("IP Server:");
        lblIP.setFont(labelFont);
        lblIP.setForeground(Color.GRAY);
        gbc.gridy = 1;
        mainPanel.add(lblIP, gbc);
        txtIP = createTextField(inputFont, ip.isEmpty() ? "localhost" : ip);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 30, 10, 30);
        mainPanel.add(txtIP, gbc);

        gbc.insets = new Insets(0, 30, 2, 30);
        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(labelFont);
        lblUser.setForeground(Color.GRAY);
        gbc.gridy = 3;
        mainPanel.add(lblUser, gbc);
        txtUsername = createTextField(inputFont, "");
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 30, 10, 30);
        mainPanel.add(txtUsername, gbc);

        gbc.insets = new Insets(0, 30, 2, 30);
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(labelFont);
        lblEmail.setForeground(Color.GRAY);
        gbc.gridy = 5;
        mainPanel.add(lblEmail, gbc);
        txtEmail = createTextField(inputFont, "");
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 30, 10, 30);
        mainPanel.add(txtEmail, gbc);

        gbc.insets = new Insets(0, 30, 2, 30);
        JLabel lblPassword = new JLabel("Mật khẩu:");
        lblPassword.setFont(labelFont);
        lblPassword.setForeground(Color.GRAY);
        gbc.gridy = 7;
        mainPanel.add(lblPassword, gbc);
        txtPassword = createTextField(inputFont, "");
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 30, 20, 30);
        mainPanel.add(txtPassword, gbc);

        btnRegister = new JButton("XÁC NHẬN ĐĂNG KÝ");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegister.setPreferredSize(new Dimension(300, 45));
        btnRegister.setBackground(new Color(39, 174, 96));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setOpaque(true);
        btnRegister.setContentAreaFilled(true);
        btnRegister.setBorderPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 9;
        gbc.insets = new Insets(0, 30, 15, 30);
        mainPanel.add(btnRegister, gbc);

        lblCancel = new JLabel("← Quay lại đăng nhập", SwingConstants.CENTER);
        lblCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCancel.setForeground(Color.GRAY);
        lblCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 10;
        gbc.insets = new Insets(0, 30, 10, 30);
        mainPanel.add(lblCancel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        btnRegister.addActionListener(e -> {
            String ipServer = txtIP.getText().trim();
            String user = txtUsername.getText().trim();
            String email = txtEmail.getText().trim();
            String pass = txtPassword.getText().trim();

            if (ipServer.isEmpty() || user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                showErrorMessage("Vui lòng nhập đầy đủ thông tin!");
            } else {
                // Kiểm tra điều kiện mật khẩu bằng cấu trúc if () else if () lồng bên trong
                if (pass.length() <= 8) {
                    showErrorMessage("Mật khẩu phải trên 8 ký tự!");
                } else if (!pass.matches(".*[A-Z].*")) {
                    showErrorMessage("Mật khẩu phải chứa ít nhất 1 ký tự viết hoa!");
                } else if (!pass.matches(".*[0-9].*")) {
                    showErrorMessage("Mật khẩu phải chứa ít nhất 1 ký tự số!");
                } else {
                    controller.attemptRegister(ipServer, user, email, pass, this);
                }
            }
        });

        lblCancel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lblCancel.setForeground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lblCancel.setForeground(Color.GRAY);
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
        btnRegister.setEnabled(!isLoading);
        btnRegister.setText(isLoading ? "ĐANG XỬ LÝ..." : "XÁC NHẬN ĐĂNG KÝ");
    }

    public void showSuccessMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showErrorMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi đăng ký", JOptionPane.ERROR_MESSAGE);
    }
}