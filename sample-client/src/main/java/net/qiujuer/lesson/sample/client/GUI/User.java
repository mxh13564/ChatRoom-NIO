package net.qiujuer.lesson.sample.client.GUI;


import net.qiujuer.lesson.sample.client.TCPClientFinal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @Author: MXH
 * @Date:
 * @Description: User 普通用户的初始化聊天室窗口，继承总窗口类，实现初始化方法
 */

public class User extends ClientFrame {

    public User(TCPClientFinalForGUI chatRoomClient) {
        super(chatRoomClient);
    }

    @Override
    public void frameInit() {
        users_map = new ConcurrentHashMap<>();
        /*设置窗口的UI风格和字体*/
        frame = new JFrame("MXH聊天室用户端");
        JPanel panel = new JPanel();        /*主要的panel，上层放置连接区，下层放置消息区，中间是消息面板，左边是系统消息，右边是当前room的用户列表*/
        JPanel headpanel = new JPanel();    /*上层panel，用于放置连接区域相关的组件*/
        JPanel footpanel = new JPanel();    /*下层panel，用于放置发送信息区域的组件*/
        JPanel centerpanel = new JPanel();    /*中间panel，用于放置聊天信息*/

        /*顶层的布局，分中间，东南西北五个部分*/
        BorderLayout layout = new BorderLayout();

        /*格子布局，主要用来设置西、东、南三个部分的布局*/
        GridBagLayout gridBagLayout = new GridBagLayout();

        /*主要设置北部的布局*/
        FlowLayout flowLayout = new FlowLayout();

        /*设置初始窗口的一些性质*/
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.setContentPane(panel);
        frame.setLayout(layout);

        /*设置各个部分的panel的布局和大小*/
        headpanel.setLayout(flowLayout);
        footpanel.setLayout(gridBagLayout);
        centerpanel.setLayout(gridBagLayout);

        centerpanel.setPreferredSize(new Dimension(300, 0)); // 世界聊天面板
        footpanel.setPreferredSize(new Dimension(0, 40));

        //头部布局
        host_textfield = new JTextField("127.0.0.1");
        port_textfield = new JTextField("30402");
        name_textfield = new JTextField("匿名");
        host_textfield.setPreferredSize(new Dimension(70, 25));
        port_textfield.setPreferredSize(new Dimension(50, 25));
        name_textfield.setPreferredSize(new Dimension(50, 25));

        JLabel host_label = new JLabel("服务器IP:");
        JLabel port_label = new JLabel("端口:");
        JLabel name_label = new JLabel("昵称:");

        head_connect = new JButton("连接");
        head_exit = new JButton("退出");


        headpanel.add(host_label);
        headpanel.add(host_textfield);
        headpanel.add(port_label);
        headpanel.add(port_textfield);
        headpanel.add(name_label);
        headpanel.add(name_textfield);
        headpanel.add(head_connect);
        headpanel.add(head_exit);

        //底部布局
        foot_sendString = new JButton("发送");
        foot_userClear = new JButton("清空聊天消息");
        foot_sendFile = new JButton("发送文件"); // 新增发送文件按钮


        text_field = new JTextField();

        footpanel.add(text_field, new GridBagConstraints(0, 0, 1, 1, 10, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));
        footpanel.add(foot_sendString, new GridBagConstraints(1, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));
        footpanel.add(foot_sendFile, new GridBagConstraints(2, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));
        footpanel.add(foot_userClear, new GridBagConstraints(3, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));


        //中间布局
        JLabel userMsg_label = new JLabel("聊天窗口：");
        userMsgArea = new JTextPane();
        userMsgArea.setEditable(false);
        userTextScrollPane = new JScrollPane();
        userTextScrollPane.setViewportView(userMsgArea);
        userVertical = new JScrollBar(JScrollBar.VERTICAL);
        userVertical.setAutoscrolls(true);
        userTextScrollPane.setVerticalScrollBar(userVertical);

        centerpanel.add(userMsg_label, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        centerpanel.add(userTextScrollPane, new GridBagConstraints(0, 1, 1, 1, 100, 100,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        /*设置顶层布局*/
        panel.add(headpanel, "North");
        panel.add(footpanel, "South");
        panel.add(centerpanel, "Center");


        //将按钮事件全部注册到监听器
        //连接服务器
        head_connect.addActionListener(chatRoomClient);
        //往服务器发送消息
        foot_sendString.addActionListener(chatRoomClient);
        //退出聊天室
        head_exit.addActionListener(chatRoomClient);
        //清空世界聊天消息
        foot_userClear.addActionListener(chatRoomClient);
        //往服务器发送文件
        foot_sendFile.addActionListener(chatRoomClient);

        //窗口关闭事件
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(frame, "确定关闭聊天室界面?", "提示",
                        JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    if (e.getWindow() == frame) {
                        frame.dispose();
                        if (TCPClientFinal.channel != null && !TCPClientFinal.channel.isOpen()) {
                            chatRoomClient.exit(); //优雅的关闭~
                        }
                        System.exit(0);
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
        });

        //聊天信息输入框的监听回车按钮事件
        text_field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {

                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    if (TCPClientFinal.channel != null && !TCPClientFinal.channel.isOpen()) {
                        JOptionPane.showMessageDialog(frame, "请先连接服务器进入聊天室！", "提示", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    String text = text_field.getText();
                    if (text != null && !text.isEmpty()) {
                        String formatMsg = "<id>" + TCPClientFinalForGUI.socketId + "</id>"
                                + "<name>" + TCPClientFinalForGUI.name + "</name>"
                                + "<msg>" + text + "</msg>"
                                + "<time>" + TCPClientFinalForGUI.format(new Date()) + "</time>";
                        chatRoomClient.send(formatMsg);
                        text_field.setText("");
                    }
                }
            }
        });

        //窗口显示
        frame.setVisible(true);

        String name = JOptionPane.showInputDialog("请输入聊天所用昵称：");
        if (name != null && !name.isEmpty()) { // 如果弹窗的昵称不为空，则设置昵称
            name_textfield.setText(name);
        }
    }

}
