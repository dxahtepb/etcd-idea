package com.github.dxahtepb.etcdidea.persistence

import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.persistence.EtcdServerConfigurationPersistenceModel.Converter
import com.github.dxahtepb.etcdidea.persistence.EtcdServerConfigurationPersistenceModel.Converter.fromConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@State(
    name = "EtcdServerConfigurationState",
    storages = [
        Storage("etcdServerSettings.xml")
    ],
    reportStatistic = false
)
class EtcdConfigurationStateComponent :
    SimplePersistentStateComponent<EtcdServerConfigurationState>(EtcdServerConfigurationState()) {

    fun addEtcdConfiguration(conf: EtcdServerConfiguration) = state.servers.add(fromConfiguration(conf))

    fun removeConfiguration(conf: EtcdServerConfiguration) = state.servers.remove(fromConfiguration(conf))

    fun upsertConfiguration(conf: EtcdServerConfiguration) {
        val confModel = fromConfiguration(conf)
        val prevConfPlace = state.servers.indexOf(confModel)
        if (prevConfPlace == -1) {
            state.servers.add(confModel)
        } else {
            state.servers[prevConfPlace] = confModel
            state.intIncrementModificationCount()
        }
    }

    fun getConfigurations() = state.servers.map(Converter::toConfiguration)

    companion object {
        @JvmStatic
        fun getInstance(project: Project): EtcdConfigurationStateComponent = project.service()
    }
}

class EtcdServerConfigurationState : BaseState() {
    var servers by list<EtcdServerConfigurationPersistenceModel>()
}
