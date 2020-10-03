package com.github.dxahtepb.etcdidea.service

import com.github.dxahtepb.etcdidea.model.EtcdKeyValue
import com.github.dxahtepb.etcdidea.model.EtcdKvEntries
import com.intellij.openapi.project.Project
import io.etcd.jetcd.ByteSequence
import io.etcd.jetcd.Client
import io.etcd.jetcd.KeyValue
import io.etcd.jetcd.options.GetOption
import java.nio.charset.StandardCharsets

@Suppress("UnstableApiUsage")
class EtcdService {
    private fun <T> getConnection(hosts: String, action: (client: Client) -> T?): T? {
        return Client.builder().endpoints(hosts).build().use(action)
    }

    fun listAllEntries(hosts: String): EtcdKvEntries {
        val getOption = GetOption.newBuilder().withRange(ZERO_KEY).build()
        val keyValues = getConnection(hosts) { client ->
            client.kvClient.get(ZERO_KEY, getOption)
                .thenApply { kvClient -> kvClient.kvs.map { EtcdKeyValue(it.getKeyAsString(), it.getValueAsString()) } }
                .get()
        } ?: emptyList()
        return EtcdKvEntries(keyValues)
    }

    companion object {
        fun getInstance(project: Project): EtcdService = project.getService(EtcdService::class.java)
    }
}

private val ZERO_KEY: ByteSequence = ByteSequence.from(byteArrayOf(0.toByte()))
private fun KeyValue.getKeyAsString() = this.key.toString(StandardCharsets.UTF_8)
private fun KeyValue.getValueAsString() = this.value.toString(StandardCharsets.UTF_8)
