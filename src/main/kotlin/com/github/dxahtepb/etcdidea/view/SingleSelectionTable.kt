package com.github.dxahtepb.etcdidea.view

import com.intellij.ui.table.JBTable
import javax.swing.ListSelectionModel
import javax.swing.table.TableModel

class SingleSelectionTable(tableModel: TableModel) : JBTable(tableModel) {
    init {
        dragEnabled = false
        tableHeader.reorderingAllowed = false
        columnSelectionAllowed = false
        selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
    }
}
