package com.github.dxahtepb.etcdidea.service

import com.github.dxahtepb.etcdidea.*
import com.github.dxahtepb.etcdidea.model.*
import com.intellij.openapi.project.Project
import io.etcd.jetcd.ByteSequence
import io.etcd.jetcd.Client
import io.etcd.jetcd.common.exception.CompactedException
import io.etcd.jetcd.options.GetOption
import io.etcd.jetcd.options.WatchOption
import io.etcd.jetcd.watch.WatchResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.future.asDeferred
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

@Suppress("UnstableApiUsage")
class EtcdService {
    suspend fun listEntries(serverConfiguration: EtcdServerConfiguration, prefix: String? = null): EtcdKvEntries {
        return withContext(Dispatchers.IO) {
            val byteSequenceKey = prefix?.toByteSequence() ?: ZERO_KEY
            val getOptionBuilder = GetOption.newBuilder().apply {
                if (prefix != null) {
                    withPrefix(byteSequenceKey)
                } else {
                    withRange(byteSequenceKey)
                }
            }
            EtcdKvEntries(fetchKeyValuesWithOptions(serverConfiguration, byteSequenceKey, getOptionBuilder.build()))
        }
    }

    private suspend fun fetchKeyValuesWithOptions(
        serverConfiguration: EtcdServerConfiguration,
        key: ByteSequence,
        getOption: GetOption
    ): List<EtcdKeyValue> {
        return serverConfiguration.useConnection { client ->
            client.kvClient.get(key, getOption)
                .thenApply { kvClient -> kvClient.kvs.map { EtcdKeyValue.fromKeyValue(it) } }
                .asDeferred().await()
        } ?: emptyList()
    }

    suspend fun putNewEntry(serverConfiguration: EtcdServerConfiguration, kv: EtcdKeyValue) {
        withContext(Dispatchers.IO) {
            serverConfiguration.useConnection { client ->
                client.kvClient.put(kv.key.toByteSequence(), kv.value.toByteSequence()).asDeferred().await()
            }
        }
    }

    suspend fun deleteEntry(serverConfiguration: EtcdServerConfiguration, key: String) {
        withContext(Dispatchers.IO) {
            serverConfiguration.useConnection { client ->
                client.kvClient.delete(key.toByteSequence()).asDeferred().await()
            }
        }
    }

    suspend fun getRevisions(
        serverConfiguration: EtcdServerConfiguration,
        key: String,
        callback: (EtcdKvRevisions) -> Unit
    ): EtcdWatcherHolder? {
        return withContext(Dispatchers.IO) {
            val revision = executeWithErrorSink(notificationErrorSink) {
                getLeastAvailableRevision(serverConfiguration, key)
                    .asDeferred().await(1000, null)
            } ?: return@withContext null
            val watchOptions = WatchOption.newBuilder().withRevision(revision).build()
            val connection = EtcdConnectionHolder(serverConfiguration)
            val watcher = connection.execute(
                notificationErrorSink { client ->
                    val onNext: (WatchResponse) -> Unit = { watchResponse ->
                        val revisions = watchResponse.events
                            .map { EtcdRevisionInfo.fromWatchEvent(it) }
                            .let { EtcdKvRevisions(it) }
                        callback(revisions)
                    }
                    val onError = notificationErrorSink
                    client.watchClient.watch(key.toByteSequence(), watchOptions, onNext, onError)
                }
            )
            EtcdWatcherHolder(watcher, connection, revision)
        }
    }

    private suspend fun getLeastAvailableRevision(
        serverConfiguration: EtcdServerConfiguration,
        key: String
    ): CompletableFuture<Long> {
        var revision = 1L
        val future = CompletableFuture<Long>()
        val watchOptions = WatchOption.newBuilder().withRevision(revision).withProgressNotify(true).build()
        val connection = EtcdConnectionHolder(serverConfiguration)
        val watcherHolder = connection.execute { client ->
            val onError = { e: Throwable ->
                if (e is CompactedException) {
                    revision = e.compactedRevision
                    future.complete(revision)
                    Unit
                } else {
                    future.complete(revision)
                    throw e
                }
            }
            val onNext: (_: WatchResponse) -> Unit = {
                future.complete(revision)
                Unit
            }
            val watcher = client.watchClient.watch(key.toByteSequence(), watchOptions, onNext, onError)
            EtcdWatcherHolder(watcher, connection, 1)
        }
        future.thenRun {
            watcherHolder?.close()
        }.exceptionally {
            watcherHolder?.close()
            throw it
        }
        return future
    }

    suspend fun getMemberStatus(serverConfiguration: EtcdServerConfiguration): EtcdMemberStatus? {
        return withContext(Dispatchers.IO) {
            serverConfiguration.useConnection { client: Client ->
                client.maintenanceClient.statusMember(URI(serverConfiguration.hosts))
                    .thenApply { EtcdMemberStatus.fromResponse(it) }
                    .asDeferred().await()
            }
        }
    }

    companion object {
        fun getInstance(project: Project): EtcdService = project.getService(EtcdService::class.java)
    }
}

private suspend fun <T> EtcdServerConfiguration.useConnection(action: suspend (client: Client) -> T?): T? {
    val connection = EtcdConnectionHolder(this)
    return connection.useAsync { it.execute(action.withNotificationErrorSink()) }
}

private val ZERO_KEY: ByteSequence = ByteSequence.from(byteArrayOf(0.toByte()))
internal fun String.toByteSequence() = ByteSequence.from(this, StandardCharsets.UTF_8)
