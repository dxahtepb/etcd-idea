package com.github.dxahtepb.etcdidea.model

sealed class EtcdAuthenticationConfiguration

data class EtcdSslConfiguration(
    val certificate: String = "",
    val certificateKey: String = "",
    val certificateAuthority: String = ""
) : EtcdAuthenticationConfiguration()

object NoSslConfiguration : EtcdAuthenticationConfiguration()
