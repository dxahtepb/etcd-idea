package com.github.dxahtepb.etcdidea.view.browser.actions

import com.github.dxahtepb.etcdidea.view.browser.BrowserToolWindow
import com.intellij.openapi.actionSystem.DataKey

object EtcdBrowserActionDataKeys {
    @JvmStatic
    val ETCD_BROWSER_TOOL_WINDOW = DataKey.create<BrowserToolWindow>("com.github.dxahtepb.etcdidea.view.browser")
}
