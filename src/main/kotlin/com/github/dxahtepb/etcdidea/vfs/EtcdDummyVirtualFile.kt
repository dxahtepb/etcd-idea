package com.github.dxahtepb.etcdidea.vfs

import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import java.io.InputStream
import java.io.OutputStream
import java.lang.UnsupportedOperationException

class EtcdDummyVirtualFile(val configuration: EtcdServerConfiguration) : VirtualFile() {
    override fun getName(): String = "${configuration.label}@${configuration.hosts}"

    override fun getFileSystem(): VirtualFileSystem = EtcdDummyFileSystem.INSTANCE

    override fun getPath(): String = name

    override fun isWritable(): Boolean = false

    override fun isDirectory(): Boolean = false

    override fun isValid(): Boolean = true

    override fun getParent(): VirtualFile? = null

    override fun getChildren(): Array<VirtualFile>? = null

    override fun getOutputStream(requestor: Any?, newModificationStamp: Long, newTimeStamp: Long): OutputStream =
        unsupported()

    override fun contentsToByteArray(): ByteArray = ByteArray(0)

    override fun getTimeStamp(): Long = 0L

    override fun getLength(): Long = 0L

    override fun refresh(asynchronous: Boolean, recursive: Boolean, postRunnable: Runnable?) = Unit

    override fun getInputStream(): InputStream = unsupported()

    private fun unsupported(): Nothing = throw UnsupportedOperationException()
}
