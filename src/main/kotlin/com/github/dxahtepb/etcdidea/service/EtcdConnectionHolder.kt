package com.github.dxahtepb.etcdidea.service

import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import io.etcd.jetcd.Client

class EtcdConnectionHolder(configuration: EtcdServerConfiguration) : AutoCloseable {
    private var client: Client

    init {
        val builder = Client.builder()
        builder.endpoints(configuration.hosts)
        if (configuration.user.isNotEmpty() && configuration.password.isNotEmpty()) {
            builder.user(configuration.user.toByteSequence())
                .password(configuration.password.toByteSequence())
        }
        client = builder.build()
    }

    suspend fun <T> execute(action: suspend (client: Client) -> T?) = action.invoke(client)

    override fun close() {
        client.close()
    }
}
