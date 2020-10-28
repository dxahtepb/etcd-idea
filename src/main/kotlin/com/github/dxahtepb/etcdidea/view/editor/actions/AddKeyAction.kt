package com.github.dxahtepb.etcdidea.view.editor.actions

import com.github.dxahtepb.etcdidea.view.editor.EtcdEditorPanel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class AddKeyAction(private val myTablePanel: EtcdEditorPanel) :
    AnAction("Add", "Add key", AllIcons.General.Add) {

    override fun actionPerformed(e: AnActionEvent) {
        myTablePanel.showAddKeyDialog()
    }
}
