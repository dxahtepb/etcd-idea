package com.github.dxahtepb.etcdidea.model

import com.jetbrains.rd.util.UUID
import io.etcd.jetcd.Util
import java.net.URI

data class EtcdServerConfiguration(
    val id: String,
    val hosts: String,
    val user: String,
    val label: String
) {
    constructor(
        hosts: String,
        user: String,
        label: String
    ) : this(generateNewUniqueId(), hosts, user, label)

    fun toURIs(): List<URI> = Util.toURIs(hosts.split(";"))
    override fun toString(): String = "$label@$hosts"

    companion object {
        @JvmStatic
        fun generateNewUniqueId() = UUID.randomUUID().toString()
    }
}
