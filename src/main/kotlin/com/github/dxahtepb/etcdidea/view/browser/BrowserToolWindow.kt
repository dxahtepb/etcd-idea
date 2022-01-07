package com.github.dxahtepb.etcdidea.view.browser

import com.github.dxahtepb.etcdidea.EtcdBundle
import com.github.dxahtepb.etcdidea.UI_DISPATCHER
import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.persistence.EtcdConfigurationStateComponent
import com.github.dxahtepb.etcdidea.service.EtcdService
import com.github.dxahtepb.etcdidea.service.auth.CredentialsService
import com.github.dxahtepb.etcdidea.service.auth.PasswordKey
import com.github.dxahtepb.etcdidea.vfs.EtcdDummyVirtualFile
import com.github.dxahtepb.etcdidea.view.*
import com.github.dxahtepb.etcdidea.view.browser.actions.*
import com.github.dxahtepb.etcdidea.view.browser.model.EtcdBrowserTreeNode
import com.github.dxahtepb.etcdidea.view.browser.model.EtcdBrowserTreeNodeUserObject
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
            add(AddServerAction())
            addSeparator()
            add(EditServerAction())
            add(CheckHealthAction())
            add(WatchStatsToggleAction())
        }
        return JPanel(BorderLayout()).apply {
            addCenter(
                ActionManager.getInstance()
                    .createActionToolbar("EtcdBrowser", actionGroup, true)
                    .also { it.setTargetComponent(this) }
                    .component
            )
        }
    }

    private fun createTreePanel(): JComponent {
        treeModel = DefaultTreeModel(EtcdBrowserTreeNode())
        myTree = Tree(treeModel).apply {
            isEditable = false
            isRootVisible = false
            emptyText.text = EtcdBundle.getMessage("browser.toolwindow.configurationsEmptyText")
            selectionModel.selectionMode = SINGLE_TREE_SELECTION
        }

        object : DoubleClickListener() {
            @SuppressWarnings("ReturnCount")
            override fun onDoubleClick(event: MouseEvent): Boolean {
                if (event.source !is JTree || !isTreeSelected()) return false
                val configuration = getCurrentConfiguration()?.etcdServerConfiguration ?: return false
                FileEditorManager.getInstance(project)
                    .openFile(EtcdDummyVirtualFile(configuration), true)
                return true
            }
        }.installOn(myTree)

        object : PopupHandler() {
            val actionGroup = DefaultActionGroup("BrowserPopupActionGroup", true).apply {
                add(EditServerAction())
                add(CheckHealthAction())
                addSeparator()
                add(DeleteServerAction())
            }

            override fun invokePopup(comp: Component?, x: Int, y: Int) {
                val popupMenu = ActionManager.getInstance()
                    .createActionPopupMenu(ActionPlaces.UNKNOWN, actionGroup)
                popupMenu.component.show(comp, x, y)
            }
        }.let { myTree.addMouseListener(it) }

        myTree.selectionModel.addTreeSelectionListener {
            val selectedConfiguration = getSelectedTreeNode()?.userObject as? EtcdBrowserTreeNodeUserObject
            clearStatsTable()
            updateStatsTable(selectedConfiguration)
        }

        return JPanel(BorderLayout()).apply {
            addCenter(myTree.getScrollComponent().withNoBorder())
        }
    }

    private fun addConfigurationToTree(configuration: EtcdServerConfiguration) {
        val nodeUserObject = EtcdBrowserTreeNodeUserObject(configuration)
        val childNode = EtcdBrowserTreeNode(nodeUserObject, false)
        val root = treeModel.root as EtcdBrowserTreeNode
        treeModel.insertNodeInto(childNode, root, root.childCount)
        myTree.scrollPathToVisible(TreePath(childNode.path))
    }

    internal fun isTreeSelected() = myTree.selectionCount != 0

    internal fun insertNewConfiguration(configuration: EtcdServerConfiguration) {
        etcdState.addEtcdConfiguration(configuration)
        addConfigurationToTree(configuration)
    }

    internal fun deleteSelectedConfiguration() {
        val nodeToRemove = getSelectedTreeNode() ?: return
        val userObject = nodeToRemove.userObject as EtcdBrowserTreeNodeUserObject
        val configuration = userObject.etcdServerConfiguration
        treeModel.removeNodeFromParent(nodeToRemove)
        etcdState.removeConfiguration(configuration)
        CredentialsService.instance.forgetPassword(PasswordKey(configuration.id))
    }

    internal fun checkHealthSelectedConfiguration() {
        val configuration = getCurrentConfiguration() ?: return
        GlobalScope.launch(UI_DISPATCHER) {
            etcdService.getAllAlarms(configuration.etcdServerConfiguration)?.let { alarms ->
                AlarmsPostProcessor(alarms).process()
            }
        }
    }

    internal fun editSelectedConfiguration() {
        val nodeToEdit = getSelectedTreeNode() ?: return
        val oldConfiguration = nodeToEdit.userObject as EtcdBrowserTreeNodeUserObject

        val configurationDialog = ConfigureServerDialogWindow(project, oldConfiguration.etcdServerConfiguration)
        if (configurationDialog.showAndGet()) {
            val newConfiguration = configurationDialog.getConfiguration()
            etcdState.upsertConfiguration(newConfiguration)
            nodeToEdit.userObject = oldConfiguration.copy(etcdServerConfiguration = newConfiguration)
        }
    }

    internal fun toggleWatchServerStatistics() {
        val nodeToEdit = getSelectedTreeNode() ?: return
        val oldConfiguration = nodeToEdit.userObject as EtcdBrowserTreeNodeUserObject
        nodeToEdit.userObject = oldConfiguration.copy(isWatchStatistics = !oldConfiguration.isWatchStatistics)
        updateStatsTable(getCurrentConfiguration())
    }

    internal fun isWatchServerStatisticsEnabled(): Boolean {
        return getCurrentConfiguration()?.isWatchStatistics ?: false
    }

    private fun createTablePanel(): JComponent {
        statsModel = EtcdStatusTableModel()
        return JPanel(BorderLayout()).apply {
            addCenter(SingleSelectionTable(statsModel).getScrollComponent().withNoBorder())
        }
    }

    private fun updateStatsTable(configuration: EtcdBrowserTreeNodeUserObject?) {
        if (configuration?.isWatchStatistics != true) {
            clearStatsTable()
        } else {
            GlobalScope.launch(UI_DISPATCHER) {
                etcdService.getMemberStatus(configuration.etcdServerConfiguration)?.let {
                    statsModel.setDataVector(it)
                }
            }
        }
    }

    private fun clearStatsTable() {
        if (this::statsModel.isInitialized) {
            statsModel.clear()
        }
    }

    private fun getSelectedTreeNode() = myTree.selectionPath?.lastPathComponent as? EtcdBrowserTreeNode

    private fun getCurrentConfiguration(): EtcdBrowserTreeNodeUserObject? {
        return getSelectedTreeNode()?.userObject as? EtcdBrowserTreeNodeUserObject
    }

    private fun fillServers() {
        etcdState.getConfigurations().forEach(::addConfigurationToTree)
    }
}
