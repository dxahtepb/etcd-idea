package com.github.dxahtepb.etcdidea.service

import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.service.auth.createAuthenticatorFor
import com.intellij.openapi.diagnostic.logger
import io.etcd.jetcd.Client

private val LOG = logger<EtcdConnectionHolder>()

class EtcdConnectionHolder(configuration: EtcdServerConfiguration) : AutoCloseable {
    private var client: Client

    init {
        client = Client.builder()
            .endpoints(configuration.hosts)
            .retryMaxDuration(configuration.timeouts.applicationTimeout)
            .keepaliveWithoutCalls(false)
            .let { builder ->
                val authenticator = createAuthenticatorFor(configuration)
                authenticator.authenticate(builder)
            }
            .build()
        LOG.trace("Client $client built")
    }

    suspend fun <T> execute(action: suspend (client: Client) -> T?) = action.invoke(client)

    override fun close() {
        LOG.trace("Client $client closed")
        client.close()
    }
}
