package com.github.dxahtepb.etcdidea.view.editor.actions

import com.github.dxahtepb.etcdidea.view.editor.EtcdEditorPanel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class EditKeyAction(private val myTablePanel: EtcdEditorPanel) :
    AnAction("Edit", "Edit key", AllIcons.Actions.Edit) {

    override fun actionPerformed(e: AnActionEvent) {
        myTablePanel.showEditKeyDialog()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = myTablePanel.isRowSelected()
    }
}
