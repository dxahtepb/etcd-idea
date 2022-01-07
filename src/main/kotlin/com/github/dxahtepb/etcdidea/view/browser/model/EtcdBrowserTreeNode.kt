package com.github.dxahtepb.etcdidea.view.browser.model

import javax.swing.tree.DefaultMutableTreeNode

class EtcdBrowserTreeNode(etcdServerConfiguration: EtcdBrowserTreeNodeUserObject?, allowsChildren: Boolean) :
    DefaultMutableTreeNode(etcdServerConfiguration, allowsChildren) {

    constructor() : this(null)
    constructor(etcdServerConfiguration: EtcdBrowserTreeNodeUserObject?) : this(etcdServerConfiguration, true)

    override fun toString(): String {
        return (super.userObject as? EtcdBrowserTreeNodeUserObject)?.etcdServerConfiguration.toString()
    }
}
