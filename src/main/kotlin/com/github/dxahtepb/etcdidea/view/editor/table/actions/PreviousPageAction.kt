package com.github.dxahtepb.etcdidea.view.editor.table.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class PreviousPageAction : AnAction(
    "Previous",
    "Show previous page",
    AllIcons.Actions.Play_back
) {
    override fun actionPerformed(e: AnActionEvent) {
        e.getData(EtcdPaginationActionDataKeys.ETCD_TABLE_PAGINATOR_CONTROL)?.previousPage()
    }
}
