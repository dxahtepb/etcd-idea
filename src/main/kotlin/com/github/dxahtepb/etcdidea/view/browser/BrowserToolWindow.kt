package com.github.dxahtepb.etcdidea.view.browser

import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.service.EtcdService
import com.github.dxahtepb.etcdidea.vfs.EtcdDummyVirtualFile
import com.github.dxahtepb.etcdidea.view.browser.actions.AddServerAction
import com.github.dxahtepb.etcdidea.view.browser.actions.DeleteServerAction
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.ListSelectionModel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION

class BrowserToolWindow(private val project: Project, private val etcdService: EtcdService) {
    private lateinit var treeModel: DefaultTreeModel
    private lateinit var myTree: Tree

    private lateinit var statsModel: EtcdStatusTableModel
    private lateinit var statsTable: JBTable

    private val rootPanel: JPanel

    init {
        rootPanel = JPanel(BorderLayout()).apply {
            add(createToolbarPanel(), BorderLayout.NORTH)
            val splitterBorder = JBUI.Borders.customLine(JBUI.CurrentTheme.ToolWindow.borderColor(), 1, 0, 0, 0)
            add(
                OnePixelSplitter(true, 0.6f).apply {
                    firstComponent = createTreePanel()
                    secondComponent = createTablePanel().apply {
                        border = splitterBorder
                    }
                },
                BorderLayout.CENTER
            )
        }
    }

    fun getContent(): JComponent = rootPanel

    private fun createToolbarPanel(): JComponent {
        val actionGroup = DefaultActionGroup("BrowserActionGroup", false).apply {
            add(AddServerAction(::insertNewConfiguration))
            add(DeleteServerAction(::deleteSelectedConfiguration, ::isTreeSelected))
        }
        val actionToolbar = ActionManager.getInstance().createActionToolbar("EtcdBrowser", actionGroup, true)
        return JPanel(BorderLayout()).apply {
            add(actionToolbar.component, BorderLayout.CENTER)
        }
    }

    private fun createTreePanel(): JComponent {
        treeModel = DefaultTreeModel(DefaultMutableTreeNode())
        myTree = Tree(treeModel).apply {
            isEditable = false
            isRootVisible = false
            emptyText.text = "Add server configuration"
            selectionModel.selectionMode = SINGLE_TREE_SELECTION
        }

        object : DoubleClickListener() {
            @SuppressWarnings("ReturnCount")
            override fun onDoubleClick(event: MouseEvent): Boolean {
                if (event.source !is JTree || !isTreeSelected()) return false
                val configuration = getCurrentConnection() ?: return false
                FileEditorManager.getInstance(project)
                    .openFile(EtcdDummyVirtualFile(configuration), true)
                return true
            }
        }.installOn(myTree)

        myTree.selectionModel.addTreeSelectionListener {
            val selectedConfiguration = getCurrentConnection()
            if (selectedConfiguration != null) {
                updateStatsTable(selectedConfiguration)
            } else {
                clearStatsTable()
            }
        }

        return JPanel(BorderLayout()).apply {
            add(JBScrollPane(myTree).withNoBorder(), BorderLayout.CENTER)
        }
    }

    private fun insertNewConfiguration(configuration: EtcdServerConfiguration) {
        val childNode = DefaultMutableTreeNode(configuration, false)
        val root = treeModel.root as DefaultMutableTreeNode
        treeModel.insertNodeInto(childNode, root, root.childCount)
        myTree.scrollPathToVisible(TreePath(childNode.path))
    }

    private fun deleteSelectedConfiguration() {
        val nodeToRemove = myTree.selectionPath?.lastPathComponent as? MutableTreeNode ?: return
        treeModel.removeNodeFromParent(nodeToRemove)
    }

    private fun createTablePanel(): JComponent {
        statsModel = EtcdStatusTableModel()
        statsTable = JBTable(statsModel).apply {
            dragEnabled = false
            tableHeader.reorderingAllowed = false
            columnSelectionAllowed = false
        }
        statsTable.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
        return JPanel(BorderLayout()).apply {
            add(JBScrollPane(statsTable).withNoBorder(), BorderLayout.CENTER)
        }
    }

    private fun updateStatsTable(configuration: EtcdServerConfiguration) {
        etcdService.getMemberStatus(configuration)?.let {
            statsModel.setDataVector(it)
        }
    }

    private fun clearStatsTable() {
        if (this::statsModel.isInitialized) {
            statsModel.clear()
        }
    }

    fun isTreeSelected() = myTree.selectionCount != 0

    private fun getCurrentConnection(): EtcdServerConfiguration? {
        val node = myTree.selectionPath?.lastPathComponent as? DefaultMutableTreeNode
        return node?.userObject as? EtcdServerConfiguration
    }
}

private fun JComponent.withNoBorder() = this.apply { border = JBUI.Borders.empty() }
