package com.github.dxahtepb.etcdidea.view.editor.table.actions

import com.github.dxahtepb.etcdidea.view.editor.table.EtcdEditorTablePaginator
import com.intellij.openapi.actionSystem.DataKey

object EtcdPaginationActionDataKeys {
    @JvmStatic
    val ETCD_TABLE_PAGINATOR_CONTROL =
        DataKey.create<EtcdEditorTablePaginator.PaginationControl>(
            "com.github.dxahtepb.etcdidea.view.table.paginator.control"
        )
}
