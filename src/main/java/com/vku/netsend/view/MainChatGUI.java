package com.vku.netsend.view;

import com.vku.netsend.controller.ClientController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;

public class MainChatGUI extends JFrame {
    private JLabel lblStatus;
    private JTextField txtServerIP;

    private DefaultListModel<String> listModelOnline;
    private JList<String> listOnline;
    private JTextArea txtInbox;
    private JComboBox<String> cbRoomList;
    private JButton btnJoinGroup, btnCreateRoom, btnDisconnect;
    private JComboBox<String> cbSendMode;
    private JComboBox<String> cbTarget;
    private JTextField txtMessage;
    private JButton btnSend;

    private ClientController controller;
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font boldFont = new Font("Segoe UI", Font.BOLD, 14);

    public MainChatGUI(ClientController controller, String displayName, String connectedIP) {
        this.controller = controller;
        setTitle("Net Send Client - " + displayName);
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 247, 250));

        initUI(displayName, connectedIP);
    }

    private void initUI(String displayName, String connectedIP) {
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setBackground(new Color(245, 247, 250));

        // --- CỘT TRÁI ---
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setPreferredSize(new Dimension(280, 0));
        leftPanel.setOpaque(false);

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), " Tài khoản của tôi "),
                new EmptyBorder(5, 10, 5, 10)));
        lblStatus = new JLabel("Tên: " + displayName);
        lblStatus.setFont(boldFont);
        lblStatus.setForeground(new Color(39, 174, 96));
        infoPanel.add(lblStatus);

        btnDisconnect = new JButton("ĐĂNG XUẤT");
        btnDisconnect.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDisconnect.setBackground(new Color(231, 76, 60));
        btnDisconnect.setForeground(Color.WHITE);
        btnDisconnect.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDisconnect.setOpaque(true);
        btnDisconnect.setContentAreaFilled(true);
        btnDisconnect.setBorderPainted(false);
        infoPanel.add(btnDisconnect);
        leftPanel.add(infoPanel, BorderLayout.NORTH);

        listModelOnline = new DefaultListModel<>();
        listOnline = new JList<>(listModelOnline);
        listOnline.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listOnline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollOnline = new JScrollPane(listOnline);
        scrollOnline.setBorder(BorderFactory
                .createTitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), " Người dùng Online "));
        leftPanel.add(scrollOnline, BorderLayout.CENTER);
        contentPane.add(leftPanel, BorderLayout.WEST);

        // --- CỘT PHẢI ---
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setOpaque(false);

        JPanel topRightPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        topRightPanel.setOpaque(false);

        JPanel serverPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        serverPanel.setBackground(Color.WHITE);
        serverPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)),
                " Máy chủ mạng đang kết nối "));
        serverPanel.add(new JLabel("IP Server: "));
        txtServerIP = new JTextField(connectedIP, 25);
        txtServerIP.setFont(mainFont);
        txtServerIP.setEditable(false); // Khóa lại, không cho đổi trực tiếp ở đây
        serverPanel.add(txtServerIP);
        topRightPanel.add(serverPanel);

        JPanel groupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        groupPanel.setBackground(Color.WHITE);
        groupPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), " Phòng ban "));
        groupPanel.add(new JLabel("Nhóm: "));
        cbRoomList = new JComboBox<>();
        cbRoomList.setFont(mainFont);
        cbRoomList.setPreferredSize(new Dimension(120, 28));
        groupPanel.add(cbRoomList);

        btnCreateRoom = new JButton("Tạo");
        btnCreateRoom.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCreateRoom.setBackground(new Color(39, 174, 96));
        btnCreateRoom.setForeground(Color.WHITE);
        btnCreateRoom.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCreateRoom.setOpaque(true);
        btnCreateRoom.setContentAreaFilled(true);
        btnCreateRoom.setBorderPainted(false);
        groupPanel.add(btnCreateRoom);

        btnJoinGroup = new JButton("Vào");
        btnJoinGroup.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnJoinGroup.setBackground(new Color(41, 128, 185));
        btnJoinGroup.setForeground(Color.WHITE);
        btnJoinGroup.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnJoinGroup.setOpaque(true);
        btnJoinGroup.setContentAreaFilled(true);
        btnJoinGroup.setBorderPainted(false);
        groupPanel.add(btnJoinGroup);
        topRightPanel.add(groupPanel);

        rightPanel.add(topRightPanel, BorderLayout.NORTH);

        txtInbox = new JTextArea();
        txtInbox.setEditable(false);
        txtInbox.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtInbox.setBorder(new EmptyBorder(5, 5, 5, 5));
        JScrollPane scrollInbox = new JScrollPane(txtInbox);
        scrollInbox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)),
                " Bảng tin "));
        rightPanel.add(scrollInbox, BorderLayout.CENTER);

        JPanel sendPanel = new JPanel(new BorderLayout(10, 5));
        sendPanel.setBackground(Color.WHITE);
        sendPanel.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), " Gửi Net Send "),
                new EmptyBorder(10, 10, 10, 10)));

        JPanel targetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        targetPanel.setOpaque(false);
        cbSendMode = new JComboBox<>(new String[] { "Cá nhân (Theo IP/Tên)", "Nhóm (Group)", "Tất cả (*)" });
        cbSendMode.setFont(mainFont);
        targetPanel.add(cbSendMode);
        targetPanel.add(new JLabel("  Đích: "));
        cbTarget = new JComboBox<>();
        cbTarget.setEditable(true);
        cbTarget.setPreferredSize(new Dimension(200, 28));
        cbTarget.setFont(mainFont);
        targetPanel.add(cbTarget);
        sendPanel.add(targetPanel, BorderLayout.NORTH);

        JPanel msgPanel = new JPanel(new BorderLayout(10, 0));
        msgPanel.setOpaque(false);
        msgPanel.add(new JLabel("Nội dung: "), BorderLayout.WEST);
        txtMessage = new JTextField();
        txtMessage.setFont(mainFont);
        msgPanel.add(txtMessage, BorderLayout.CENTER);

        btnSend = new JButton("Gửi Tin");
        btnSend.setFont(boldFont);
        btnSend.setBackground(new Color(41, 128, 185));
        btnSend.setForeground(Color.WHITE);
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSend.setOpaque(true);
        btnSend.setContentAreaFilled(true);
        btnSend.setBorderPainted(false);
        msgPanel.add(btnSend, BorderLayout.EAST);
        sendPanel.add(msgPanel, BorderLayout.CENTER);

        rightPanel.add(sendPanel, BorderLayout.SOUTH);
        contentPane.add(rightPanel, BorderLayout.CENTER);
        add(contentPane);

        // --- SỰ KIỆN ---
        btnDisconnect.addActionListener(e -> controller.disconnect());
        
        btnJoinGroup.addActionListener(e -> {
            if (cbRoomList.getSelectedItem() != null) {
                controller.joinGroup(cbRoomList.getSelectedItem().toString().trim());
            }
        });
        
        btnCreateRoom.addActionListener(e -> {
            String roomName = JOptionPane.showInputDialog(this, "Nhập tên phòng muốn tạo:", "Tạo phòng mới", JOptionPane.PLAIN_MESSAGE);
            if (roomName != null && !roomName.trim().isEmpty()) {
                controller.createRoom(roomName.trim());
            }
        });

        btnSend.addActionListener(e -> {
            String target = cbTarget.getSelectedItem() != null ? cbTarget.getSelectedItem().toString().trim() : "";
            controller.sendNetSendMessage(cbSendMode.getSelectedIndex(), target, txtMessage.getText().trim());
        });

        cbSendMode.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (cbSendMode.getSelectedIndex() == 2) { // Tất cả
                    cbTarget.removeAllItems();
                    cbTarget.setSelectedItem("*");
                    cbTarget.setEnabled(false);
                } else if (cbSendMode.getSelectedIndex() == 1) { // Nhóm
                    cbTarget.setEnabled(true);
                    cbTarget.removeAllItems();
                    for (int i = 0; i < cbRoomList.getItemCount(); i++) {
                        cbTarget.addItem(cbRoomList.getItemAt(i));
                    }
                    cbTarget.setSelectedItem("");
                } else { // Cá nhân
                    cbTarget.setEnabled(true);
                    cbTarget.removeAllItems();
                    cbTarget.setSelectedItem("");
                }
            }
        });

        // Click vào tên trong List sẽ lấy đúng chữ số IP đẩy xuống ô gửi tin
        listOnline.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listOnline.getSelectedValue() != null
                    && cbSendMode.getSelectedIndex() == 0) {
                String selected = listOnline.getSelectedValue();
                if (selected.contains("IP: ")) {
                    String ip = selected.substring(selected.indexOf("IP: ") + 4);
                    cbTarget.setSelectedItem(ip);
                }
            }
        });
    }

    public void clearMessageField() {
        txtMessage.setText("");
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public void appendInbox(String msg) {
        txtInbox.append(msg);
        txtInbox.setCaretPosition(txtInbox.getDocument().getLength());
    }

    public void updateOnlineList(String onlineData) {
        listModelOnline.clear();
        String[] users = onlineData.split(",");
        for (String u : users) {
            if (!u.trim().isEmpty())
                listModelOnline.addElement("• " + u.trim());
        }
    }

    public void updateRoomList(String roomData) {
        Object currentSelected = cbRoomList.getSelectedItem();
        Object currentTargetSelected = cbTarget.getSelectedItem();
        cbRoomList.removeAllItems();
        String[] rooms = roomData.split(",");
        for (String r : rooms) {
            if (!r.trim().isEmpty()) {
                cbRoomList.addItem(r.trim());
            }
        }
        if (currentSelected != null) {
            cbRoomList.setSelectedItem(currentSelected);
        }
        
        if (cbSendMode != null && cbSendMode.getSelectedIndex() == 1) {
            cbTarget.removeAllItems();
            for (String r : rooms) {
                if (!r.trim().isEmpty()) {
                    cbTarget.addItem(r.trim());
                }
            }
            if (currentTargetSelected != null) {
                cbTarget.setSelectedItem(currentTargetSelected);
            }
        }
    }
}