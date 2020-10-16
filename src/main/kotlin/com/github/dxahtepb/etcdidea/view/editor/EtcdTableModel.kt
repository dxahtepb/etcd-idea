package com.github.dxahtepb.etcdidea.view.editor

import com.github.dxahtepb.etcdidea.model.EtcdKvEntries
import java.util.Vector
import javax.swing.table.DefaultTableModel

private val COLUMNS = Vector(listOf("key", "value"))

class EtcdTableModel : DefaultTableModel() {
    fun setDataVector(entries: EtcdKvEntries) {
        val rows = entries.etcdKeyValues.map { Vector(listOf(it.key, it.value)) }
        setDataVector(Vector(rows), COLUMNS)
    }

    override fun isCellEditable(row: Int, column: Int) = false
}
