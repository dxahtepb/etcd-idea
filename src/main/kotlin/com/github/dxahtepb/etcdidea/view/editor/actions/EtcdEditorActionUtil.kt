package com.github.dxahtepb.etcdidea.view.editor.actions

import com.github.dxahtepb.etcdidea.view.editor.EtcdEditor
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys

internal object EtcdEditorActionUtil {
    fun getFileEditor(e: AnActionEvent) = e.getData(PlatformDataKeys.FILE_EDITOR) as? EtcdEditor

    fun isRowSelected(e: AnActionEvent): Boolean {
        return getFileEditor(e)?.let {
            it.isValid && it.editorPanel.isRowSelected()
        } ?: false
    }
}
