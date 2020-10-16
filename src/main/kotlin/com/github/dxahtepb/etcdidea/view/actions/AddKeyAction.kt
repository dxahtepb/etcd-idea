package com.github.dxahtepb.etcdidea.view.actions

import com.github.dxahtepb.etcdidea.view.browser.BrowserToolWindow
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class AddKeyAction(private val myTablePanel: BrowserToolWindow) :
    AnAction("Add", "Add key", AllIcons.General.Add) {

    override fun actionPerformed(e: AnActionEvent) {
        myTablePanel.showAddKeyDialog()
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isEnabled = myTablePanel.isTreeSelected()
    }
}
