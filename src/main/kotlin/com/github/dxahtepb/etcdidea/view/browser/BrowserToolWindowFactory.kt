package com.github.dxahtepb.etcdidea.view.browser

import com.github.dxahtepb.etcdidea.service.EtcdService
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class BrowserToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = BrowserToolWindow(project, EtcdService.getInstance(project))
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(myToolWindow.getContent(), "", false)
        toolWindow.contentManager.addContent(content)
    }
}
