package com.github.dxahtepb.etcdidea.view.browser.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class DeleteServerAction :
    AnAction("Remove", "Delete selected server", null) {

    override fun actionPerformed(e: AnActionEvent) {
        e.getData(EtcdBrowserActionDataKeys.ETCD_BROWSER_TOOL_WINDOW)?.deleteSelectedConfiguration()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled =
            e.getData(EtcdBrowserActionDataKeys.ETCD_BROWSER_TOOL_WINDOW)?.isTreeSelected() ?: false
    }
}
