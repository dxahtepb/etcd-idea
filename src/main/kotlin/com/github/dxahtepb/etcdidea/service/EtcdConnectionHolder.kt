package com.github.dxahtepb.etcdidea.service

import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.service.auth.CredentialsService
import com.github.dxahtepb.etcdidea.service.auth.PasswordKey
import com.github.dxahtepb.etcdidea.toByteSequence
import io.etcd.jetcd.Client

class EtcdConnectionHolder(configuration: EtcdServerConfiguration) : AutoCloseable {
    private var client: Client

    init {
        val builder = Client.builder()
        builder.endpoints(configuration.hosts)
        if (configuration.user.isNotEmpty()) {
            builder.user(configuration.user.toByteSequence())
            val password = CredentialsService.instance.getPassword(PasswordKey(configuration.id))
            if (password != null) {
                builder.password(password.toByteSequence())
            }
        }
        client = builder.build()
    }

    suspend fun <T> execute(action: suspend (client: Client) -> T?) = action.invoke(client)

    override fun close() {
        client.close()
    }
}
