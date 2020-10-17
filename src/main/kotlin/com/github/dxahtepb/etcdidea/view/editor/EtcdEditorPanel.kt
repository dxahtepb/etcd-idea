package com.github.dxahtepb.etcdidea.view.editor

import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.service.EtcdService
import com.github.dxahtepb.etcdidea.view.actions.AddKeyAction
import com.github.dxahtepb.etcdidea.view.actions.DeleteKeyAction
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.project.Project
import com.intellij.ui.AnActionButton
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ListSelectionModel

class EtcdEditorPanel(
    private val project: Project,
    private val configuration: EtcdServerConfiguration,
    private val etcdService: EtcdService
) {
    private val rootPanel: JPanel

    private lateinit var resultsModel: EtcdTableModel
    private lateinit var resultTable: JBTable

    init {
        rootPanel = JPanel(BorderLayout()).apply {
            add(createTablePanel(), BorderLayout.CENTER)
        }
        updateResults()
    }

    fun getContent() = rootPanel

    private fun createTablePanel(): JComponent {
        resultsModel = EtcdTableModel()
        resultTable = JBTable(resultsModel).apply {
            dragEnabled = false
            tableHeader.reorderingAllowed = false
            columnSelectionAllowed = false
        }
        resultTable.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
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
        AddKeyDialogWindow(project, configuration).show()
        updateResults()
    }

    fun deleteSelectedKey() {
        val selectedRow = resultTable.selectedRow
        if (selectedRow < 0) return
        val selectedKey = resultTable.getValueAt(selectedRow, 0) as String
        etcdService.deleteEntry(configuration, selectedKey)
        updateResults()
    }

    private fun updateResults() {
        val entries = etcdService.listAllEntries(configuration)
        resultsModel.setDataVector(entries)
    }

    fun isRowSelected() = resultTable.selectedRow != -1
}
