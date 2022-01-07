package com.github.dxahtepb.etcdidea.view.browser.actions

import com.github.dxahtepb.etcdidea.EtcdBundle
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Toggleable

class WatchStatsToggleAction :
    AnAction(
        EtcdBundle.getMessage("browser.action.watchStatistics.text"),
        EtcdBundle.getMessage("browser.action.watchStatistics.description"),
        AllIcons.Debugger.Watch
    ),
    Toggleable {

    private fun getToolWindow(e: AnActionEvent) = e.getData(EtcdBrowserActionDataKeys.ETCD_BROWSER_TOOL_WINDOW)

    override fun actionPerformed(e: AnActionEvent) {
        val toolWindow = getToolWindow(e) ?: return
        toolWindow.toggleWatchServerStatistics()
        Toggleable.setSelected(e.presentation, toolWindow.isWatchServerStatisticsEnabled())
    }

    override fun update(e: AnActionEvent) {
        val toolWindow = getToolWindow(e)
        val isTreeSelected = toolWindow?.isTreeSelected() ?: false
        e.presentation.isEnabled = isTreeSelected
        val isWatchEnabled = toolWindow?.isWatchServerStatisticsEnabled() ?: false
        Toggleable.setSelected(e.presentation, isWatchEnabled)
    }
}
