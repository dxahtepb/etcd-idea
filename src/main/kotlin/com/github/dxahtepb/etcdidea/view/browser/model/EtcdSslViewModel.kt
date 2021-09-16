package com.github.dxahtepb.etcdidea.view.browser.model

import com.github.dxahtepb.etcdidea.model.EtcdSslConfiguration

data class EtcdSslViewModel(
    var certificate: String = "",
    var certificateKey: String = "",
    var certificateAuthority: String = "",
    var sslEnabled: Boolean = false
) {
    fun toConfiguration() = EtcdSslConfiguration(
        certificate = certificate,
        certificateKey = certificateKey,
        certificateAuthority = certificateAuthority,
        sslEnabled = sslEnabled
    )
    companion object {
        fun fromConfiguration(conf: EtcdSslConfiguration?) = EtcdSslViewModel(
            conf?.certificate.orEmpty(),
            conf?.certificateKey.orEmpty(),
            conf?.certificateAuthority.orEmpty(),
            conf?.sslEnabled ?: false
        )
    }
}
