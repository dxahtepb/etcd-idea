package com.github.dxahtepb.etcdidea.model

import com.jetbrains.rd.util.UUID
import io.etcd.jetcd.Util
import java.net.URI

data class EtcdServerConfiguration(
    val hosts: String,
    val user: String,
    val label: String,
    val timeouts: EtcdTimeoutConfiguration,
    val sslConfiguration: EtcdSslConfiguration = EtcdSslConfiguration(),
    val id: String = generateNewUniqueId()
) {
    fun toURIs(): List<URI> = Util.toURIs(hosts.split(";"))
    override fun toString(): String = "$label@$hosts"

    companion object {
        @JvmStatic
        fun generateNewUniqueId() = UUID.randomUUID().toString()
    }
}
