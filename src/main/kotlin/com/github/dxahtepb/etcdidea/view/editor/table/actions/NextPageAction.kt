package com.github.dxahtepb.etcdidea.view.editor.table.actions

import com.github.dxahtepb.etcdidea.EtcdBundle
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class NextPageAction : AnAction(
    EtcdBundle.getMessage("editor.pagination.action.next.text"),
    EtcdBundle.getMessage("editor.pagination.action.next.description"),
    AllIcons.Actions.Play_forward
) {
    override fun actionPerformed(e: AnActionEvent) {
        e.getData(EtcdPaginationActionDataKeys.ETCD_TABLE_PAGINATOR_CONTROL)?.nextPage()
    }
}
