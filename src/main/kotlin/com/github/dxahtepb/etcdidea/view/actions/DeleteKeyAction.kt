package com.github.dxahtepb.etcdidea.view.actions

import com.github.dxahtepb.etcdidea.view.BrowserToolWindow
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class DeleteKeyAction(private val myTablePanel: BrowserToolWindow) :
    AnAction("Delete", "Delete selected key", AllIcons.General.Remove) {

    override fun actionPerformed(e: AnActionEvent) {
        myTablePanel.deleteSelectedKey()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = myTablePanel.isRowSelected() && myTablePanel.isTreeSelected()
    }
}