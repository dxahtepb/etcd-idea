package com.github.dxahtepb.etcdidea.service

import com.github.dxahtepb.etcdidea.model.EtcdKeyValue
import com.github.dxahtepb.etcdidea.model.EtcdKvEntries
import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
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
import java.util.concurrent.ExecutionException

@Suppress("UnstableApiUsage")
class EtcdService {
    private fun <T> getConnection(serverConfiguration: EtcdServerConfiguration, action: (client: Client) -> T?): T? {
        try {
            val builder = Client.builder()
            builder.endpoints(serverConfiguration.hosts)
            if (serverConfiguration.user.isNotEmpty() && serverConfiguration.password.isNotEmpty()) {
                builder.user(serverConfiguration.user.toByteSequence())
                    .password(serverConfiguration.password.toByteSequence())
            }
            return builder.build().use(action)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            val cause = getCause(e)
            if (!ApplicationManager.getApplication().isUnitTestMode) {
                Notifications.Bus.notify(
                    Notification(
                        "Etcd Browser",
                        "Etcd error",
                        cause.message ?: "Unknown error",
                        NotificationType.ERROR
                    )
                )
            } else {
                throw cause
            }
        }
        return null
    }

    fun listAllEntries(serverConfiguration: EtcdServerConfiguration): EtcdKvEntries {
        val getOption = GetOption.newBuilder().withRange(ZERO_KEY).build()
        val keyValues = getConnection(serverConfiguration) { client ->
            client.kvClient.get(ZERO_KEY, getOption)
                .thenApply { kvClient -> kvClient.kvs.map { EtcdKeyValue(it.getKeyAsString(), it.getValueAsString()) } }
                .get()
        } ?: emptyList()
        return EtcdKvEntries(keyValues)
    }

    fun putNewEntry(serverConfiguration: EtcdServerConfiguration, kv: EtcdKeyValue) {
        getConnection(serverConfiguration) { client ->
            client.kvClient.put(kv.key.toByteSequence(), kv.value.toByteSequence()).get()
        }
    }

    fun deleteEntry(serverConfiguration: EtcdServerConfiguration, key: String) {
        getConnection(serverConfiguration) { client ->
            client.kvClient.delete(key.toByteSequence()).get()
        }
    }

    companion object {
        fun getInstance(project: Project): EtcdService = project.getService(EtcdService::class.java)
    }
}

private fun getCause(e: Throwable) = if (e is ExecutionException) e.cause ?: e else e

private val ZERO_KEY: ByteSequence = ByteSequence.from(byteArrayOf(0.toByte()))
private fun KeyValue.getKeyAsString() = this.key.toString(StandardCharsets.UTF_8)
private fun KeyValue.getValueAsString() = this.value.toString(StandardCharsets.UTF_8)
private fun String.toByteSequence() = ByteSequence.from(this, StandardCharsets.UTF_8)
