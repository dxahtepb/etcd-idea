package com.github.dxahtepb.etcdidea.model

import com.jetbrains.rd.util.UUID
import io.etcd.jetcd.Util
import java.net.URI

data class EtcdServerConfiguration(
    val hosts: EtcdServerHosts,
    val user: String,
    val label: String,
    val timeouts: EtcdTimeoutConfiguration,
    val sslConfiguration: EtcdSslConfiguration = EtcdSslConfiguration(),
    val id: String = generateNewUniqueId()
) {
    override fun toString(): String = "$label@$hosts"

    companion object {
        @JvmStatic
        fun generateNewUniqueId() = UUID.randomUUID().toString()
    }
}

data class EtcdServerHosts(private val hosts: List<URI>) {
    fun asString() = toString()
    fun asURIs() = hosts
    override fun toString(): String = hosts.joinToString(";")

    companion object {
        fun create(hostsString: String) = EtcdServerHosts(Util.toURIs(hostsString.split(";")))
    }
}
