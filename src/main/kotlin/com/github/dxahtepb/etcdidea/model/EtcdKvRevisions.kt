package com.github.dxahtepb.etcdidea.model

import io.etcd.jetcd.watch.WatchEvent

data class EtcdKvRevisions(val revisions: List<EtcdRevisionInfo>)

data class EtcdRevisionInfo(
    val kv: EtcdKeyValue,
    val version: Long,
    val createRevision: Long,
    val modRevision: Long,
    val lease: Long,
    val type: RevisionEventType
) {
    companion object Factory {
        fun fromWatchEvent(event: WatchEvent): EtcdRevisionInfo {
            val type = when (event.eventType) {
                WatchEvent.EventType.PUT -> RevisionEventType.PUT
                WatchEvent.EventType.DELETE -> RevisionEventType.DELETE
                else -> RevisionEventType.UNKNOWN
            }
            return EtcdRevisionInfo(
                EtcdKeyValue.fromKeyValue(event.keyValue),
                event.keyValue.version,
                event.keyValue.createRevision,
                event.keyValue.modRevision,
                event.keyValue.lease,
                type
            )
        }
    }
}

enum class RevisionEventType {
    PUT, DELETE, UNKNOWN
}
