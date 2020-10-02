package com.github.dxahtepb.etcdidea.service

import com.intellij.openapi.project.Project
import io.etcd.jetcd.Client

class EtcdService {
    private fun <T> getConnection(hosts: String, action: (client: Client) -> T?): T? {
        return Client.builder().endpoints(hosts).build()?.use(action)
    }

    fun listMembers(hosts: String): String? {
        return getConnection(hosts) { client ->
            client.clusterClient
                .listMember()
                .thenApply { membersList ->
                    membersList.members.joinToString(separator = "\n") { "${it.name}: ${it.clientURIs}" }
                }
                .get()
        }
    }

    companion object SERVICE {
        fun getInstance(project: Project): EtcdService = project.getService(EtcdService::class.java)
    }
}
