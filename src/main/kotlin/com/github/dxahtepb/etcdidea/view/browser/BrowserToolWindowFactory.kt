package com.github.dxahtepb.etcdidea.view.browser

import com.github.dxahtepb.etcdidea.persistence.EtcdConfigurationStateComponent
import com.github.dxahtepb.etcdidea.service.EtcdService
import com.github.dxahtepb.etcdidea.view.browser.actions.EtcdBrowserActionDataKeys
import com.intellij.ide.DataManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class BrowserToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        BrowserToolWindow(
            project,
            EtcdService.getInstance(project),
            EtcdConfigurationStateComponent.getInstance(project)
        ).also { myToolWindow ->
            with(toolWindow.contentManager) {
                factory.createContent(myToolWindow.getContent(), "", false).also {
                    addContent(it)
                }
            }
            DataManager.registerDataProvider(myToolWindow.getContent()) { dataId ->
                when {
                    EtcdBrowserActionDataKeys.ETCD_BROWSER_TOOL_WINDOW.`is`(dataId) -> myToolWindow
                    else -> null
                }
            }
        }
    }
}
