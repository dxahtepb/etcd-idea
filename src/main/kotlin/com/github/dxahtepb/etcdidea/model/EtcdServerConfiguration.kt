package com.github.dxahtepb.etcdidea.model

import io.etcd.jetcd.Util
import java.net.URI

data class EtcdServerConfiguration(val hosts: String, val user: String, val label: String, val password: String) {
    fun toURIs(): List<URI> = Util.toURIs(hosts.split(";"))
    override fun toString(): String = "$label@$hosts"
}
