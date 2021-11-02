package com.github.dxahtepb.etcdidea.view.editor.actions

import com.github.dxahtepb.etcdidea.EtcdBundle
import com.intellij.icons.AllIcons
import com.intellij.ide.actions.RefreshAction
import com.intellij.openapi.actionSystem.AnActionEvent

class RefreshTableAction :
    RefreshAction(
        EtcdBundle.getMessage("editor.kv.action.refreshTable.text"),
        EtcdBundle.getMessage("editor.kv.action.refreshTable.description"),
        AllIcons.Actions.Refresh
    ) {

    override fun actionPerformed(e: AnActionEvent) {
        EtcdEditorActionUtil.getFileEditor(e)?.editorPanel?.updateResults()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = EtcdEditorActionUtil.getFileEditor(e)?.isValid ?: false
    }
}
