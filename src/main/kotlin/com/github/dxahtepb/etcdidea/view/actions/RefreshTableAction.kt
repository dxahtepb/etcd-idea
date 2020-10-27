package com.github.dxahtepb.etcdidea.view.actions

import com.github.dxahtepb.etcdidea.view.editor.EtcdEditorPanel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class RefreshTableAction(private val myTablePanel: EtcdEditorPanel) :
    AnAction("Refresh", "Refresh table", AllIcons.Actions.Refresh) {

    override fun actionPerformed(e: AnActionEvent) {
        myTablePanel.updateResults()
    }
}
