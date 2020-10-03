package com.github.dxahtepb.etcdidea.view

import com.github.dxahtepb.etcdidea.model.EtcdKvEntries
import java.util.Vector
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableModel

private val COLUMNS = Vector(listOf("key", "value"))

fun createTableModel(entries: EtcdKvEntries): TableModel {
    val rows = entries.etcdKeyValues.map { Vector(listOf(it.key, it.value)) }
    return DefaultTableModel(Vector(rows), COLUMNS)
}
