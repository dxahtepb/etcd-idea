package com.github.dxahtepb.etcdidea.view.editor.table

import com.github.dxahtepb.etcdidea.EtcdBundle
import com.github.dxahtepb.etcdidea.view.addCenter
import com.github.dxahtepb.etcdidea.view.addLineEnd
import com.github.dxahtepb.etcdidea.view.addLineStart
import com.github.dxahtepb.etcdidea.view.editor.table.actions.FirstPageAction
import com.github.dxahtepb.etcdidea.view.editor.table.actions.LastPageAction
import com.github.dxahtepb.etcdidea.view.editor.table.actions.NextPageAction
import com.github.dxahtepb.etcdidea.view.editor.table.actions.PreviousPageAction
import com.github.dxahtepb.etcdidea.view.noOpaquePanel
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.ComboBox
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTable

class EtcdEditorTablePaginator(table: JTable) : JPanel(BorderLayout()) {

    private val rowsNumberPerPageComboBox = ComboBox<Int>(arrayOf(10, 15, 30, 100))
    private var currentPageChanging = false
    private val label = JLabel()

    private val tableModel = table.model as EtcdPaginatedEditorTableModel

    val control = PaginationControl()

    init {
        addLineStart(createPaginationToolbarPanel())
        addLineEnd(createPageSizeComboBoxPanel())
        setUp(table)
    }

    private fun createPageSizeComboBoxPanel() = noOpaquePanel().apply {
        rowsNumberPerPageComboBox.apply {
            isEditable = true
            addActionListener {
                if (selectedItem is Int) {
                    tableModel.pageSize = selectedItem as Int
                    updateInfo()
                }
            }
        }.also { addCenter(it) }
    }

    private fun createPaginationToolbarPanel() = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
        DefaultActionGroup("EtcdKeyManagementActionsPrevious", false).apply {
            add(FirstPageAction())
            add(PreviousPageAction())
        }.also { add(createToolbarPart(it)) }
        add(label)
        DefaultActionGroup("EtcdKeyManagementActionsNext", false).apply {
            add(NextPageAction())
            add(LastPageAction())
        }.also { add(createToolbarPart(it)) }
    }

    private fun createToolbarPart(actionGroup: ActionGroup): JComponent {
        return ActionManager.getInstance()
            .createActionToolbar(PAGINATION_TOOLBAR_PLACES, actionGroup, true)
            .also { it.setTargetComponent(this) }
            .component.apply {
                isOpaque = false
                border = JBUI.Borders.empty()
            }
    }

    private fun setUp(table: JTable) {
        // Table-ViewPort-ScrollPane-JPanel
        val parentPanel = table.parent?.parent?.parent as? JPanel ?: return
        parentPanel.add(this, BorderLayout.PAGE_END)
        parentPanel.revalidate()

        tableModel.addTableModelListener {
            updateInfo()
        }
    }

    private fun updateInfo() {
        if (currentPageChanging) return
        currentPageChanging = true
        val skippedRows = tableModel.pageOffset * tableModel.pageSize
        label.text = EtcdBundle.getMessage(
            "editor.pagination.rowsShown",
            skippedRows + 1,
            skippedRows + tableModel.rowCount,
            tableModel.getRealRowCount()
        )
        currentPageChanging = false
    }

    inner class PaginationControl {
        fun nextPage() {
            tableModel.pageOffset++
            updateInfo()
        }

        fun previousPage() {
            tableModel.pageOffset--
            updateInfo()
        }

        fun firstPage() {
            tableModel.pageOffset = 0
            updateInfo()
        }

        fun lastPage() {
            tableModel.pageOffset = tableModel.getPageCount() - 1
            updateInfo()
        }
    }

    companion object {
        const val PAGINATION_TOOLBAR_PLACES = "EtcdEditorTablePaginator"
    }
}
