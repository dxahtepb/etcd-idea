package com.github.dxahtepb.etcdidea.service

import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.model.EtcdSslConfiguration
import com.github.dxahtepb.etcdidea.model.NoSslConfiguration
import com.github.dxahtepb.etcdidea.service.auth.CredentialsService
import com.github.dxahtepb.etcdidea.service.auth.PasswordKey
import com.github.dxahtepb.etcdidea.toByteSequence
import com.intellij.util.text.nullize
import io.etcd.jetcd.Client
import io.etcd.jetcd.ClientBuilder
import io.grpc.netty.GrpcSslContexts
import java.io.File

class EtcdConnectionHolder(configuration: EtcdServerConfiguration) : AutoCloseable {
    private var client: Client

    init {
        val builder = Client.builder()
        builder.endpoints(configuration.hosts)
            .authUser(configuration)
        client = builder.build()
    }

    suspend fun <T> execute(action: suspend (client: Client) -> T?) = action.invoke(client)

    override fun close() {
        client.close()
    }
}

private fun ClientBuilder.authUser(configuration: EtcdServerConfiguration): ClientBuilder {
    when (val sslConf = configuration.sslConfiguration) {
        is NoSslConfiguration -> {
            if (configuration.user.isNotEmpty()) {
                this.user(configuration.user.toByteSequence())
                val password = CredentialsService.instance.getPassword(PasswordKey(configuration.id))
                if (password != null) {
                    this.password(password.toByteSequence())
                }
            }
        }
        is EtcdSslConfiguration -> {
            val sslContext = GrpcSslContexts
                .forClient()
                .trustManager(createFile(sslConf.certificate))
                .keyManager(createFile(sslConf.certificateAuthority), createFile(sslConf.certificateKey))
                .build()
            this.sslContext(sslContext)
        }
    }
    return this
}

private fun createFile(path: String): File? = path.nullize()?.let { File(it) }
