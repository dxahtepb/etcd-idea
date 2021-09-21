package com.github.dxahtepb.etcdidea.service

import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.service.auth.CredentialsService
import com.github.dxahtepb.etcdidea.service.auth.PasswordKey
import com.github.dxahtepb.etcdidea.toByteSequence
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.text.nullize
import io.etcd.jetcd.Client
import io.etcd.jetcd.ClientBuilder
import io.grpc.netty.GrpcSslContexts
import java.io.File

private val LOG = logger<EtcdConnectionHolder>()

class EtcdConnectionHolder(configuration: EtcdServerConfiguration) : AutoCloseable {
    private var client: Client

    init {
        val builder = Client.builder()
        builder.endpoints(configuration.hosts)
            .retryMaxDuration(configuration.timeouts.applicationTimeout)
            .keepaliveWithoutCalls(false)
            .authUser(configuration)
        client = builder.build()
        LOG.trace("Client $client built")
    }

    suspend fun <T> execute(action: suspend (client: Client) -> T?) = action.invoke(client)

    override fun close() {
        LOG.trace("Client $client closed")
        client.close()
    }
}

private fun ClientBuilder.authUser(configuration: EtcdServerConfiguration): ClientBuilder {
    val sslConf = configuration.sslConfiguration
    if (sslConf.sslEnabled) {
        val sslContext = GrpcSslContexts
            .forClient()
            .trustManager(createFile(sslConf.rootCertificate))
            .keyManager(createFile(sslConf.clientKeyChain), createFile(sslConf.clientKey))
            .build()
        this.sslContext(sslContext)
    } else {
        if (configuration.user.isNotEmpty()) {
            this.user(configuration.user.toByteSequence())
            val password = CredentialsService.instance.getPassword(PasswordKey(configuration.id))
            if (password != null) {
                this.password(password.toByteSequence())
            }
        }
    }
    return this
}

private fun createFile(path: String): File? = path.nullize()?.let { File(it) }
