package com.github.dxahtepb.etcdidea.service.auth

interface EtcdAuthenticator {
    fun authenticate()
}

data class EtcdPasswordAuthenticator(val user: String, val password: String) : EtcdAuthenticator {
    override fun authenticate() {
        TODO("Not yet implemented")
    }
}
