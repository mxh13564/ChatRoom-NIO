package net.qiujuer.lesson.sample.client.GUI;

import net.qiujuer.lesson.sample.client.bean.ServerInfo;
import net.qiujuer.lesson.sample.foo.Foo;
import net.qiujuer.library.clink.box.FileSendPacket;
import net.qiujuer.library.clink.core.ConnectorFinal;
import net.qiujuer.library.clink.core.Packet;
import net.qiujuer.library.clink.core.ReceivedPacket;
import net.qiujuer.library.clink.utils.CloseUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class TCPClientFinalForGUI extends ConnectorFinal implements ActionListener {
    protected static Login login;

    protected static ClientFrame clientFrame; //操作界面

    public static String name;

    public static String socketId;

    private File cachePath;

    protected static TCPClientFinalForGUI chatRoomClient;

    private static final ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    public TCPClientFinalForGUI() {

    }

    public static Date parse(String dateStr) throws ParseException {
        return df.get().parse(dateStr);
    }

    public static String format(Date date) {
        return df.get().format(date);
    }


    public void exit() {
        CloseUtils.close(this);
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        System.out.println("连接已关闭，无法读取数据!");
    }

    @Override
    protected File createNewReceiveFile() {
        return Foo.createRandomTemp(cachePath);
    }

    @Override
    protected void onReceivedPacket(ReceivedPacket packet) {
        super.onReceivedPacket(packet);
        if (packet.type() == Packet.TYPE_MEMORY_STRING) {
            String msg = (String) packet.entity();
            this.clientFrame.updateTextArea(msg, socketId);
        } else if (packet.type() == Packet.TYPE_STREAM_FILE) {
            clientFrame.insertMessage(clientFrame.userMsgArea, null, "" + " "
                    + TCPClientFinalForGUI.format(new Date()), " "
                    + "接受群文件成功", false);
        }
    }


    public void startWith(ServerInfo info, File cachePath) {
        SocketChannel socketChannel = null;
        try {
            socketChannel = SocketChannel.open();
            // 连接本地，端口2000；超时时间3000ms
            socketChannel.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()), info.getPort()));
            System.out.println("已发起服务器连接，并进入后续流程～");
            System.out.println("客户端信息：" + socketChannel.getLocalAddress().toString());
            System.out.println("服务器信息：" + socketChannel.getRemoteAddress().toString());
            this.cachePath = cachePath;
            super.setup(socketChannel);
            if (socketChannel.isConnected()) {
                clientFrame.head_connect.setText("已连接");
                clientFrame.head_exit.setText("退出");
                socketId = String.valueOf(System.identityHashCode(socketChannel));
            }
        } catch (Exception e) {
            System.out.println("连接异常");
            JOptionPane.showMessageDialog(clientFrame.frame, "连接异常!", "错误", JOptionPane.ERROR_MESSAGE);
            CloseUtils.close(socketChannel);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (!cmd.equals("进入")) {
            name = clientFrame.name_textfield.getText();
        }
        switch (cmd) {
            case "进入": //进入初始化界面
                String code = login.textField.getText();
                if (Objects.equals(code, "")) {
                    JOptionPane.showMessageDialog(null, "权限密令不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
                    break;
                } else {
                    if (code.equals("888")) {
                        clientFrame = new User(chatRoomClient);
                    } else {
                        JOptionPane.showMessageDialog(null, "权限密令错误，请重新输入！", "错误", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                }
                login.dispose();
                ClientFrameContext clientFrameContext = new ClientFrameContext(clientFrame);
                clientFrameContext.init();
                break;
            case "连接":
                //获取文本框里面的ip和port
                String strhost = clientFrame.host_textfield.getText();
                String strport = clientFrame.port_textfield.getText();
                if (!User.ipCheckHost(strhost)) {
                    JOptionPane.showMessageDialog(clientFrame.frame, "请检查ip格式是否准确！", "错误", JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (!User.ipCheckPort(strport)) {
                    JOptionPane.showMessageDialog(clientFrame.frame, "请检查端口号是否为0~65535之间的整数！", "错误", JOptionPane.ERROR_MESSAGE);
                    break;
                }
                //连接服务器;
                ServerInfo serverInfo = new ServerInfo(Integer.parseInt(strport), strhost, "");
                chatRoomClient.startWith(serverInfo, Foo.getCacheDir("client"));
                clientFrame.port_textfield.setEditable(false);
                clientFrame.name_textfield.setEditable(false);
                clientFrame.host_textfield.setEditable(false);
                break;
            case "退出":
                if (TCPClientFinalForGUI.channel == null || !TCPClientFinalForGUI.channel.isOpen()) {
                    JOptionPane.showMessageDialog(clientFrame.frame, "对不起，您现在不在聊天室，无法退出！", "错误", JOptionPane.ERROR_MESSAGE);
                    break;
                }
                exit();
                if (!TCPClientFinalForGUI.channel.isConnected()) {
                    clientFrame.head_connect.setText("连接");
                }
                break;
            case "发送":
                if (TCPClientFinalForGUI.channel == null || !TCPClientFinalForGUI.channel.isOpen()) {
                    JOptionPane.showMessageDialog(clientFrame.frame, "请先连接服务器进入聊天室！", "提示", JOptionPane.WARNING_MESSAGE);
                    break;
                }
                String text = clientFrame.text_field.getText();
                if (text != null && !text.isEmpty()) {
                    String formatMsg = "<id>" + TCPClientFinalForGUI.socketId + "</id>"
                            + "<name>" + TCPClientFinalForGUI.name + "</name>"
                            + "<msg>" + text + "</msg>"
                            + "<time>" + TCPClientFinalForGUI.format(new Date()) + "</time>";
                    chatRoomClient.send(formatMsg);
                    clientFrame.text_field.setText("");
                }
                break;
            case "发送文件":
                if (TCPClientFinalForGUI.channel == null || !TCPClientFinalForGUI.channel.isOpen()) {
                    JOptionPane.showMessageDialog(clientFrame.frame, "请先连接服务器进入聊天室！", "提示", JOptionPane.WARNING_MESSAGE);
                    break;
                }
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(clientFrame.frame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    FileSendPacket packet = new FileSendPacket(selectedFile);
                    chatRoomClient.send(packet);
                    JOptionPane.showMessageDialog(clientFrame.frame, "文件发送成功: " + selectedFile.getName(), "提示", JOptionPane.INFORMATION_MESSAGE);
                }
                break;
            case "清空聊天消息":
                clientFrame.userMsgArea.setText("");
            default:
                System.out.println("监听出错，无效的按钮事件！");
                break;
        }
    }
}
