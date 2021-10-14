package com.github.dxahtepb.etcdidea.vfs

import com.intellij.openapi.fileTypes.ex.FakeFileType
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

object EtcdFakeFileType : FakeFileType() {
    private val ETCD_ICON: Icon? = IconLoader.findIcon("/icons/etcd.png")

    override fun getIcon() = ETCD_ICON

    override fun getName() = "ETCD"

    override fun getDescription() = "ETCD key-value store"

    override fun isMyFileType(file: VirtualFile) = file is EtcdDummyVirtualFile
}
