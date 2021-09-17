package com.github.dxahtepb.etcdidea.view.browser

import com.github.dxahtepb.etcdidea.UI_DISPATCHER
import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.persistence.EtcdConfigurationStateComponent
import com.github.dxahtepb.etcdidea.service.EtcdService
import com.github.dxahtepb.etcdidea.service.auth.CredentialsService
import com.github.dxahtepb.etcdidea.service.auth.PasswordKey
import com.github.dxahtepb.etcdidea.vfs.EtcdDummyVirtualFile
import com.github.dxahtepb.etcdidea.view.SingleSelectionTable
import com.github.dxahtepb.etcdidea.view.addCenter
import com.github.dxahtepb.etcdidea.view.addNorth
import com.github.dxahtepb.etcdidea.view.browser.actions.AddServerAction
import com.github.dxahtepb.etcdidea.view.browser.actions.CheckHealthAction
import com.github.dxahtepb.etcdidea.view.browser.actions.DeleteServerAction
import com.github.dxahtepb.etcdidea.view.browser.actions.EditServerAction
import com.github.dxahtepb.etcdidea.view.getScrollComponent
import com.github.dxahtepb.etcdidea.view.withNoBorder
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.PopupHandler
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.JBUI.Borders.customLine
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION

class BrowserToolWindow(
    private val project: Project,
    private val etcdService: EtcdService,
    private val etcdState: EtcdConfigurationStateComponent
) {
    private lateinit var treeModel: DefaultTreeModel
    private lateinit var myTree: Tree

    private lateinit var statsModel: EtcdStatusTableModel

    private val rootPanel: JPanel

    init {
        rootPanel = JPanel(BorderLayout()).apply {
            addNorth(createToolbarPanel())
            addCenter(
                OnePixelSplitter(true, 0.6f).apply {
                    firstComponent = createTreePanel()
                    secondComponent = createTablePanel().apply {
                        border = customLine(JBUI.CurrentTheme.ToolWindow.borderColor(), 1, 0, 0, 0)
                    }
                }
            )
        }
        fillServers()
    }

    fun getContent(): JComponent = rootPanel

    private fun createToolbarPanel(): JComponent {
        val actionGroup = DefaultActionGroup("BrowserActionGroup", false).apply {
            add(AddServerAction(::insertNewConfiguration))
            addSeparator()
            add(EditServerAction(::editSelectedConfiguration, ::isTreeSelected))
            add(CheckHealthAction(::checkHealthSelectedConfiguration, ::isTreeSelected))
        }
        val actionToolbar = ActionManager.getInstance().createActionToolbar("EtcdBrowser", actionGroup, true)
        return JPanel(BorderLayout()).apply {
            addCenter(actionToolbar.component)
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

        object : PopupHandler() {
            val actionGroup = DefaultActionGroup("BrowserPopupActionGroup", true).apply {
                add(EditServerAction(::editSelectedConfiguration, ::isTreeSelected))
                add(CheckHealthAction(::checkHealthSelectedConfiguration, ::isTreeSelected))
                addSeparator()
                add(DeleteServerAction(::deleteSelectedConfiguration, ::isTreeSelected))
            }

            override fun invokePopup(comp: Component?, x: Int, y: Int) {
                val popupMenu = ActionManager.getInstance()
                    .createActionPopupMenu(ActionPlaces.UNKNOWN, actionGroup)
                popupMenu.component.show(comp, x, y)
            }
        }.let { myTree.addMouseListener(it) }

        myTree.selectionModel.addTreeSelectionListener {
            val selectedConfiguration = getCurrentConnection()
            if (selectedConfiguration != null) {
                updateStatsTable(selectedConfiguration)
            } else {
                clearStatsTable()
            }
        }

        return JPanel(BorderLayout()).apply {
            addCenter(myTree.getScrollComponent().withNoBorder())
        }
    }

    private fun addConfigurationToTree(configuration: EtcdServerConfiguration) {
        val childNode = DefaultMutableTreeNode(configuration, false)
        val root = treeModel.root as DefaultMutableTreeNode
        treeModel.insertNodeInto(childNode, root, root.childCount)
        myTree.scrollPathToVisible(TreePath(childNode.path))
    }

    private fun insertNewConfiguration(configuration: EtcdServerConfiguration) {
        etcdState.addEtcdConfiguration(configuration)
        addConfigurationToTree(configuration)
    }

    private fun deleteSelectedConfiguration() {
        val nodeToRemove = myTree.selectionPath?.lastPathComponent as? DefaultMutableTreeNode ?: return
        val configuration = nodeToRemove.userObject as EtcdServerConfiguration
        treeModel.removeNodeFromParent(nodeToRemove)
        etcdState.removeConfiguration(configuration)
        CredentialsService.instance.forgetPassword(PasswordKey(configuration.id))
    }

    private fun checkHealthSelectedConfiguration() {
        val selected = myTree.selectionPath?.lastPathComponent as? DefaultMutableTreeNode ?: return
        val configuration = selected.userObject as EtcdServerConfiguration
        GlobalScope.launch(UI_DISPATCHER) {
            etcdService.getAllAlarms(configuration)?.let { alarms ->
                if (alarms.alarms.isEmpty()) {
                    Notifications.Bus.notify(
                        Notification(
                            "Etcd Browser",
                            "Etcd health check",
                            "Etcd cluster has no raised alarms",
                            NotificationType.INFORMATION
                        )
                    )
                } else {
                    Notifications.Bus.notify(
                        Notification(
                            "Etcd Browser",
                            "Etcd health check",
                            "Etcd cluster has ${alarms.alarms.size} alarms",
                            NotificationType.WARNING
                        )
                    )
                }
            }
        }
    }

    private fun editSelectedConfiguration() {
        val nodeToEdit = myTree.selectionPath?.lastPathComponent as? DefaultMutableTreeNode ?: return
        val oldConfiguration = nodeToEdit.userObject as EtcdServerConfiguration

        val configurationDialog = ConfigureServerDialogWindow(project, oldConfiguration)
        if (configurationDialog.showAndGet()) {
            val newConfiguration = configurationDialog.getConfiguration()
            etcdState.upsertConfiguration(newConfiguration)
            nodeToEdit.userObject = newConfiguration
        }
    }

    private fun createTablePanel(): JComponent {
        statsModel = EtcdStatusTableModel()
        return JPanel(BorderLayout()).apply {
            addCenter(SingleSelectionTable(statsModel).getScrollComponent().withNoBorder())
        }
    }

    private fun updateStatsTable(configuration: EtcdServerConfiguration) {
        GlobalScope.launch(UI_DISPATCHER) {
            etcdService.getMemberStatus(configuration)?.let {
                statsModel.setDataVector(it)
            }
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

    private fun fillServers() {
        etcdState.getConfigurations().forEach(::addConfigurationToTree)
    }
}
