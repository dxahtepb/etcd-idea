// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.dxahtepb.etcdidea.view;

import com.github.dxahtepb.etcdidea.service.EtcdService;

import javax.swing.*;
import java.util.Objects;

public class BrowserToolWindow {
    private final EtcdService etcdService;

    private JPanel myToolWindowContent;
    private JButton button;
    private JTextField textField;
    private JLabel label;

    public BrowserToolWindow(EtcdService etcdService) {
        this.etcdService = etcdService;
        button.addActionListener(e ->
                label.setText(Objects.toString(this.etcdService.listMembers(textField.getText()))));
    }

    public JPanel getContent() {
        return myToolWindowContent;
    }
}
