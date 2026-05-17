/**
 * Icon tự vẽ bằng Java2D cho các component Swing.
 * Chỉ giữ các icon đang được dùng trực tiếp trên UI, không dùng làm fallback cho asset thiếu.
 *
 * Quy ước chung:
 * - Constructor nhận Color hoặc trạng thái để cấu hình màu/kiểu icon.
 * - getIconWidth() và getIconHeight() trả kích thước cố định để Swing layout đúng.
 * - paintIcon(...) là hàm Swing gọi khi cần vẽ icon lên JLabel/JButton/JCheckBox.
 */
package com.coffeechain.ui.common.icons;
