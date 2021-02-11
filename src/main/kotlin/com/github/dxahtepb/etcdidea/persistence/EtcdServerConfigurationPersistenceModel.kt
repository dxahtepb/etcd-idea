package com.github.dxahtepb.etcdidea.persistence

import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.intellij.openapi.components.BaseState

class EtcdServerConfigurationPersistenceModel : BaseState() {
    var id by string("")
    var label by string("")
    var hosts by string("")
    var user by string("")
    var password by string("")

    companion object Converter {
        fun toConfiguration(stored: EtcdServerConfigurationPersistenceModel) =
            EtcdServerConfiguration(
                stored.id.orEmpty(),
                stored.hosts.orEmpty(),
                stored.user.orEmpty(),
                stored.label.orEmpty(),
                stored.password.orEmpty()
            )

        fun fromConfiguration(conf: EtcdServerConfiguration) =
            EtcdServerConfigurationPersistenceModel().apply {
                id = conf.id
                hosts = conf.hosts
                label = conf.label
                user = conf.user
                password = conf.password
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
