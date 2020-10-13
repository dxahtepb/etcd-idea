package com.github.dxahtepb.etcdidea.view

import com.github.dxahtepb.etcdidea.EtcdBundle
import com.github.dxahtepb.etcdidea.model.EtcdConnection
import com.github.dxahtepb.etcdidea.service.EtcdService
import com.github.dxahtepb.etcdidea.view.actions.AddKeyAction
import com.github.dxahtepb.etcdidea.view.actions.DeleteKeyAction
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.project.Project
import com.intellij.ui.AnActionButton
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class BrowserToolWindow(private val project: Project, private val etcdService: EtcdService) {
    @Suppress("MagicNumber")
    private val hostTextFieldSize = Dimension(353, 30)
    private val hostTextFieldDefaultText = "http://192.168.99.100:2379"
    private lateinit var hostTextField: JBTextField

    private lateinit var resultsModel: EtcdTableModel
    private lateinit var resultTable: JBTable

    private val rootPanel: JPanel

    init {
        rootPanel = JPanel(BorderLayout()).apply {
            add(createToolbarPanel(), BorderLayout.NORTH)
            add(createTablePanel(), BorderLayout.CENTER)
        }
    }

    fun getContent(): JComponent = rootPanel

    private fun createToolbarPanel(): JComponent {
        hostTextField = JBTextField(hostTextFieldDefaultText).apply { preferredSize = hostTextFieldSize }
        return JPanel(GridLayoutManager(1, 2)).apply {
            add(hostTextField, GridConstraints())
            add(
                JButton(EtcdBundle.getMessage("browser.toolwindow.button.list"))
                    .apply { addActionListener { updateResults() } },
                GridConstraints().apply { column = 1 }
            )
        }
    }

    private fun createTablePanel(): JComponent {
        resultsModel = EtcdTableModel()
        resultTable = JBTable(resultsModel).apply {
            dragEnabled = false
            tableHeader.reorderingAllowed = false
            columnSelectionAllowed = false
        }
        val resultTableDecorator = ToolbarDecorator.createDecorator(resultTable)
            .setToolbarPosition(ActionToolbarPosition.TOP)
            .setPanelBorder(JBUI.Borders.empty()).setScrollPaneBorder(JBUI.Borders.empty())
            .addExtraAction(AnActionButton.fromAction(AddKeyAction(this)))
            .addExtraAction(AnActionButton.fromAction(DeleteKeyAction(this)))
        return JPanel(BorderLayout()).apply {
            add(resultTableDecorator.createPanel(), BorderLayout.CENTER)
        }
    }

    fun showAddKeyDialog() {
        AddKeyDialogWindow(project, getCurrentConnection()).show()
        updateResults()
    }

    fun deleteSelectedKey() {
        val selectedRow = resultTable.selectedRow
        if (selectedRow < 0) return
        val selectedKey = resultTable.getValueAt(selectedRow, 0) as String
        etcdService.deleteEntry(getCurrentConnection(), selectedKey)
        updateResults()
    }

    private fun updateResults() {
        val entries = etcdService.listAllEntries(getCurrentConnection())
        resultsModel.setDataVector(entries)
    }

    fun isRowSelected() = resultTable.selectedRow != -1

    private fun getCurrentConnection() = EtcdConnection(hostTextField.text)
}
