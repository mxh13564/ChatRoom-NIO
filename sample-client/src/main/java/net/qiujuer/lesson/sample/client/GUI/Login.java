package net.qiujuer.lesson.sample.client.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @Author: Himit_ZH
 * @Date: 2020/7/10 17:31
 * @Description:
 */
class Login extends JFrame {
    protected JComboBox<String> comboBox;
    protected JTextField textField;
    public Login() {
        setTitle("登录认证");
        setSize(300, 400);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //获取窗口的第二层，将label放入
        ImageIcon icon=new ImageIcon(this.getClass().getResource("/HCODE.png").getPath());

        //设置图片缩略大小，并添加到面板布局中
        icon.setImage(icon.getImage().getScaledInstance(300, 400, Image.SCALE_DEFAULT ));
        JLabel lbBg = new JLabel(icon);
        lbBg.setBounds(0, 0, 300, 400);
        getLayeredPane().add(lbBg,new Integer(Integer.MIN_VALUE));

        //获取frame的顶层容器,并设置为透明
        JPanel top = (JPanel)getContentPane();
        top.setOpaque(false);

        JLabel label1 = new JLabel("HCODE在线聊天室");
        label1.setBounds(100, 20, 150, 30);
        add(label1);

        JLabel label3 = new JLabel("权限密令：");
        label3.setBounds(30, 150, 100, 30);
        add(label3);
        label3.setVisible(true);

        textField = new JTextField();
        textField.setBounds(100, 150, 130, 30);
        add(textField);
        textField.setVisible(true);

        JButton login = new JButton("进入");
        login.setBounds(80, 230, 130, 40);
        add(login);

        JButton exit = new JButton("退出");
        exit.setBounds(80, 300, 130, 40);
        add(exit);


        // 进入按钮监听事件
        login.addActionListener(TCPClientFinalForGUI.chatRoomClient);

        //退出按钮监听事件
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });
        setVisible(true);
    }
}