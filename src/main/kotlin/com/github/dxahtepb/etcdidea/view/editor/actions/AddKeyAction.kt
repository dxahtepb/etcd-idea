package com.github.dxahtepb.etcdidea.view.editor.actions

import com.github.dxahtepb.etcdidea.EtcdBundle
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class AddKeyAction :
    AnAction(
        EtcdBundle.getMessage("editor.kv.action.addKey.text"),
        EtcdBundle.getMessage("editor.kv.action.addKey.description"),
        AllIcons.General.Add
    ) {

    override fun actionPerformed(e: AnActionEvent) {
        EtcdEditorActionUtil.getFileEditor(e)?.editorPanel?.showAddKeyDialog()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = EtcdEditorActionUtil.getFileEditor(e)?.isValid ?: false
    }
}
