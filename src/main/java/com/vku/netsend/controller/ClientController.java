package com.vku.netsend.controller;

import com.vku.netsend.network.MulticastService;
import com.vku.netsend.network.TCPConnection;
import com.vku.netsend.view.MainChatGUI;
import com.vku.netsend.view.LoginGUI;
import com.vku.netsend.view.RegisterGUI;
import javax.swing.SwingUtilities;

public class ClientController {
    private MainChatGUI mainView;
    private TCPConnection connection;
    private MulticastService multicastService = new MulticastService();
    private String currentUsername = "";
    private boolean isConnected = false;

    public void attemptLogin(String ip, String username, String pass, LoginGUI dialog) {
        dialog.setLoadingState(true);
        new Thread(() -> {
            try {
                connection = new TCPConnection(ip, 9876);
                connection.send("LOGIN::" + username + "::" + pass);
                String response = connection.receive();

                SwingUtilities.invokeLater(() -> {
                    if (response.startsWith("SYS::Đăng nhập thành công")) {
                        this.currentUsername = username;
                        this.isConnected = true;

                        dialog.dispose();
                        mainView = new MainChatGUI(this, username, ip);
                        mainView.setVisible(true);

                        // Khởi động Multicast cho Lab 6
                        multicastService.start(ip, username, msg -> {
                            SwingUtilities.invokeLater(() -> mainView.appendInbox(msg));
                        });

                        new Thread(this::listenIncomingMessages).start();
                    } else {
                        dialog.showErrorMessage(response.replace("SYS::", ""));
                        connection.close();
                    }
                    dialog.setLoadingState(false);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    dialog.showErrorMessage("Không kết nối được Server " + ip);
                    dialog.setLoadingState(false);
                });
            }
        }).start();
    }

    // Đã xóa tham số displayName
    public void attemptRegister(String ip, String username, String email, String pass, RegisterGUI dialog) {
        dialog.setLoadingState(true);
        new Thread(() -> {
            try {
                TCPConnection tempConn = new TCPConnection(ip, 9876);
                tempConn.send("REGISTER::" + username + "::" + email + "::" + pass);
                String response = tempConn.receive();
                tempConn.close();

                SwingUtilities.invokeLater(() -> {
                    if (response.startsWith("SYS::Đăng ký thành công")) {
                        dialog.showSuccessMessage("Đăng ký thành công!");
                        dialog.dispose();
                    } else {
                        dialog.showErrorMessage(response.replace("SYS::", ""));
                    }
                    dialog.setLoadingState(false);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    dialog.showErrorMessage("Lỗi kết nối Server lúc đăng ký!");
                    dialog.setLoadingState(false);
                });
            }
        }).start();
    }

    public void disconnect() {
        if (isConnected && connection != null && !connection.isClosed()) {
            try {
                connection.send("LOGOUT::" + currentUsername);
            } catch (Exception e) {
            }
        }
        if (multicastService != null) {
            multicastService.stop();
        }
        isConnected = false;
        if (connection != null)
            connection.close();
        if (mainView != null)
            mainView.dispose();
        SwingUtilities.invokeLater(() -> new LoginGUI(this).setVisible(true));
    }

    public void joinGroup(String groupName) {
        if (groupName.isEmpty())
            return;
        // Tham gia nhóm Multicast
        if (multicastService != null) {
            multicastService.joinRoom(groupName);
        }
        try {
            connection.send("JOIN_GROUP::" + currentUsername + "::" + groupName);
        } catch (Exception e) {
            disconnect();
        }
    }

    public void sendNetSendMessage(int modeIndex, String targetValue, String message) {
        String targetType = modeIndex == 0 ? "IP_NAME" : (modeIndex == 1 ? "GROUP" : "ALL");
        if (modeIndex == 2)
            targetValue = "*";

        // Gửi qua Multicast (Lập trình Multicast Socket Lab 6)
        if (multicastService != null) {
            if (modeIndex == 2 || "*".equals(targetValue)) {
                multicastService.sendToAll(message);
            } else if (modeIndex == 1) {
                multicastService.sendToGroup(targetValue, message);
            } else if (modeIndex == 0 && targetValue.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                multicastService.sendToIP(targetValue, message);
            }
        }

        // Đồng bộ lưu lịch sử Server và chuyển tiếp TCP dự phòng
        try {
            connection.send("NET_SEND::" + currentUsername + "::" + targetType + "::" + targetValue + "::" + message);
            mainView.clearMessageField();
        } catch (Exception e) {
            mainView.showError("Lỗi gửi tin: " + e.getMessage());
            disconnect();
        }
    }

    public void createRoom(String roomName) {
        if (roomName == null || roomName.trim().isEmpty())
            return;
        try {
            connection.send("CREATE_ROOM::" + roomName.trim());
        } catch (Exception e) {
            disconnect();
        }
    }

    // Hàm lắng nghe tin nhắn từ client khác gửi tới server
    private void listenIncomingMessages() {
        while (isConnected) {
            try {
                String data = connection.receive();
                if (data == null || data.isEmpty())
                    continue;
                SwingUtilities.invokeLater(() -> {
                    if (data.startsWith("SYS::")) {
                        String msg = data.substring(5);
                        if (msg.startsWith("Lỗi:")) {
                            mainView.showError(msg);
                        } else {
                            mainView.appendInbox("[Hệ thống]: " + msg + "\n");
                        }
                    } else if (data.startsWith("MSG::") || data.startsWith("HISTORY::")) {
                        mainView.appendInbox(data.substring(data.indexOf("::") + 2) + "\n");
                    } else if (data.startsWith("ONLINE_LIST::")) {
                        mainView.updateOnlineList(data.substring(13));
                    } else if (data.startsWith("ROOM_LIST::")) {
                        mainView.updateRoomList(data.substring(11));
                    }
                });
            } catch (Exception e) {
                if (isConnected) {
                    SwingUtilities.invokeLater(() -> {
                        mainView.showError("Mất kết nối với Server!");
                        disconnect();
                    });
                }
                break;
            }
        }
    }
}