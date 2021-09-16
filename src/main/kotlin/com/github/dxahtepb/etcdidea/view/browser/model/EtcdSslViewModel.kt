package com.github.dxahtepb.etcdidea.view.browser.model

import com.github.dxahtepb.etcdidea.model.EtcdSslConfiguration

data class EtcdSslViewModel(
    var rootCertificate: String = "",
    var clientKey: String = "",
    var clientKeyChain: String = "",
    var sslEnabled: Boolean = false
) {
    fun toConfiguration() = EtcdSslConfiguration(
        rootCertificate = rootCertificate,
        clientKey = clientKey,
        clientKeyChain = clientKeyChain,
        sslEnabled = sslEnabled
    )
    companion object {
        fun fromConfiguration(conf: EtcdSslConfiguration?) = EtcdSslViewModel(
            conf?.rootCertificate.orEmpty(),
            conf?.clientKey.orEmpty(),
            conf?.clientKeyChain.orEmpty(),
            conf?.sslEnabled ?: false
        )
    }
}
