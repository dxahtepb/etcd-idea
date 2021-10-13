package com.github.dxahtepb.etcdidea.view.editor.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class DeleteKeyAction :
    AnAction("Delete", "Delete selected key", AllIcons.General.Remove) {

    override fun actionPerformed(e: AnActionEvent) {
        EtcdEditorActionUtil.getFileEditor(e)?.editorPanel?.deleteSelectedKey()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = EtcdEditorActionUtil.isRowSelected(e)
    }
}
