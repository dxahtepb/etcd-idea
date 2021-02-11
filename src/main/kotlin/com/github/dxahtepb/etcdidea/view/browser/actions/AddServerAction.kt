package com.github.dxahtepb.etcdidea.view.browser.actions

import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.view.browser.ConfigureServerDialogWindow
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class AddServerAction(private val callback: (configuration: EtcdServerConfiguration) -> Unit) :
    AnAction("Add Etcd Server", "Add Etcd server", AllIcons.General.Add) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val configurationDialog = ConfigureServerDialogWindow(project)
        if (!configurationDialog.showAndGet()) return

        val newConfiguration = configurationDialog.getConfiguration()
        callback(newConfiguration)
    }
}
