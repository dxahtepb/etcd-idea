package com.github.dxahtepb.etcdidea.vfs

import com.intellij.openapi.vfs.NonPhysicalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.openapi.vfs.newvfs.impl.NullVirtualFile

private const val PROTOCOL = "etcd"

class EtcdDummyFileSystem : VirtualFileSystem(), NonPhysicalFileSystem {
    companion object {
        val INSTANCE: EtcdDummyFileSystem
            get() = VirtualFileManager.getInstance().getFileSystem(PROTOCOL) as EtcdDummyFileSystem
    }

    override fun getProtocol() = PROTOCOL

    override fun findFileByPath(path: String): VirtualFile? = null

    override fun refresh(asynchronous: Boolean) = Unit

    override fun refreshAndFindFileByPath(path: String): VirtualFile? = null

    override fun addVirtualFileListener(listener: VirtualFileListener) = Unit

    override fun removeVirtualFileListener(listener: VirtualFileListener) = Unit

    override fun deleteFile(requestor: Any?, vFile: VirtualFile) = Unit

    override fun moveFile(requestor: Any?, vFile: VirtualFile, newParent: VirtualFile) = Unit

    override fun renameFile(requestor: Any?, vFile: VirtualFile, newName: String) = Unit

    override fun createChildFile(requestor: Any?, vDir: VirtualFile, fileName: String): VirtualFile =
        NullVirtualFile.INSTANCE

    override fun createChildDirectory(requestor: Any?, vDir: VirtualFile, dirName: String): VirtualFile =
        NullVirtualFile.INSTANCE

    override fun copyFile(
        requestor: Any?,
        virtualFile: VirtualFile,
        newParent: VirtualFile,
        copyName: String
    ): VirtualFile = NullVirtualFile.INSTANCE

    override fun isReadOnly() = true
}
