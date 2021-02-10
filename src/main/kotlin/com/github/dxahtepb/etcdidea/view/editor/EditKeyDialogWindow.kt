package com.github.dxahtepb.etcdidea.view.editor

import com.github.dxahtepb.etcdidea.invokeLaterOnEdt
import com.github.dxahtepb.etcdidea.model.EtcdKeyValue
import com.github.dxahtepb.etcdidea.model.EtcdKvRevisions
import com.github.dxahtepb.etcdidea.model.EtcdRevisionInfo
import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.service.EtcdService
import com.github.dxahtepb.etcdidea.service.EtcdWatcherHolder
import com.github.dxahtepb.etcdidea.uiDispatcher
import com.github.dxahtepb.etcdidea.view.*
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import com.intellij.uiDesigner.core.GridLayoutManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.util.*
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableModel

private const val TEXT_FIELD_SIZE = 18

class EditKeyDialogWindow(
    private val project: Project,
    private val hosts: EtcdServerConfiguration,
    private val keyValue: EtcdKeyValue
) : DialogWrapper(project, true) {

    private var watcher: EtcdWatcherHolder? = null
    private lateinit var keyField: JBTextField
    private lateinit var valueField: JBTextField
    private lateinit var revisionsTableModel: RevisionsTableModel
    private lateinit var startRevisionLabel: JBLabel
    private var modalityState: ModalityState

    init {
        title = "Edit Key"
        setOKButtonText("Submit")
        super.init()
        modalityState = ModalityState.current()
    }

    override fun createCenterPanel() =
        JPanel(BorderLayout()).apply {
            add(createKeyValuePanel(), BorderLayout.NORTH)
            add(createRevisionsPanel(), BorderLayout.CENTER)
        }

    private fun createKeyValuePanel(): JComponent {
        keyField = JBTextField(keyValue.key, TEXT_FIELD_SIZE).apply {
            isEnabled = false
            isEditable = false
        }
        valueField = JBTextField(keyValue.value, TEXT_FIELD_SIZE)

        return JPanel(GridLayoutManager(2, 2)).apply {
            add(JBLabel("Key:").apply { isEnabled = false }, gridConstraints(0, 0).leftAlignedLabel())
            add(keyField, gridConstraints(0, 1).textField())

            add(JBLabel("Value:"), gridConstraints(1, 0).leftAlignedLabel())
            add(valueField, gridConstraints(1, 1).textField())
        }
    }

    private fun createRevisionsPanel(): JComponent {
        revisionsTableModel = RevisionsTableModel()
        startRevisionLabel = JBLabel().apply {
            isVisible = false
        }
        return JPanel(BorderLayout()).apply {
            val loadButton = JButton("Load Revisions").apply {
                addActionListener {
                    watcher?.close()
                    revisionsTableModel.clear()
                    val modalityState = ModalityState.current()
                    GlobalScope.launch(uiDispatcher(modalityState)) {
                        watcher = EtcdService.getInstance(project)
                            .getRevisions(hosts, keyField.text) { revisions ->
                                invokeLaterOnEdt(modalityState) {
                                    revisionsTableModel.addRevisions(revisions)
                                }
                            }
                        startRevisionLabel.text = if (watcher != null) {
                            "Least revision shown: ${watcher?.startRevision}"
                        } else {
                            "Error while loading revisions"
                        }
                        startRevisionLabel.isVisible = true
                    }
                }
            }
            addNorth(
                JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                    add(loadButton)
                    add(startRevisionLabel)
                }
            )

            val revisionsTable = JBTable(revisionsTableModel).apply {
                emptyText.text = "Press \"Load Revisions\" to see historical revisions"
                dragEnabled = false
                tableHeader.reorderingAllowed = false
                columnSelectionAllowed = false
                selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
            }
            addCenter(
                JBScrollPane(revisionsTable).apply {
                    minimumSize = Dimension(120, 60)
                    preferredSize = Dimension(500, 150)
                }
            )
        }
    }

    override fun doValidate(): ValidationInfo? {
        if (keyField.text == "") {
            return ValidationInfo("Key should not be empty", keyField)
        }
        return null
    }

    override fun dispose() {
        watcher?.close()
        super.dispose()
    }

    fun getKv() = EtcdKeyValue(keyField.text, valueField.text)
}

private class RevisionsTableModel : DefaultTableModel(Vector<Vector<String>>(), columnNames) {
    private companion object {
        private val columnsModel = listOf(
            ColumnDescriptor("Value") { rev -> rev.kv.value },
            ColumnDescriptor("Version") { rev -> rev.version.toString() },
            ColumnDescriptor("Type") { rev -> rev.type.name },
            ColumnDescriptor("Create Revision") { rev -> rev.createRevision.toString() },
            ColumnDescriptor("Mod Revision") { rev -> rev.modRevision.toString() }
        )
        private val columnNames = Vector(columnsModel.map(ColumnDescriptor::name))
    }

    val revisions = mutableListOf<Vector<String>>()

    fun addRevisions(revisionInfos: EtcdKvRevisions) {
        revisionInfos.revisions
            .map { toColumn(it) }
            .reversed()
            .let { revisions.addAll(0, it) }
        setDataVector(Vector(revisions), columnNames)
    }

    fun clear() {
        revisions.clear()
        setDataVector(Vector<Vector<String>>(), columnNames)
    }

    override fun isCellEditable(row: Int, column: Int) = false

    private fun toColumn(rev: EtcdRevisionInfo) = Vector(columnsModel.map { it.extractor(rev) })

    data class ColumnDescriptor(val name: String, val extractor: (rev: EtcdRevisionInfo) -> String)
}
