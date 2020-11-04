package com.github.dxahtepb.etcdidea.service

import com.github.dxahtepb.etcdidea.model.EtcdKeyValue
import com.github.dxahtepb.etcdidea.model.EtcdKvEntries
import com.github.dxahtepb.etcdidea.model.EtcdKvRevisions
import com.github.dxahtepb.etcdidea.model.EtcdMemberStatus
import com.github.dxahtepb.etcdidea.model.EtcdRevisionInfo
import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.intellij.openapi.project.Project
import io.etcd.jetcd.ByteSequence
import io.etcd.jetcd.Client
import io.etcd.jetcd.options.GetOption
import io.etcd.jetcd.options.WatchOption
import io.etcd.jetcd.watch.WatchResponse
import java.net.URI
import java.nio.charset.StandardCharsets

@Suppress("UnstableApiUsage")
class EtcdService {

    fun listAllEntries(serverConfiguration: EtcdServerConfiguration): EtcdKvEntries {
        val getOption = GetOption.newBuilder().withRange(ZERO_KEY).build()
        val keyValues = serverConfiguration.useConnection { client ->
            client.kvClient.get(ZERO_KEY, getOption)
                .thenApply { kvClient -> kvClient.kvs.map { EtcdKeyValue.fromKeyValue(it) } }
                .get()
        } ?: emptyList()
        return EtcdKvEntries(keyValues)
    }

    fun putNewEntry(serverConfiguration: EtcdServerConfiguration, kv: EtcdKeyValue) {
        serverConfiguration.useConnection { client ->
            client.kvClient.put(kv.key.toByteSequence(), kv.value.toByteSequence()).get()
        }
    }

    fun deleteEntry(serverConfiguration: EtcdServerConfiguration, key: String) {
        serverConfiguration.useConnection { client ->
            client.kvClient.delete(key.toByteSequence()).get()
        }
    }

    fun getRevisions(
        serverConfiguration: EtcdServerConfiguration,
        key: String,
        callback: (EtcdKvRevisions) -> Unit
    ): EtcdWatcherHolder {
        val watchOptions = WatchOption.newBuilder().withRevision(1).build()
        val connection = EtcdConnectionHolder(serverConfiguration)
        val watcher = connection.execute(
            notificationErrorSink { client ->
                val onNext = { watchResponse: WatchResponse ->
                    val events = mutableListOf<EtcdRevisionInfo>()
                    watchResponse.events.forEach {
                        events.add(EtcdRevisionInfo.fromWatchEvent(it))
                    }
                    callback(EtcdKvRevisions(events))
                }
                val onError = notificationErrorSink
                client.watchClient.watch(key.toByteSequence(), watchOptions, onNext, onError)
            }
        )
        return EtcdWatcherHolder(watcher, connection)
    }

    fun getMemberStatus(serverConfiguration: EtcdServerConfiguration): EtcdMemberStatus? {
        return serverConfiguration.useConnection { client: Client ->
            client.maintenanceClient.statusMember(URI(serverConfiguration.hosts))
                .thenApply { EtcdMemberStatus.fromResponse(it) }
                .get()
        }
    }

    companion object {
        fun getInstance(project: Project): EtcdService = project.getService(EtcdService::class.java)
    }
}

private fun <T> EtcdServerConfiguration.useConnection(action: (client: Client) -> T?): T? {
    val connection = EtcdConnectionHolder(this)
    return connection.use { it.execute(action.withNotificationErrorSink()) }
}

private val ZERO_KEY: ByteSequence = ByteSequence.from(byteArrayOf(0.toByte()))
internal fun String.toByteSequence() = ByteSequence.from(this, StandardCharsets.UTF_8)
