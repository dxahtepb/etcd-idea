package com.github.dxahtepb.etcdidea.service

import io.etcd.jetcd.Watch

class EtcdWatcherHolder(
    private val watcher: Watch.Watcher?,
    private val connectionHolder: EtcdConnectionHolder?,
    val startRevision: Long
) : AutoCloseable {
    override fun close() {
        watcher?.close()
        connectionHolder?.close()
    }
}
