package com.github.dxahtepb.etcdidea.view.browser

import com.github.dxahtepb.etcdidea.EtcdBundle
import com.github.dxahtepb.etcdidea.model.EtcdMemberStatus
import java.util.Vector
import javax.swing.table.DefaultTableModel

private val COLUMNS = Vector(
    listOf(
        EtcdBundle.getMessage("browser.toolwindow.stats.property"),
        EtcdBundle.getMessage("browser.toolwindow.stats.value")
    )
)

class EtcdStatusTableModel : DefaultTableModel() {
    fun setDataVector(status: EtcdMemberStatus) {
        val rows = listOf(
            listOf("version", status.version),
            listOf("leader", status.leader.toString()),
            listOf("dbSize", status.size.toString()),
            listOf("raftIndex", status.raftIndex.toString()),
            listOf("raftTerm", status.raftTerm.toString())
        ).map { Vector(it) }
        setDataVector(Vector(rows), COLUMNS)
    }

    fun clear() {
        setDataVector(Vector<Vector<String>>(), COLUMNS)
    }

    override fun isCellEditable(row: Int, column: Int) = false
}
