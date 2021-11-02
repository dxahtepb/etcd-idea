package com.github.dxahtepb.etcdidea.view.editor.table.actions

import com.github.dxahtepb.etcdidea.EtcdBundle
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys

class FirstPageAction : AnAction(
    EtcdBundle.getMessage("editor.pagination.action.first.text"),
    EtcdBundle.getMessage("editor.pagination.action.first.description"),
    AllIcons.Actions.Play_first
) {
    override fun actionPerformed(e: AnActionEvent) {
        e.getData(PlatformDataKeys.CONTEXT_COMPONENT)
        e.getData(EtcdPaginationActionDataKeys.ETCD_TABLE_PAGINATOR_CONTROL)?.firstPage()
    }
}
