package com.coffeechain;

import com.coffeechain.ui.LoginFrame;
import com.coffeechain.util.AppFonts;

import javax.swing.*;

/**
 * Điểm khởi chạy của ứng dụng desktop Swing.
 * Hiện tại app luôn mở màn hình đăng nhập {@link LoginFrame} đầu tiên.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        System.out.println(AppFonts.regular(14f).canDisplayUpTo("QUẢN TRỊ HỆ THỐNG PHỤNG LỘC"));
    }
}
