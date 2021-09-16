package com.github.dxahtepb.etcdidea.model

data class EtcdSslConfiguration(
    val sslEnabled: Boolean = false,
    val rootCertificate: String = "",
    val clientKey: String = "",
    val clientKeyChain: String = ""
)
