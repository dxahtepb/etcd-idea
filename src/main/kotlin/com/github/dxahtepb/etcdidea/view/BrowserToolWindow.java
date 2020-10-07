package com.github.dxahtepb.etcdidea.view;

import com.github.dxahtepb.etcdidea.model.EtcdConnection;
import com.github.dxahtepb.etcdidea.model.EtcdKvEntries;
import com.github.dxahtepb.etcdidea.service.EtcdService;
import com.github.dxahtepb.etcdidea.view.actions.AddKeyAction;
import com.github.dxahtepb.etcdidea.view.actions.DeleteKeyAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

public class BrowserToolWindow {
    private final EtcdService etcdService;
    private final Project project;

    private JPanel myToolWindowContent;

    private JTextField textField;
    private JButton getAllKeysButton;

    private JPanel tablePanel;
    private JPanel toolbarPanel;
    private EtcdTableModel resultsModel;
    private JBTable resultTable;

    public BrowserToolWindow(Project project, EtcdService etcdService) {
        this.etcdService = etcdService;
        this.project = project;
        initResultTable();
        initToolBar();
        initActionListeners();
    }

    public JPanel getContent() {
        return myToolWindowContent;
    }

    private void initActionListeners() {
        getAllKeysButton.addActionListener(this::buttonListener);
    }

    private void initToolBar() {
        toolbarPanel.setLayout(new BorderLayout());
        DefaultActionGroup actionResultGroup = new DefaultActionGroup("EtcdResultGroup", true);
        actionResultGroup.add(new AddKeyAction(this));
        actionResultGroup.add(new DeleteKeyAction(this));
        JComponent actionToolBarComponent = ActionManager.getInstance()
                .createActionToolbar("EtcdKvActionGroup", actionResultGroup, true)
                .getComponent();
        toolbarPanel.add(actionToolBarComponent, BorderLayout.WEST);
        toolbarPanel.setVisible(false);
    }

    private void initResultTable() {
        this.resultsModel = new EtcdTableModel();
        this.resultTable = new JBTable(resultsModel);
        resultTable.setDragEnabled(false);
        resultTable.getTableHeader().setReorderingAllowed(false);
        resultTable.setColumnSelectionAllowed(false);
    }

    private void buttonListener(ActionEvent event) {
        updateResults();
    }

    public void showAddKeyDialog() {
        new AddKeyDialogWindow(project, getCurrentConnection()).show();
        updateResults();
    }

    public void deleteSelectedKey() {
        int selectedRow = resultTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        String selectedKey = (String) resultTable.getValueAt(selectedRow, 0);
        etcdService.deleteEntry(getCurrentConnection(), selectedKey);
        updateResults();
    }

    public boolean isRowSelected() {
        return resultTable.getSelectedRow() != -1;
    }

    private void updateResults() {
        EtcdKvEntries entries = etcdService.listAllEntries(getCurrentConnection());
        resultsModel.setDataVector(entries);
        displayResult(resultTable);
    }

    private void displayResult(JComponent tableView) {
        tablePanel.invalidate();
        tablePanel.removeAll();
        tablePanel.add(new JBScrollPane(tableView));
        tablePanel.validate();
        toolbarPanel.setVisible(true);
    }

    private EtcdConnection getCurrentConnection() {
        return new EtcdConnection(textField.getText());
    }
}
