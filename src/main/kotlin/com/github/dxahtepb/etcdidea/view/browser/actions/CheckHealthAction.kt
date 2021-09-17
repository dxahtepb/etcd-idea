package com.github.dxahtepb.etcdidea.view.browser.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class CheckHealthAction(private val callback: () -> Unit, private val isActive: () -> Boolean) :
    AnAction("Check Etcd Health", "Check Etcd cluster health", AllIcons.RunConfigurations.TestUnknown) {

    override fun actionPerformed(e: AnActionEvent) {
        callback()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = isActive()
    }
}
