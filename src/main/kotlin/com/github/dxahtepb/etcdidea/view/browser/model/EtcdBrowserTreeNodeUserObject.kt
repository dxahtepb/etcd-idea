package com.github.dxahtepb.etcdidea.view.browser.model

import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration

data class EtcdBrowserTreeNodeUserObject(
    val etcdServerConfiguration: EtcdServerConfiguration,
    val isWatchStatistics: Boolean = false
)
