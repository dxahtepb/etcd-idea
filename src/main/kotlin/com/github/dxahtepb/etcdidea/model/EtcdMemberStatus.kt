package com.github.dxahtepb.etcdidea.model

import io.etcd.jetcd.maintenance.StatusResponse

data class EtcdMemberStatus(
    val version: String,
    val size: Long,
    val leader: Long,
    val raftIndex: Long,
    val raftTerm: Long
) {
    companion object Factory {
        fun fromResponse(response: StatusResponse) = EtcdMemberStatus(
            response.version,
            response.dbSize,
            response.leader,
            response.raftIndex,
            response.raftTerm
        )
    }
}
