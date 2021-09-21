package com.github.dxahtepb.etcdidea.service.auth

import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.model.EtcdSslConfiguration
import com.github.dxahtepb.etcdidea.toByteSequence
import com.intellij.util.text.nullize
import io.etcd.jetcd.ClientBuilder
import io.grpc.netty.GrpcSslContexts
import java.io.File

interface EtcdAuthenticator {
    fun authenticate(builder: ClientBuilder): ClientBuilder
}

fun createAuthenticatorFor(configuration: EtcdServerConfiguration) =
    if (configuration.sslConfiguration.sslEnabled) EtcdSslAuthenticator(configuration.sslConfiguration)
    else EtcdPasswordAuthenticator(configuration.id, configuration.user)

class EtcdPasswordAuthenticator(private val configurationId: String, private val user: String) : EtcdAuthenticator {
    override fun authenticate(builder: ClientBuilder): ClientBuilder {
        if (user.isNotEmpty()) {
            builder.user(user.toByteSequence())
            val password = CredentialsService.instance.getPassword(PasswordKey(configurationId))
            if (password != null) {
                builder.password(password.toByteSequence())
            }
        }
        return builder
    }
}

class EtcdSslAuthenticator(private val sslConfiguration: EtcdSslConfiguration) : EtcdAuthenticator {
    override fun authenticate(builder: ClientBuilder): ClientBuilder {
        return GrpcSslContexts
            .forClient()
            .trustManager(createFile(sslConfiguration.rootCertificate))
            .keyManager(createFile(sslConfiguration.clientKeyChain), createFile(sslConfiguration.clientKey))
            .build()
            .let { builder.sslContext(it) }
    }

    private fun createFile(path: String): File? = path.nullize()?.let { File(it) }
}
