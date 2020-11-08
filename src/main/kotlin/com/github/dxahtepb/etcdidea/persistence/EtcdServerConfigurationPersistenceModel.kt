package com.github.dxahtepb.etcdidea.persistence

import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.intellij.openapi.components.BaseState

class EtcdServerConfigurationPersistenceModel : BaseState() {
    var label by string("")
    var hosts by string("")
    var user by string("")
    var password by string("")

    companion object Converter {
        fun toConfiguration(stored: EtcdServerConfigurationPersistenceModel) =
            EtcdServerConfiguration(
                stored.hosts.orEmpty(),
                stored.user.orEmpty(),
                stored.label.orEmpty(),
                stored.password.orEmpty()
            )

        fun fromConfiguration(conf: EtcdServerConfiguration) =
            EtcdServerConfigurationPersistenceModel().apply {
                hosts = conf.hosts
                label = conf.label
                user = conf.user
                password = conf.password
            }
    }
}

private fun String?.orEmpty() = this ?: ""
