package com.github.dxahtepb.etcdidea.persistence

import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.model.EtcdSslConfiguration
import com.github.dxahtepb.etcdidea.model.EtcdTimeoutConfiguration
import com.intellij.openapi.components.BaseState
import java.time.Duration

class EtcdServerConfigurationPersistenceModel : BaseState() {
    var id by string("")
    var label by string("")
    var hosts by string("")
    var user by string("")
    var isSslEnabled by property(false)
    var clientCertificate by string("")
    var clientKey by string("")
    var rootCertificate by string("")
    var applicationTimeoutMillis by property(10_000L)

    companion object Converter {
        fun toConfiguration(stored: EtcdServerConfigurationPersistenceModel) =
            EtcdServerConfiguration(
                stored.hosts.orEmpty(),
                stored.user.orEmpty(),
                stored.label.orEmpty(),
                id = stored.id.orEmpty(),
                timeouts = EtcdTimeoutConfiguration(Duration.ofMillis(stored.applicationTimeoutMillis)),
                sslConfiguration = EtcdSslConfiguration(
                    sslEnabled = stored.isSslEnabled,
                    rootCertificate = stored.rootCertificate.orEmpty(),
                    clientKeyChain = stored.clientCertificate.orEmpty(),
                    clientKey = stored.clientKey.orEmpty()
                )
            )

        fun fromConfiguration(conf: EtcdServerConfiguration) =
            EtcdServerConfigurationPersistenceModel().apply {
                id = conf.id
                hosts = conf.hosts
                label = conf.label
                user = conf.user
                isSslEnabled = conf.sslConfiguration.sslEnabled
                rootCertificate = conf.sslConfiguration.rootCertificate
                clientCertificate = conf.sslConfiguration.clientKeyChain
                clientKey = conf.sslConfiguration.clientKey
                applicationTimeoutMillis = conf.timeouts.applicationTimeout.toMillis()
            }
    }

    override fun equals(other: Any?): Boolean = (this === other) ||
        (other is EtcdServerConfigurationPersistenceModel && id == other.id)

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        return result
    }
}
