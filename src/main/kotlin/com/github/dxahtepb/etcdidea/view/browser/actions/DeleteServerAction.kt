package com.github.dxahtepb.etcdidea.view.browser.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class DeleteServerAction(private val callback: () -> Unit, private val isActive: () -> Boolean) :
    AnAction("Delete server", "Delete selected server", AllIcons.General.Remove) {

    override fun actionPerformed(e: AnActionEvent) {
        callback()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = isActive()
    }
}
