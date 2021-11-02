package com.github.dxahtepb.etcdidea.view.editor.actions

import com.github.dxahtepb.etcdidea.EtcdBundle
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class EditKeyAction :
    AnAction(
        EtcdBundle.getMessage("editor.kv.action.editKey.text"),
        EtcdBundle.getMessage("editor.kv.action.editKey.description"),
        AllIcons.Actions.Edit
    ) {

    override fun actionPerformed(e: AnActionEvent) {
        EtcdEditorActionUtil.getFileEditor(e)?.editorPanel?.showEditKeyDialog()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = EtcdEditorActionUtil.isRowSelected(e)
    }
}
