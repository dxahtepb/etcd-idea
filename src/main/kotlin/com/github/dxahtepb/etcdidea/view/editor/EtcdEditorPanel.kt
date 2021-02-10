package com.github.dxahtepb.etcdidea.view.editor

import com.github.dxahtepb.etcdidea.UI_DISPATCHER
import com.github.dxahtepb.etcdidea.model.EtcdKeyValue
import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.service.EtcdService
import com.github.dxahtepb.etcdidea.view.*
import com.github.dxahtepb.etcdidea.view.editor.actions.AddKeyAction
import com.github.dxahtepb.etcdidea.view.editor.actions.DeleteKeyAction
import com.github.dxahtepb.etcdidea.view.editor.actions.EditKeyAction
import com.github.dxahtepb.etcdidea.view.editor.actions.RefreshTableAction
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import com.intellij.util.text.nullize
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JComponent
import javax.swing.JPanel

class EtcdEditorPanel(
    private val project: Project,
    private val configuration: EtcdServerConfiguration,
    private val etcdService: EtcdService
) {
    private val rootPanel: JPanel = JPanel(BorderLayout())

    private lateinit var resultsModel: EtcdTableModel
    private lateinit var resultTable: JBTable
    private lateinit var searchPrefixField: JBTextField

    init {
        rootPanel.apply {
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
        actionToolbar.setTargetComponent(rootPanel)
        return actionToolbar.component.apply {
            isOpaque = false
            border = JBUI.Borders.empty()
        }
    }

    private fun createSearchToolbarPanel(): JComponent {
        searchPrefixField = JBTextField("", 19).apply {
            emptyText.text = "<Key prefix>"
            addActionListener {
                updateResults()
            }
        }
        return JPanel(FlowLayout()).apply {
            add(searchPrefixField)
        }
    }

    fun showAddKeyDialog() {
        AddKeyDialogWindow(project).also {
            if (it.showAndGet()) {
                // todo: don't use GlobalScope, but rather local scope for panel
                GlobalScope.launch(UI_DISPATCHER) {
                    etcdService.putNewEntry(configuration, it.getKv())
                    updateResults()
                }
            }
        }
    }

    fun showEditKeyDialog() {
        val selectedRow = resultTable.selectedRow
        if (selectedRow < 0) return
        EditKeyDialogWindow(project, configuration, resultTable.getEtcdKv(selectedRow)).also {
            if (it.showAndGet()) {
                GlobalScope.launch(UI_DISPATCHER) {
                    etcdService.putNewEntry(configuration, it.getKv())
                    updateResults()
                }
            }
        }
    }

    fun deleteSelectedKey() {
        val selectedRow = resultTable.selectedRow
        if (selectedRow < 0) return
        GlobalScope.launch(UI_DISPATCHER) {
            etcdService.deleteEntry(configuration, resultTable.getEtcdKv(selectedRow).key)
            updateResults()
        }
    }

    fun updateResults() {
        val prefix = searchPrefixField.text
        GlobalScope.launch(UI_DISPATCHER) {
            val entries = etcdService.listEntries(configuration, prefix.nullize())
            resultsModel.setDataVector(entries)
        }
    }

    fun isRowSelected() = resultTable.selectedRow != -1
}

private fun JBTable.getEtcdKv(row: Int) = EtcdKeyValue(
    this.getValueAt(row, 0) as String,
    this.getValueAt(row, 1) as String
)
