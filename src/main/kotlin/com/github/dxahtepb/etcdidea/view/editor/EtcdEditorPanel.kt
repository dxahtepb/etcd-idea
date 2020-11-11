package com.github.dxahtepb.etcdidea.view.editor

import com.github.dxahtepb.etcdidea.model.EtcdKeyValue
import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.service.EtcdService
import com.github.dxahtepb.etcdidea.view.SingleSelectionTable
import com.github.dxahtepb.etcdidea.view.addCenter
import com.github.dxahtepb.etcdidea.view.addEast
import com.github.dxahtepb.etcdidea.view.addNorth
import com.github.dxahtepb.etcdidea.view.addWest
import com.github.dxahtepb.etcdidea.view.editor.actions.AddKeyAction
import com.github.dxahtepb.etcdidea.view.editor.actions.DeleteKeyAction
import com.github.dxahtepb.etcdidea.view.editor.actions.EditKeyAction
import com.github.dxahtepb.etcdidea.view.editor.actions.RefreshTableAction
import com.github.dxahtepb.etcdidea.view.getScrollComponent
import com.github.dxahtepb.etcdidea.view.withNoBorder
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class EtcdEditorPanel(
    private val project: Project,
    private val configuration: EtcdServerConfiguration,
    private val etcdService: EtcdService
) {
    private val rootPanel: JPanel

    private lateinit var resultsModel: EtcdTableModel
    private lateinit var resultTable: JBTable
    private lateinit var searchPrefixField: JBTextField

    init {
        rootPanel = JPanel(BorderLayout()).apply {
            addNorth(createToolbarPanel())
            addCenter(createTablePanel())
        }
        updateResults()
    }

    fun getContent() = rootPanel

    private fun createTablePanel(): JComponent {
        resultsModel = EtcdTableModel()
        resultTable = SingleSelectionTable(resultsModel)
        return JPanel(BorderLayout()).apply {
            addCenter(resultTable.getScrollComponent().withNoBorder())
        }
    }

    private fun createToolbarPanel(): JComponent {
        return JPanel(BorderLayout()).apply {
            addWest(createKeyManagementToolbarPanel())
            addEast(createSearchToolbarPanel())
        }
    }

    private fun createKeyManagementToolbarPanel(): JComponent {
        val actionGroup = DefaultActionGroup("EtcdKeyManagementActions", false).also {
            it.add(AddKeyAction(this))
            it.add(DeleteKeyAction(this))
            it.add(EditKeyAction(this))
            it.addSeparator()
            it.add(RefreshTableAction(this))
        }
        val actionToolbar = ActionManager.getInstance().createActionToolbar("EtcdKeyManagement", actionGroup, true)
            .apply {
                layoutPolicy = ActionToolbar.AUTO_LAYOUT_POLICY
            }
        return actionToolbar.component.apply {
            isOpaque = false
            border = JBUI.Borders.empty()
        }
    }

    private fun createSearchToolbarPanel(): JComponent {
        searchPrefixField = JBTextField("", 19)
        return JPanel(FlowLayout()).apply {
            add(searchPrefixField)
            add(JButton("Search").apply { addActionListener { updateResults() } })
        }
    }

    fun showAddKeyDialog() {
        AddKeyDialogWindow(project, configuration).show()
        updateResults()
    }

    fun showEditKeyDialog() {
        val selectedRow = resultTable.selectedRow
        if (selectedRow < 0) return
        EditKeyDialogWindow(project, configuration, resultTable.getEtcdKv(selectedRow)).show()
        updateResults()
    }

    fun deleteSelectedKey() {
        val selectedRow = resultTable.selectedRow
        if (selectedRow < 0) return
        etcdService.deleteEntry(configuration, resultTable.getEtcdKv(selectedRow).key)
        updateResults()
    }

    fun updateResults() {
        val prefix = searchPrefixField.text
        val entries = if (prefix.isEmpty()) {
            etcdService.listAllEntries(configuration)
        } else {
            etcdService.listEntriesWithPrefix(configuration, prefix)
        }
        resultsModel.setDataVector(entries)
    }

    fun isRowSelected() = resultTable.selectedRow != -1
}

private fun JBTable.getEtcdKv(row: Int) = EtcdKeyValue(
    this.getValueAt(row, 0) as String,
    this.getValueAt(row, 1) as String
)
