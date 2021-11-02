package com.github.dxahtepb.etcdidea.view.browser.actions

import com.github.dxahtepb.etcdidea.EtcdBundle
import com.github.dxahtepb.etcdidea.view.browser.ConfigureServerDialogWindow
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class AddServerAction :
    AnAction(
        EtcdBundle.getMessage("browser.action.addServer.text"),
        EtcdBundle.getMessage("browser.action.addServer.description"),
        AllIcons.General.Add
    ) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        e.getData(EtcdBrowserActionDataKeys.ETCD_BROWSER_TOOL_WINDOW)?.also { toolWindow ->
            ConfigureServerDialogWindow(project).also {
                if (it.showAndGet()) {
                    toolWindow.insertNewConfiguration(it.getConfiguration())
                }
            }
        }
    }
}
