package com.github.dxahtepb.etcdidea.model

data class EtcdSslConfiguration(
    val sslEnabled: Boolean = false,
    val certificate: String = "",
    val certificateKey: String = "",
    val certificateAuthority: String = ""
)
