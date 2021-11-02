package com.github.dxahtepb.etcdidea.service

import com.github.dxahtepb.etcdidea.*
import com.github.dxahtepb.etcdidea.model.*
import com.intellij.openapi.project.Project
import io.etcd.jetcd.ByteSequence
import io.etcd.jetcd.Client
import io.etcd.jetcd.common.exception.CompactedException
import io.etcd.jetcd.maintenance.AlarmType
import io.etcd.jetcd.options.GetOption
import io.etcd.jetcd.options.OptionsUtil
import io.etcd.jetcd.options.WatchOption
import io.etcd.jetcd.watch.WatchResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.future.asDeferred
import java.util.concurrent.CompletableFuture

@Suppress("UnstableApiUsage")
class EtcdService {
    suspend fun listEntries(serverConfiguration: EtcdServerConfiguration, prefix: String? = null): EtcdKvEntries {
        return withContext(Dispatchers.IO) {
            val byteSequenceKey = prefix?.toByteSequence() ?: ZERO_KEY
            val getOptionBuilder = GetOption.newBuilder()
                .withSortOrder(GetOption.SortOrder.ASCEND)
                .apply {
                    if (prefix != null) {
                        withRange(OptionsUtil.prefixEndOf(byteSequenceKey))
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
                .asDeferred()
                .awaitOrThrow(serverConfiguration.timeouts.applicationTimeout)
        } ?: emptyList()
    }

    suspend fun putNewEntry(serverConfiguration: EtcdServerConfiguration, kv: EtcdKeyValue) {
        withContext(Dispatchers.IO) {
            serverConfiguration.useConnection { client ->
                client.kvClient.put(kv.key.toByteSequence(), kv.value.toByteSequence())
                    .asDeferred()
                    .awaitOrThrow(serverConfiguration.timeouts.applicationTimeout)
            }
        }
    }

    suspend fun deleteEntry(serverConfiguration: EtcdServerConfiguration, key: String) {
        withContext(Dispatchers.IO) {
            serverConfiguration.useConnection { client ->
                client.kvClient.delete(key.toByteSequence())
                    .asDeferred()
                    .awaitOrThrow(serverConfiguration.timeouts.applicationTimeout)
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
                    .asDeferred()
                    .await(serverConfiguration.timeouts.applicationTimeout, null)
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

    suspend fun getAllAlarms(serverConfiguration: EtcdServerConfiguration): EtcdAlarms? {
        return withContext(Dispatchers.IO) {
            serverConfiguration.useConnection { client: Client ->
                val alarms = client.maintenanceClient.listAlarms()
                    .thenApply { response ->
                        response.alarms.map {
                            val alarmType = when (it.alarmType) {
                                AlarmType.NOSPACE -> EtcdAlarmType.NOSPACE
                                else -> EtcdAlarmType.UNRECOGNIZED
                            }
                            EtcdAlarmMember(it.memberId, alarmType)
                        }
                    }
                    .asDeferred().awaitOrThrow(serverConfiguration.timeouts.applicationTimeout)
                EtcdAlarms(alarms)
            }
        }
    }

    suspend fun getMemberStatus(serverConfiguration: EtcdServerConfiguration): EtcdMemberStatus? {
        return withContext(Dispatchers.IO) {
            serverConfiguration.useConnection { client: Client ->
                // todo: run for all members?
                val uri = serverConfiguration.hosts.asURIs().first()
                val res = client.maintenanceClient.statusMember(uri)
                    .thenApply { EtcdMemberStatus.fromResponse(it) }
                    .asDeferred().awaitOrThrow(serverConfiguration.timeouts.applicationTimeout)
                res
            }
        }
    }

    companion object {
        fun getInstance(project: Project): EtcdService = project.getService(EtcdService::class.java)
    }
}

private suspend fun <T> EtcdServerConfiguration.useConnection(action: suspend (client: Client) -> T?): T? {
    return EtcdConnectionHolder(this).useAsync {
        it.execute(action.withNotificationErrorSink())
    }
}

private val ZERO_KEY: ByteSequence = ByteSequence.from(byteArrayOf(0.toByte()))
