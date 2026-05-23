package com.coffeechain.ui.common;

import com.coffeechain.service.AuthApiClient;
import java.awt.Cursor;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class AppHeaderPanel extends RoundedPanel {
  public AppHeaderPanel(
      JFrame owner,
      AuthApiClient authApiClient,
      String titleText,
      String subtitleText,
      int subtitleWidth,
      String cafeIconPath,
      String avatarIconPath,
      String logoutIconPath) {
    super(10, UiTheme.CARD_BG);
    setLayout(null);

    JLabel title = new JLabel(titleText);
    title.setForeground(UiTheme.PRIMARY);
    title.setFont(UiTheme.bold(15));
    title.setBounds(20, 9, 390, 25);
    add(title);

    JLabel logoLabel = new JLabel(IconLoader.svg(cafeIconPath, 30, 30));
    logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
    logoLabel.setBounds(270, 1, 36, 36);
    add(logoLabel);

    JLabel subtitle = new JLabel(subtitleText);
    subtitle.setForeground(UiTheme.TEXT_DARK);
    subtitle.setFont(UiTheme.regular(15));
    subtitle.setBounds(20, 42, subtitleWidth, 25);
    add(subtitle);

    JLabel avatar = new JLabel(IconLoader.svg(avatarIconPath, 28, 28));
    avatar.setHorizontalAlignment(SwingConstants.CENTER);
    avatar.setBounds(486, 24, 30, 30);
    add(avatar);

    JLabel logout = new JLabel(IconLoader.svg(logoutIconPath, 90, 36));
    logout.setBounds(528, 20, 90, 36);
    logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    logout.addMouseListener(
        new java.awt.event.MouseAdapter() {
          @Override
          public void mouseClicked(java.awt.event.MouseEvent e) {
            FrameNavigator.logout(owner, authApiClient);
          }
        });
    add(logout);
  }
}
