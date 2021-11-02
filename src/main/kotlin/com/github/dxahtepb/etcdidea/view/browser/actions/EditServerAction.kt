package com.github.dxahtepb.etcdidea.view.browser.actions

import com.github.dxahtepb.etcdidea.EtcdBundle
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class EditServerAction :
    AnAction(
        EtcdBundle.getMessage("browser.action.editServer.text"),
        EtcdBundle.getMessage("browser.action.editServer.description"),
        AllIcons.Actions.Edit
    ) {

    override fun actionPerformed(e: AnActionEvent) {
        e.getData(EtcdBrowserActionDataKeys.ETCD_BROWSER_TOOL_WINDOW)?.editSelectedConfiguration()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled =
            e.getData(EtcdBrowserActionDataKeys.ETCD_BROWSER_TOOL_WINDOW)?.isTreeSelected() ?: false
    }
}
