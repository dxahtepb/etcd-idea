package com.github.dxahtepb.etcdidea.view.browser.actions

import com.github.dxahtepb.etcdidea.EtcdBundle
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class CheckHealthAction :
    AnAction(
        EtcdBundle.getMessage("browser.action.checkHealth.text"),
        EtcdBundle.getMessage("browser.action.checkHealth.description"),
        AllIcons.RunConfigurations.TestUnknown
    ) {

    private fun getToolWindow(e: AnActionEvent) = e.getData(EtcdBrowserActionDataKeys.ETCD_BROWSER_TOOL_WINDOW)

    override fun actionPerformed(e: AnActionEvent) {
        val browserToolWindow = getToolWindow(e)
        browserToolWindow?.checkHealthSelectedConfiguration()
    }

    override fun update(e: AnActionEvent) {
        val browserToolWindow = getToolWindow(e)
        e.presentation.isEnabled = browserToolWindow?.isTreeSelected() ?: false
    }
}
