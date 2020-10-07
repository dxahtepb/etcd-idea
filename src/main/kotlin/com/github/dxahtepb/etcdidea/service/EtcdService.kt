package com.github.dxahtepb.etcdidea.service

import com.github.dxahtepb.etcdidea.model.EtcdConnection
import com.github.dxahtepb.etcdidea.model.EtcdKeyValue
import com.github.dxahtepb.etcdidea.model.EtcdKvEntries
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import io.etcd.jetcd.ByteSequence
import io.etcd.jetcd.Client
import io.etcd.jetcd.KeyValue
import io.etcd.jetcd.options.GetOption
import java.nio.charset.StandardCharsets

@Suppress("UnstableApiUsage")
class EtcdService {
    private fun <T> getConnection(connection: EtcdConnection, action: (client: Client) -> T?): T? {
        try {
            return Client.builder().endpoints(connection.hosts).build().use(action)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            if (!ApplicationManager.getApplication().isUnitTestMode) {
                Notifications.Bus.notify(
                    Notification(
                        "Etcd Browser",
                        "Etcd error",
                        e.message ?: "Unknown error",
                        NotificationType.ERROR
                    )
                )
            } else {
                throw e
            }
        }
        return null
    }

    fun listAllEntries(connection: EtcdConnection): EtcdKvEntries {
        val getOption = GetOption.newBuilder().withRange(ZERO_KEY).build()
        val keyValues = getConnection(connection) { client ->
            client.kvClient.get(ZERO_KEY, getOption)
                .thenApply { kvClient -> kvClient.kvs.map { EtcdKeyValue(it.getKeyAsString(), it.getValueAsString()) } }
                .get()
        } ?: emptyList()
        return EtcdKvEntries(keyValues)
    }

    fun putNewEntry(connection: EtcdConnection, kv: EtcdKeyValue) {
        getConnection(connection) { client ->
            client.kvClient.put(kv.key.toByteSequence(), kv.value.toByteSequence()).get()
        }
    }

    fun deleteEntry(connection: EtcdConnection, key: String) {
        getConnection(connection) { client ->
            client.kvClient.delete(key.toByteSequence()).get()
        }
    }

    companion object {
        fun getInstance(project: Project): EtcdService = project.getService(EtcdService::class.java)
    }
}

private val ZERO_KEY: ByteSequence = ByteSequence.from(byteArrayOf(0.toByte()))
private fun KeyValue.getKeyAsString() = this.key.toString(StandardCharsets.UTF_8)
private fun KeyValue.getValueAsString() = this.value.toString(StandardCharsets.UTF_8)
private fun String.toByteSequence() = ByteSequence.from(this, StandardCharsets.UTF_8)
