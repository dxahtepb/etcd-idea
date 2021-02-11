package com.github.dxahtepb.etcdidea.view.browser.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class DeleteServerAction(private val callback: () -> Unit, private val isActive: () -> Boolean) :
    AnAction("Remove", "Delete selected server", null) {

    override fun actionPerformed(e: AnActionEvent) {
        callback()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = isActive()
    }
}
