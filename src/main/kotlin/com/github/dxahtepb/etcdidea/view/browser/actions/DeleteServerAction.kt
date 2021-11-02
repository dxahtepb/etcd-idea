package com.github.dxahtepb.etcdidea.view.browser.actions

import com.github.dxahtepb.etcdidea.EtcdBundle
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class DeleteServerAction :
    AnAction(
        EtcdBundle.getMessage("browser.action.deleteServer.text"),
        EtcdBundle.getMessage("browser.action.deleteServer.description"),
        null
    ) {

    override fun actionPerformed(e: AnActionEvent) {
        e.getData(EtcdBrowserActionDataKeys.ETCD_BROWSER_TOOL_WINDOW)?.deleteSelectedConfiguration()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled =
            e.getData(EtcdBrowserActionDataKeys.ETCD_BROWSER_TOOL_WINDOW)?.isTreeSelected() ?: false
    }
}
