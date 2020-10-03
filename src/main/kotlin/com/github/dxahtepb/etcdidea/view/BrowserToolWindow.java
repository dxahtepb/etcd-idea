package com.github.dxahtepb.etcdidea.view;

import com.github.dxahtepb.etcdidea.model.EtcdKvEntries;
import com.github.dxahtepb.etcdidea.service.EtcdService;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;

public class BrowserToolWindow {
    private final EtcdService etcdService;

    private JPanel myToolWindowContent;
    private JTextField textField;
    private JButton getAllKeysButton;
    private JPanel tablePanel;

    public BrowserToolWindow(EtcdService etcdService) {
        this.etcdService = etcdService;
        initActionListeners();
    }

    public JPanel getContent() {
        return myToolWindowContent;
    }

    private void initActionListeners() {
        getAllKeysButton.addActionListener(this::buttonListener);
    }

    private void buttonListener(ActionEvent event) {
        EtcdKvEntries entries = etcdService.listAllEntries(textField.getText());
        JTable table = new JBTable(EtcdTableModelKt.createTableModel(entries));
        displayResult(table);
    }

    private void displayResult(JComponent tableView) {
        tablePanel.invalidate();
        tablePanel.removeAll();
        tablePanel.add(new JBScrollPane(tableView));
        tablePanel.validate();
    }
}
