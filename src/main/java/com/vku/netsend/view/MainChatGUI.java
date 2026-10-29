package com.vku.netsend.view;

import com.vku.netsend.controller.ClientController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainChatGUI extends JFrame {
    private JLabel lblStatus;
    private JTextField txtServerIP;

    private DefaultListModel<String> listModelOnline;
    private JList<String> listOnline;
    private JComboBox<String> cbRoomList;
    private JButton btnJoinGroup, btnCreateRoom, btnDisconnect;
    private JComboBox<String> cbSendMode;
    private JComboBox<String> cbTarget;
    private JTextField txtMessage;
    private JButton btnSend;
    private JButton btnEmoji;

    // Nút và ô Đính kèm file + nút Voice
    private JButton btnAttach;
    private JTextField txtAttachedFile;
    private JButton btnVoice;
    private File selectedFileOrFolder = null;

    // Chat bubble container
    private JPanel chatMessagesPanel;
    private JScrollPane scrollChat;

    private ClientController controller;
    private String currentUsername;

    // Font hỗ trợ Emoji và ký tự đặc biệt (Segoe UI Emoji)
    private static final Font EMOJI_FONT = getSupportedEmojiFont(14);
    private static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);

    // Bảng màu phong cách Zalo
    private static final Color ZALO_BLUE = new Color(0, 104, 255);
    private static final Color ZALO_LIGHT_BLUE = new Color(229, 239, 255); // Bubble gửi đi
    private static final Color ZALO_GRAY_BUBBLE = Color.WHITE; // Bubble nhận
    private static final Color ZALO_BORDER = new Color(218, 224, 233);
    private static final Color BG_CHAT = new Color(238, 242, 246);

    /**
     * Tìm kiếm Font hỗ trợ hiển thị Emoji tốt nhất trên hệ điều hành
     */
    private static Font getSupportedEmojiFont(int size) {
        String[] fontNames = { "Segoe UI Emoji", "Segoe UI Symbol", "Apple Color Emoji", "Noto Color Emoji", "Dialog" };
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        java.util.Set<String> available = new java.util.HashSet<>(
                java.util.Arrays.asList(ge.getAvailableFontFamilyNames()));

        for (String name : fontNames) {
            if (available.contains(name)) {
                return new Font(name, Font.PLAIN, size);
            }
        }
        return new Font("Dialog", Font.PLAIN, size);
    }

    public MainChatGUI(ClientController controller, String displayName, String connectedIP) {
        this.controller = controller;
        this.currentUsername = displayName;
        setTitle("Zalo Net Send - " + displayName);

        // Mở toàn màn hình
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(950, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 247, 250));

        initUI(displayName, connectedIP);
    }

    /**
     * Tạo Custom Button có màu nền rõ ràng trên Windows Look and Feel
     */
    private JButton createColoredButton(String text, Color bgColor, Color fgColor, Font font) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(
                            Math.min(255, bgColor.getRed() + 20),
                            Math.min(255, bgColor.getGreen() + 20),
                            Math.min(255, bgColor.getBlue() + 20)));
                } else {
                    g2.setColor(bgColor);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(fgColor);
        btn.setFont(font != null ? font : new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        return btn;
    }

    private void initUI(String displayName, String connectedIP) {
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setBackground(new Color(245, 247, 250));

        // --- CỘT TRÁI (DANH SÁCH & PROFILE) ---
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setPreferredSize(new Dimension(290, 0));
        leftPanel.setOpaque(false);

        // Header Profile User
        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ZALO_BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)));

        JPanel userDetail = new JPanel(new GridLayout(2, 1, 2, 2));
        userDetail.setOpaque(false);
        lblStatus = new JLabel(displayName);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblStatus.setForeground(new Color(30, 41, 59));
        JLabel lblSub = new JLabel("● Đang trực tuyến");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(34, 197, 94));
        userDetail.add(lblStatus);
        userDetail.add(lblSub);
        infoPanel.add(userDetail, BorderLayout.CENTER);

        btnDisconnect = createColoredButton("Đăng xuất", new Color(239, 68, 68), Color.WHITE, null);
        infoPanel.add(btnDisconnect, BorderLayout.EAST);
        leftPanel.add(infoPanel, BorderLayout.NORTH);

        // Online Users List
        listModelOnline = new DefaultListModel<>();
        listOnline = new JList<>(listModelOnline);
        listOnline.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listOnline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listOnline.setFixedCellHeight(38);
        listOnline.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                label.setBorder(new EmptyBorder(4, 10, 4, 10));
                if (isSelected) {
                    label.setBackground(new Color(224, 238, 255));
                    label.setForeground(ZALO_BLUE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(new Color(51, 65, 85));
                }
                return label;
            }
        });

        JScrollPane scrollOnline = new JScrollPane(listOnline);
        scrollOnline.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ZALO_BORDER, 1, true), " Bạn bè trực tuyến "));
        leftPanel.add(scrollOnline, BorderLayout.CENTER);
        contentPane.add(leftPanel, BorderLayout.WEST);

        // --- CỘT PHẢI (CHAT AREA PHONG CÁCH ZALO) ---
        JPanel rightPanel = new JPanel(new BorderLayout(0, 8));
        rightPanel.setOpaque(false);

        // Thanh điều hướng trên cùng (Phòng ban & Server IP)
        JPanel topRightPanel = new JPanel(new BorderLayout(8, 0));
        topRightPanel.setBackground(Color.WHITE);
        topRightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ZALO_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)));

        JPanel groupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        groupPanel.setOpaque(false);
        groupPanel.add(new JLabel("Phòng chat:"));
        cbRoomList = new JComboBox<>();
        cbRoomList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbRoomList.setPreferredSize(new Dimension(140, 30));
        groupPanel.add(cbRoomList);

        btnJoinGroup = createColoredButton("Vào phòng", ZALO_BLUE, Color.WHITE, null);
        groupPanel.add(btnJoinGroup);

        btnCreateRoom = createColoredButton("+ Tạo phòng", new Color(16, 185, 129), Color.WHITE, null);
        groupPanel.add(btnCreateRoom);
        topRightPanel.add(groupPanel, BorderLayout.WEST);

        JPanel serverPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        serverPanel.setOpaque(false);
        serverPanel.add(new JLabel("Server:"));
        txtServerIP = new JTextField(connectedIP, 12);
        txtServerIP.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtServerIP.setEditable(false);
        txtServerIP.setBackground(new Color(241, 245, 249));
        serverPanel.add(txtServerIP);
        topRightPanel.add(serverPanel, BorderLayout.EAST);

        rightPanel.add(topRightPanel, BorderLayout.NORTH);

        // KHUNG HIỂN THỊ TIN NHẮN (Bong bóng chat Zalo)
        chatMessagesPanel = new JPanel();
        chatMessagesPanel.setLayout(new BoxLayout(chatMessagesPanel, BoxLayout.Y_AXIS));
        chatMessagesPanel.setBackground(BG_CHAT);
        chatMessagesPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        scrollChat = new JScrollPane(chatMessagesPanel);
        scrollChat.setBorder(BorderFactory.createLineBorder(ZALO_BORDER, 1, true));
        scrollChat.getVerticalScrollBar().setUnitIncrement(16);
        scrollChat.setBackground(BG_CHAT);
        scrollChat.getViewport().setBackground(BG_CHAT);
        rightPanel.add(scrollChat, BorderLayout.CENTER);

        // KHU VỰC SOẠN THẢO VÀ GỬI TIN NHẮN
        JPanel sendPanel = new JPanel(new BorderLayout(8, 8));
        sendPanel.setBackground(Color.WHITE);
        sendPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ZALO_BORDER, 1, true),
                new EmptyBorder(8, 12, 10, 12)));

        // Hàng 1: Chọn đối tượng gửi
        JPanel targetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        targetPanel.setOpaque(false);
        targetPanel.add(new JLabel("Gửi tới:"));
        cbSendMode = new JComboBox<>(new String[] { "Cá nhân (IP / Tên)", "Nhóm (Group)", "Tất cả (*)" });
        cbSendMode.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        targetPanel.add(cbSendMode);

        cbTarget = new JComboBox<>();
        cbTarget.setEditable(true);
        cbTarget.setPreferredSize(new Dimension(180, 28));
        cbTarget.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        targetPanel.add(cbTarget);
        sendPanel.add(targetPanel, BorderLayout.NORTH);

        // Hàng 2: Thanh đính kèm file + nút Voice (Sử dụng Segoe UI Emoji cho icon
        // không bị lỗi ô vuông)
        JPanel attachPanel = new JPanel(new BorderLayout(6, 0));
        attachPanel.setOpaque(false);

        Font iconBtnFont = getSupportedEmojiFont(12);
        btnAttach = createColoredButton("📁 Đính kèm", new Color(241, 245, 249), new Color(30, 41, 59), iconBtnFont);
        btnAttach.setToolTipText("Chọn File hoặc Thư mục cần gửi");
        attachPanel.add(btnAttach, BorderLayout.WEST);

        txtAttachedFile = new JTextField("Chưa chọn file đính kèm nào...");
        txtAttachedFile.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        txtAttachedFile.setEditable(false);
        txtAttachedFile.setEnabled(false);
        txtAttachedFile.setBackground(new Color(248, 250, 252));
        txtAttachedFile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ZALO_BORDER, 1, true),
                new EmptyBorder(4, 8, 4, 8)));
        attachPanel.add(txtAttachedFile, BorderLayout.CENTER);

        btnVoice = createColoredButton("🎙 Voice", new Color(139, 92, 246), Color.WHITE, iconBtnFont);
        btnVoice.setToolTipText("Ghi âm giọng nói");
        attachPanel.add(btnVoice, BorderLayout.EAST);

        // Panel trung tâm chứa Đính kèm + Ô nhập tin nhắn
        JPanel centerInputPanel = new JPanel(new BorderLayout(0, 6));
        centerInputPanel.setOpaque(false);
        centerInputPanel.add(attachPanel, BorderLayout.NORTH);

        // Hàng 3: Thanh input tin nhắn + nút Emoji + nút Gửi
        JPanel msgPanel = new JPanel(new BorderLayout(6, 0));
        msgPanel.setOpaque(false);

        btnEmoji = createColoredButton("😊", new Color(241, 245, 249), new Color(30, 41, 59),
                getSupportedEmojiFont(18));
        btnEmoji.setToolTipText("Chọn biểu tượng cảm xúc");
        btnEmoji.setBorder(new EmptyBorder(4, 10, 4, 10));
        msgPanel.add(btnEmoji, BorderLayout.WEST);

        // Quan trọng: Dùng EMOJI_FONT cho txtMessage để hiển thị chuẩn xác Emoji
        txtMessage = new JTextField();
        txtMessage.setFont(EMOJI_FONT);
        txtMessage.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ZALO_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        msgPanel.add(txtMessage, BorderLayout.CENTER);

        btnSend = createColoredButton("Gửi", ZALO_BLUE, Color.WHITE, BOLD_FONT);
        btnSend.setPreferredSize(new Dimension(85, 36));
        msgPanel.add(btnSend, BorderLayout.EAST);
        centerInputPanel.add(msgPanel, BorderLayout.CENTER);

        sendPanel.add(centerInputPanel, BorderLayout.CENTER);

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
            String roomName = JOptionPane.showInputDialog(this, "Nhập tên phòng muốn tạo:", "Tạo phòng mới",
                    JOptionPane.PLAIN_MESSAGE);
            if (roomName != null && !roomName.trim().isEmpty()) {
                controller.createRoom(roomName.trim());
            }
        });

        // Bấm Enter để gửi tin
        txtMessage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnSend.doClick();
                }
            }
        });

        // Xử lý nút Đính kèm
        btnAttach.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Chọn tập tin hoặc thư mục cần gửi");
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFileOrFolder = chooser.getSelectedFile();
                if (selectedFileOrFolder.isDirectory()) {
                    txtAttachedFile.setText("[Thư mục]: " + selectedFileOrFolder.getName());
                } else {
                    txtAttachedFile.setText("[File]: " + selectedFileOrFolder.getName() + " ("
                            + (selectedFileOrFolder.length() / 1024) + " KB)");
                }
                txtAttachedFile.setForeground(new Color(15, 23, 42));
                txtAttachedFile.setFont(new Font("Segoe UI", Font.BOLD, 12));
            }
        });

        // Nút Voice
        btnVoice.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Tính năng ghi âm giọng nói (Voice Note) sẵn sàng kích hoạt ở bước tiếp theo!",
                    "Ghi âm Voice", JOptionPane.INFORMATION_MESSAGE);
        });

        btnSend.addActionListener(e -> {
            String rawMessage = txtMessage.getText().trim();
            if (selectedFileOrFolder != null) {
                String attachTag = (selectedFileOrFolder.isDirectory() ? "📁 [Thư mục]: " : "📎 [File]: ")
                        + selectedFileOrFolder.getName();
                rawMessage = rawMessage.isEmpty() ? attachTag : rawMessage + " (" + attachTag + ")";
                selectedFileOrFolder = null;
                txtAttachedFile.setText("Chưa chọn file đính kèm nào...");
                txtAttachedFile.setForeground(Color.GRAY);
                txtAttachedFile.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            }

            if (rawMessage.isEmpty())
                return;

            // Hỗ trợ thực hiện lệnh: net send {IP | group | *} message
            if (rawMessage.toLowerCase().startsWith("net send ")) {
                String cmdBody = rawMessage.substring(9).trim();
                String[] parts = cmdBody.split("\\s+", 2);
                if (parts.length >= 2) {
                    String target = parts[0].trim();
                    String actualMsg = parts[1].trim();

                    if ("*".equals(target)) {
                        controller.sendNetSendMessage(2, "*", actualMsg);
                    } else if (isRoom(target)) {
                        controller.sendNetSendMessage(1, target, actualMsg);
                    } else {
                        controller.sendNetSendMessage(0, target, actualMsg);
                    }
                    return;
                }
            }

            String target = cbTarget.getSelectedItem() != null ? cbTarget.getSelectedItem().toString().trim() : "";
            controller.sendNetSendMessage(cbSendMode.getSelectedIndex(), target, rawMessage);
        });

        // Emoji popup menu
        btnEmoji.addActionListener(e -> showEmojiPopup());

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
                    for (int i = 0; i < listModelOnline.getSize(); i++) {
                        String userLine = listModelOnline.getElementAt(i);
                        if (userLine.contains("IP: ")) {
                            cbTarget.addItem(userLine.substring(userLine.indexOf("IP: ") + 4));
                        }
                    }
                    cbTarget.setSelectedItem("");
                }
            }
        });

        // Click chọn User từ danh sách Online
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

    private void showEmojiPopup() {
        JPopupMenu emojiMenu = new JPopupMenu();
        JPanel emojiPanel = new JPanel(new GridLayout(3, 8, 4, 4));
        emojiPanel.setBackground(Color.WHITE);
        emojiPanel.setBorder(new EmptyBorder(6, 6, 6, 6));

        String[] emojis = {
                "😀", "😁", "😂", "🤣", "😃", "😄", "😅", "😆",
                "😉", "😊", "😋", "😎", "😍", "😘", "🥰", "😗",
                "👍", "👎", "👏", "🙌", "❤️", "💔", "🔥", "🎉"
        };

        Font emoFont = getSupportedEmojiFont(20);
        for (String emo : emojis) {
            JButton b = new JButton(emo);
            b.setFont(emoFont);
            b.setMargin(new Insets(2, 2, 2, 2));
            b.setBackground(Color.WHITE);
            b.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.addActionListener(ev -> {
                txtMessage.setText(txtMessage.getText() + emo);
                txtMessage.requestFocus();
                emojiMenu.setVisible(false);
            });
            emojiPanel.add(b);
        }

        emojiMenu.add(emojiPanel);
        emojiMenu.show(btnEmoji, 0, -emojiMenu.getPreferredSize().height - 5);
    }

    public void clearMessageField() {
        txtMessage.setText("");
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private boolean isRoom(String target) {
        for (int i = 0; i < cbRoomList.getItemCount(); i++) {
            if (cbRoomList.getItemAt(i).equalsIgnoreCase(target))
                return true;
        }
        return false;
    }

    private final java.util.Map<String, Boolean> recentMessages=java.util.Collections.synchronizedMap(new java.util.LinkedHashMap<String,Boolean>(){@Override protected boolean removeEldestEntry(java.util.Map.Entry<String,Boolean>eldest){return size()>200;}});

    /**
     * Thêm tin nhắn và hiển thị dưới dạng Khối Bong Bóng (Chat Bubble Zalo)
     */
    public void appendInbox(String rawMsg) {
        String trimmed = rawMsg.trim();
        if (trimmed.isEmpty())
            return;

        // Xử lý chống trùng lặp tin multicast / TCP song song
        if (!trimmed.startsWith("[Hệ thống") && !trimmed.startsWith("---")) {
            String core = trimmed;
            if (trimmed.contains("]: ")) {
                core = trimmed.substring(trimmed.indexOf("]: ") + 3);
            }
            if (recentMessages.containsKey(core)) {
                return;
            }
            recentMessages.put(core, Boolean.TRUE);
        }

        // Tách chuỗi nhiều dòng nếu là lịch sử gửi về cùng lúc
        String[] lines = trimmed.split("\n");
        for (String line : lines) {
            String singleLine = line.trim();
            if (!singleLine.isEmpty()) {
                addBubble(singleLine);
            }
        }

        // Cuộn xuống cuối
        SwingUtilities.invokeLater(() -> {
            chatMessagesPanel.revalidate();
            chatMessagesPanel.repaint();
            JScrollBar vertical = scrollChat.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void addBubble(String line) {
        // 1. Dạng thông báo Hệ thống hoặc phân cách
        if (line.startsWith("[Hệ thống") || line.startsWith("---")) {
            JPanel sysRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
            sysRow.setOpaque(false);
            JLabel lblSys = new JLabel(line);
            lblSys.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            lblSys.setForeground(new Color(100, 116, 139));
            sysRow.add(lblSys);
            chatMessagesPanel.add(sysRow);
            chatMessagesPanel.add(Box.createVerticalStrut(4));
            return;
        }

        // 2. Phân tích tin nhắn chuẩn:
        // [Phạm vi] [Thời gian] [Từ IP: ... - Tên: ...]: Nội dung
        Pattern p = Pattern.compile("^\\[(.*?)\\]\\s*\\[(.*?)\\]\\s*\\[Từ IP:\\s*(.*?)\\s*-\\s*Tên:\\s*(.*?)\\]:\\s*(.*)$");
        Matcher m = p.matcher(line);

        String scope = "Tất cả";
        String time = "";
        String senderName = "";
        String content = line;
        boolean isMe = false;

        if (m.find()) {
            scope = m.group(1);
            time = m.group(2);
            senderName = m.group(4);
            content = m.group(5);
            isMe = senderName.equalsIgnoreCase(currentUsername);
        }

        // Rút gọn thời gian nếu dài (vd: "14:45:15 10/10/2026" -> "14:45 10/10/2026")
        String displayTime = time;
        if (time.length() >= 19 && time.charAt(2) == ':' && time.charAt(5) == ':') {
            displayTime = time.substring(0, 5) + " " + time.substring(9);
        }

        // Dòng chứa toàn bộ khối tin nhắn (Căn trái cho người khác, căn phải cho tôi)
        JPanel rowPanel = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        rowPanel.setOpaque(false);

        // Khối bong bóng chat phong cách Zalo
        BubblePanel bubble = new BubblePanel(isMe ? ZALO_LIGHT_BLUE : ZALO_GRAY_BUBBLE, isMe);
        bubble.setLayout(new BorderLayout(8, 6));
        bubble.setBorder(new EmptyBorder(10, 14, 10, 14));

        // --- DÒNG TRÊN: Tên góc trái - Dạng gửi góc phải cùng ---
        JPanel topHeader = new JPanel(new BorderLayout(30, 0));
        topHeader.setOpaque(false);

        // Chỉ hiển thị Tên
        JLabel lblName = new JLabel(senderName.isEmpty() ? currentUsername : senderName);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setForeground(new Color(30, 41, 59));
        topHeader.add(lblName, BorderLayout.WEST);

        JLabel lblScope = new JLabel("Gửi: " + scope);
        lblScope.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblScope.setForeground(new Color(100, 116, 139));
        topHeader.add(lblScope, BorderLayout.EAST);

        bubble.add(topHeader, BorderLayout.NORTH);

        // --- DÒNG GIỮA: Nội dung tin nhắn ---
        int maxBubbleWidth = 580;

        JTextArea txtContent = new JTextArea(content);
        txtContent.setFont(EMOJI_FONT); // Sử dụng font hỗ trợ emoji cho nội dung bubble
        txtContent.setEditable(false);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtContent.setOpaque(false);
        txtContent.setForeground(new Color(30, 41, 59));

        FontMetrics fm = txtContent.getFontMetrics(txtContent.getFont());
        int textWidth = fm.stringWidth(content);

        if (textWidth > maxBubbleWidth) {
            txtContent.setColumns(45);
        } else {
            int headerWidth = fm.stringWidth(lblName.getText()) + fm.stringWidth(lblScope.getText()) + 60;
            int finalWidth = Math.max(textWidth, headerWidth);
            txtContent.setPreferredSize(new Dimension(finalWidth, fm.getHeight()));
        }
        bubble.add(txtContent, BorderLayout.CENTER);

        // --- DÒNG DƯỚI: Thời gian gửi nằm dưới nội dung ---
        JPanel bottomFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomFooter.setOpaque(false);
        JLabel lblTime = new JLabel(displayTime);
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTime.setForeground(new Color(148, 163, 184));
        bottomFooter.add(lblTime);

        bubble.add(bottomFooter, BorderLayout.SOUTH);

        rowPanel.add(bubble);
        chatMessagesPanel.add(rowPanel);
        chatMessagesPanel.add(Box.createVerticalStrut(10));
    }

    /**
     * Panel vẽ nền bo tròn bóng mượt như Zalo
     */
    private static class BubblePanel extends JPanel {
        private final Color bgColor;
        private final boolean isMe;

        public BubblePanel(Color bgColor, boolean isMe) {
            this.bgColor = bgColor;
            this.isMe = isMe;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Nền bo góc tròn
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 16, 16));

            // Đường viền nhẹ
            g2.setColor(isMe ? new Color(190, 218, 255) : new Color(226, 232, 240));
            g2.draw(new RoundRectangle2D.Float(0, 0, width - 1, height - 1, 16, 16));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public void updateOnlineList(String onlineData) {
        listModelOnline.clear();
        String[] users = onlineData.split(",");
        for (String u : users) {
            if (!u.trim().isEmpty())
                listModelOnline.addElement("● " + u.trim());
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
