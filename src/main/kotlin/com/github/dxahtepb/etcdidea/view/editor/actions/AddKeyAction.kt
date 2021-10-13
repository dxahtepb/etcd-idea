package com.github.dxahtepb.etcdidea.view.editor.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class AddKeyAction :
    AnAction("Add", "Add key", AllIcons.General.Add) {

    override fun actionPerformed(e: AnActionEvent) {
        EtcdEditorActionUtil.getFileEditor(e)?.editorPanel?.showAddKeyDialog()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = EtcdEditorActionUtil.getFileEditor(e)?.isValid ?: false
    }
}
