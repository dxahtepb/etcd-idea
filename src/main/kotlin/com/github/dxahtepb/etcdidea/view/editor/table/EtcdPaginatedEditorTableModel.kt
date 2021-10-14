package com.github.dxahtepb.etcdidea.view.editor.table

import com.github.dxahtepb.etcdidea.model.EtcdKeyValue
import com.github.dxahtepb.etcdidea.model.EtcdKvEntries
import java.util.*
import javax.swing.table.AbstractTableModel
import kotlin.math.ceil
import kotlin.math.min

private val COLUMNS = Vector(listOf("key", "value"))

class EtcdPaginatedEditorTableModel(data: EtcdKvEntries) : AbstractTableModel() {
    var pageSize = 10
        set(value) {
            if (value == field) {
                return
            }
            val oldPageSize = field
            field = value
            pageOffset = oldPageSize * pageOffset / field
            fireTableDataChanged()
        }

    var pageOffset = 0
        set(value) {
            field = when {
                value < 0 -> 0
                value >= getPageCount() -> getPageCount() - 1
                else -> value
            }
            fireTableDataChanged()
        }

    var data = data
        set(value) {
            field = value
            pageOffset = 0
            fireTableDataChanged()
        }

    override fun getRowCount(): Int {
        return min(data.etcdKeyValues.size - pageOffset * pageSize, pageSize)
    }

    override fun getColumnCount(): Int {
        return COLUMNS.size
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        val realRow = rowIndex + pageOffset * pageSize
        return data.etcdKeyValues[realRow][columnIndex]
    }

    override fun getColumnName(columnIndex: Int): String {
        return COLUMNS[columnIndex]
    }

    override fun isCellEditable(row: Int, column: Int) = false

    fun getPageCount(): Int {
        return ceil(data.etcdKeyValues.size / pageSize.toDouble()).toInt()
    }

    fun getRealRowCount(): Int {
        return data.etcdKeyValues.size
    }
}

private operator fun EtcdKeyValue.get(index: Int): String? = when (index) {
    0 -> key
    1 -> value
    else -> null
}
