package com.github.dxahtepb.etcdidea.view.browser.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class EditServerAction :
    AnAction("Edit Server", "Edit selected server", AllIcons.Actions.Edit) {

    override fun actionPerformed(e: AnActionEvent) {
        e.getData(EtcdBrowserActionDataKeys.ETCD_BROWSER_TOOL_WINDOW)?.editSelectedConfiguration()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled =
            e.getData(EtcdBrowserActionDataKeys.ETCD_BROWSER_TOOL_WINDOW)?.isTreeSelected() ?: false
    }
}
